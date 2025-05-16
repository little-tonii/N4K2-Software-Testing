package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.repository.IntakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class IntakeServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(IntakeServiceTest.class);

    @Autowired
    private IntakeService intakeService;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            transactionTemplate.execute(status -> {
                try {
                    // Find all test intakes
                    List<Intake> testIntakes = intakeRepository.findAll().stream()
                        .filter(intake -> intake.getName() != null && intake.getName().startsWith("Test Intake_"))
                        .collect(Collectors.toList());

                    // Delete all test intakes
                    for (Intake intake : testIntakes) {
                        intakeRepository.delete(intake);
                        logger.info("Successfully deleted test intake: {}", intake.getName());
                    }

                    return null;
                } catch (Exception e) {
                    logger.error("Error during test data cleanup: {}", e.getMessage());
                    status.setRollbackOnly();
                    throw e;
                }
            });
        } catch (Exception e) {
            logger.error("Transaction error during test data cleanup: {}", e.getMessage());
        }
    }

    /**
     * Test Case ID: UT_CM_08
     * Purpose: Test finding intake by code
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test intake exists in database
     * 
     * Test Steps:
     * 1. Create a test intake with a unique code
     * 2. Find intake by code
     * 
     * Expected Results:
     * - Intake is found successfully
     * - Retrieved intake data matches created intake
     */
    @Test
    @DisplayName("Test find intake by code")
    void testFindByCode() {
        transactionTemplate.execute(status -> {
            // Create test intake
            String uniqueCode = "TEST_" + UUID.randomUUID().toString().substring(0, 4);
            Intake intake = new Intake();
            intake.setName("Test Intake_" + uniqueCode);
            intake.setIntakeCode(uniqueCode);
            Intake savedIntake = intakeRepository.save(intake);

            // Find intake by code
            Intake foundIntake = intakeService.findByCode(uniqueCode);

            // Verify results
            assertNotNull(foundIntake);
            assertEquals(savedIntake.getId(), foundIntake.getId());
            assertEquals(savedIntake.getName(), foundIntake.getName());
            assertEquals(savedIntake.getIntakeCode(), foundIntake.getIntakeCode());

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_09
     * Purpose: Test finding intake by ID
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test intake exists in database
     * 
     * Test Steps:
     * 1. Create a test intake
     * 2. Find intake by ID
     * 
     * Expected Results:
     * - Intake is found successfully
     * - Retrieved intake data matches created intake
     */
    @Test
    @DisplayName("Test find intake by ID")
    void testFindById() {
        transactionTemplate.execute(status -> {
            // Create test intake
            String uniqueCode = "TEST_" + UUID.randomUUID().toString().substring(0, 4);
            Intake intake = new Intake();
            intake.setName("Test Intake_" + uniqueCode);
            intake.setIntakeCode(uniqueCode);
            Intake savedIntake = intakeRepository.save(intake);

            // Find intake by ID
            Optional<Intake> foundIntake = intakeService.findById(savedIntake.getId());

            // Verify results
            assertTrue(foundIntake.isPresent());
            assertEquals(savedIntake.getId(), foundIntake.get().getId());
            assertEquals(savedIntake.getName(), foundIntake.get().getName());
            assertEquals(savedIntake.getIntakeCode(), foundIntake.get().getIntakeCode());

            // Test with non-existent ID
            Optional<Intake> notFoundIntake = intakeService.findById(99999L);
            assertFalse(notFoundIntake.isPresent());

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_10
     * Purpose: Test finding all intakes
     * 
     * Prerequisites:
     * - Database is accessible
     * - Multiple test intakes exist in database
     * 
     * Test Steps:
     * 1. Create multiple test intakes
     * 2. Find all intakes
     * 
     * Expected Results:
     * - All intakes are found successfully
     * - Retrieved intakes include all created test intakes
     */
    @Test
    @DisplayName("Test find all intakes")
    void testFindAll() {
        transactionTemplate.execute(status -> {
            // Create multiple test intakes
            int numIntakes = 3;
            for (int i = 0; i < numIntakes; i++) {
                String uniqueCode = "TEST_" + UUID.randomUUID().toString().substring(0, 4);
                Intake intake = new Intake();
                intake.setName("Test Intake_" + uniqueCode);
                intake.setIntakeCode(uniqueCode);
                intakeRepository.save(intake);
            }

            // Find all intakes
            List<Intake> intakes = intakeService.findAll();

            // Verify results
            assertNotNull(intakes);
            assertTrue(intakes.size() >= numIntakes);
            assertTrue(intakes.stream()
                .filter(intake -> intake.getName() != null && intake.getName().startsWith("Test Intake_"))
                .count() >= numIntakes);

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_11
     * Purpose: Test finding intake by non-existent code
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Try to find intake with non-existent code
     * 
     * Expected Results:
     * - NoSuchElementException is thrown
     */
    @Test
    @DisplayName("Test find intake by non-existent code")
    void testFindByNonExistentCode() {
        // Try to find intake with non-existent code
        String nonExistentCode = "NONEXISTENT_" + UUID.randomUUID().toString();
        
        assertThrows(java.util.NoSuchElementException.class, () -> {
            intakeService.findByCode(nonExistentCode);
        });
    }

    /**
     * Test Case ID: UT_CM_12
     * Purpose: Test finding intake by null code
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Try to find intake with null code
     * 
     * Expected Results:
     * - IllegalArgumentException is thrown
     */
    @Test
    @DisplayName("Test find intake by null code")
    void testFindByNullCode() {
        // Try to find intake with null code
        assertThrows(IllegalArgumentException.class, () -> {
            intakeService.findByCode(null);
        });
    }

    /**
     * Test Case ID: UT_CM_13
     * Purpose: Test finding intake by empty code
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Try to find intake with empty code
     * 
     * Expected Results:
     * - IllegalArgumentException is thrown
     */
    @Test
    @DisplayName("Test find intake by empty code")
    void testFindByEmptyCode() {
        // Try to find intake with empty code
        assertThrows(IllegalArgumentException.class, () -> {
            intakeService.findByCode("");
        });
    }
} 