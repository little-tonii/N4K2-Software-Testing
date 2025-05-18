package com.thanhtam.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.PartRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
public class PartServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(
        PartServiceTest.class
    );

    @Autowired
    private PartService partService;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private CourseRepository courseRepository;

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
                    // Find all test parts
                    List<Part> testParts = partRepository
                        .findAll()
                        .stream()
                        .filter(
                            part ->
                                part.getName() != null &&
                                (part.getName().startsWith("testPart_") ||
                                    part.getName().startsWith("searchPart_"))
                        )
                        .collect(Collectors.toList());

                    // Delete all test parts in a single transaction
                    for (Part part : testParts) {
                        try {
                            partRepository.delete(part);
                            logger.info(
                                "Successfully deleted test part: {}",
                                part.getName()
                            );
                        } catch (Exception e) {
                            logger.error(
                                "Error deleting part {}: {}",
                                part.getName(),
                                e.getMessage()
                            );
                        }
                    }
                } catch (Exception e) {
                    logger.error(
                        "Error during test data cleanup: {}",
                        e.getMessage()
                    );
                }
                return null;
            });
        } catch (Exception e) {
            logger.error(
                "Transaction error during test data cleanup: {}",
                e.getMessage()
            );
        }
    }

    /**
     * Test Case ID: UT_CM_PST_001
     * Purpose: Test part creation with valid data
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Create a new part with valid data
     * 3. Save the part using partService
     * 4. Verify the part was saved correctly
     *
     * Expected Results:
     * - Part is created successfully
     * - All part data is saved correctly
     * - Part can be retrieved from database
     */
    @Test
    @DisplayName("Test create part with valid data")
    void testCreatePartWithValidData() {
        transactionTemplate.execute(status -> {
            // Create a test course
            Course course = createTestCourse();
            Course savedCourse = courseRepository.save(course);

            // Create a new part with unique name
            String uniqueName =
                "testPart_" + UUID.randomUUID().toString().substring(0, 8);
            Part part = new Part();
            part.setName(uniqueName);
            part.setCourse(savedCourse);

            // Save the part
            partService.savePart(part);

            // Verify the part was saved correctly
            Optional<Part> savedPart = partService.findPartById(part.getId());
            assertTrue(savedPart.isPresent());
            assertEquals(uniqueName, savedPart.get().getName());
            assertEquals(
                savedCourse.getId(),
                savedPart.get().getCourse().getId()
            );

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_PST_002
     * Purpose: Test retrieving parts by course with pagination
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     * - Multiple test parts exist in database
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Create multiple test parts
     * 3. Retrieve parts by course with pagination
     *
     * Expected Results:
     * - Parts are retrieved with correct pagination
     * - All parts belong to the specified course
     */
    @Test
    @DisplayName("Test get parts by course with pagination")
    void testGetPartListByCourse() {
        transactionTemplate.execute(status -> {
            // Create a test course
            Course course = createTestCourse();
            Course savedCourse = courseRepository.save(course);

            // Create multiple test parts
            for (int i = 0; i < 5; i++) {
                String uniqueName =
                    "testPart_" + UUID.randomUUID().toString().substring(0, 8);
                Part part = new Part();
                part.setName(uniqueName);
                part.setCourse(savedCourse);
                partService.savePart(part);
            }

            // Test pagination
            Pageable pageable = PageRequest.of(0, 2);
            Page<Part> partPage = partService.getPartLisByCourse(
                pageable,
                savedCourse.getId()
            );

            assertNotNull(partPage);
            assertEquals(2, partPage.getSize());
            assertTrue(partPage.getTotalElements() >= 5);
            assertTrue(
                partPage
                    .getContent()
                    .stream()
                    .allMatch(part ->
                        part.getCourse().getId().equals(savedCourse.getId())
                    )
            );

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_PST_003
     * Purpose: Test retrieving all parts by course
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     * - Multiple test parts exist in database
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Create multiple test parts
     * 3. Retrieve all parts by course
     *
     * Expected Results:
     * - All parts for the course are retrieved
     * - Parts are in the correct order
     */
    @Test
    @DisplayName("Test get all parts by course")
    void testGetPartListByCourseWithoutPagination() {
        transactionTemplate.execute(status -> {
            // Create a test course
            Course course = createTestCourse();
            Course savedCourse = courseRepository.save(course);

            // Create multiple test parts
            for (int i = 0; i < 3; i++) {
                String uniqueName =
                    "testPart_" + UUID.randomUUID().toString().substring(0, 8);
                Part part = new Part();
                part.setName(uniqueName);
                part.setCourse(savedCourse);
                partService.savePart(part);
            }

            // Get all parts for the course
            List<Part> parts = partService.getPartListByCourse(savedCourse);

            assertNotNull(parts);
            assertEquals(3, parts.size());
            assertTrue(
                parts
                    .stream()
                    .allMatch(part ->
                        part.getCourse().getId().equals(savedCourse.getId())
                    )
            );

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_PST_004
     * Purpose: Test finding part by ID
     *
     * Prerequisites:
     * - Database is accessible
     * - Test part exists in database
     *
     * Test Steps:
     * 1. Create a test course and part
     * 2. Find part by ID
     *
     * Expected Results:
     * - Part is found successfully
     * - Retrieved part data matches created part
     */
    @Test
    @DisplayName("Test find part by ID")
    void testFindPartById() {
        transactionTemplate.execute(status -> {
            // Create a test course and part
            Course course = createTestCourse();
            Course savedCourse = courseRepository.save(course);

            String uniqueName =
                "testPart_" + UUID.randomUUID().toString().substring(0, 8);
            Part part = new Part();
            part.setName(uniqueName);
            part.setCourse(savedCourse);
            partService.savePart(part);

            // Find part by ID
            Optional<Part> foundPart = partService.findPartById(part.getId());

            assertTrue(foundPart.isPresent());
            assertEquals(uniqueName, foundPart.get().getName());
            assertEquals(
                savedCourse.getId(),
                foundPart.get().getCourse().getId()
            );

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_PST_005
     * Purpose: Test checking part existence by ID
     *
     * Prerequisites:
     * - Database is accessible
     * - Test part exists in database
     *
     * Test Steps:
     * 1. Create a test course and part
     * 2. Check if part exists by ID
     *
     * Expected Results:
     * - Part existence check returns correct result
     */
    @Test
    @DisplayName("Test check part existence by ID")
    void testExistsById() {
        transactionTemplate.execute(status -> {
            // Create a test course and part
            Course course = createTestCourse();
            Course savedCourse = courseRepository.save(course);

            String uniqueName =
                "testPart_" + UUID.randomUUID().toString().substring(0, 8);
            Part part = new Part();
            part.setName(uniqueName);
            part.setCourse(savedCourse);
            partService.savePart(part);

            // Check if part exists
            assertTrue(partService.existsById(part.getId()));
            assertFalse(partService.existsById(999L)); // Non-existent ID

            return null;
        });
    }

    // Helper method to create test course
    private Course createTestCourse() {
        Course course = new Course();
        course.setCourseCode(
            "TEST_" + UUID.randomUUID().toString().substring(0, 4)
        );
        course.setName(
            "Test Course " + UUID.randomUUID().toString().substring(0, 8)
        );
        course.setImgUrl("test.jpg");
        return course;
    }
}
