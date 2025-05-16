package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.ultilities.ERole;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RoleServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceTest.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        // Clean up any existing test data before each test
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            transactionTemplate.execute(status -> {
                try {
                    // Find all test roles
                    roleRepository.findAll().stream()
                        .filter(role -> role.getName() != null && (
                            role.getName().name().startsWith("TEST_")))
                        .forEach(role -> {
                            try {
                                roleRepository.delete(role);
                                logger.info("Successfully deleted test role: {}", role.getName());
                            } catch (Exception e) {
                                logger.error("Error deleting role {}: {}", role.getName(), e.getMessage());
                            }
                        });
                } catch (Exception e) {
                    logger.error("Error during test data cleanup: {}", e.getMessage());
                }
                return null;
            });
        } catch (Exception e) {
            logger.error("Transaction error during test data cleanup: {}", e.getMessage());
        }
    }

    /**
     * Test Case ID: UT_AM_03
     * Purpose: Test finding role by name with existing role
     * 
     * Prerequisites:
     * - Database is accessible
     * - Role exists in database
     * 
     * Test Steps:
     * 1. Create a test role
     * 2. Find role by name
     * 
     * Expected Results:
     * - Role is found successfully
     * - Retrieved role matches created role
     */
    @Test
    @DisplayName("Test find role by name with existing role")
    void testFindByNameWithExistingRole() {
        transactionTemplate.execute(status -> {
            // First ensure no existing roles with this name
            roleRepository.findAll().stream()
                .filter(role -> role.getName() == ERole.ROLE_STUDENT)
                .forEach(role -> {
                    roleRepository.delete(role);
                    logger.info("Deleted existing role: {}", role.getName());
                });

            // Create a test role
            Role role = new Role();
            role.setName(ERole.ROLE_STUDENT);
            role = roleRepository.save(role);
            logger.info("Created test role: {}", role.getName());

            // Find role by name
            Optional<Role> foundRole = roleService.findByName(ERole.ROLE_STUDENT);

            // Verify role was found correctly
            assertTrue(foundRole.isPresent());
            assertEquals(ERole.ROLE_STUDENT, foundRole.get().getName());
            assertEquals(role.getId(), foundRole.get().getId());

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_04
     * Purpose: Test finding role by name with non-existent role
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Try to find a non-existent role
     * 
     * Expected Results:
     * - Role is not found
     * - Optional is empty
     */
    @Test
    @DisplayName("Test find role by name with non-existent role")
    void testFindByNameWithNonExistentRole() {
        transactionTemplate.execute(status -> {
            // Try to find a non-existent role
            Optional<Role> foundRole = roleService.findByName(ERole.ROLE_ADMIN);

            // Verify role was not found
            assertFalse(foundRole.isPresent());

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_05
     * Purpose: Test finding role by name with null name
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Try to find a role with null name
     * 
     * Expected Results:
     * - IllegalArgumentException is thrown
     */
    @Test
    @DisplayName("Test find role by name with null name")
    void testFindByNameWithNullName() {
        transactionTemplate.execute(status -> {
            // Try to find a role with null name
            assertThrows(IllegalArgumentException.class, () -> {
                roleService.findByName(null);
            });

            return null;
        });
    }

    // Helper method to create test role
    private Role createTestRole(ERole name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }
} 