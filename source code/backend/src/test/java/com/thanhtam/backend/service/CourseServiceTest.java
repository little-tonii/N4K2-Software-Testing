package com.thanhtam.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.repository.PartRepository;
import java.util.ArrayList;
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
public class CourseServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(
        CourseServiceTest.class
    );

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        transactionTemplate.execute(status -> {
            List<Course> testCourses = courseRepository
                .findAll()
                .stream()
                .filter(course -> course.getCourseCode().startsWith("TEST_"))
                .collect(Collectors.toList());
            courseRepository.deleteAll(testCourses);
            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_CST_001
     * Purpose: Test course creation with valid data
     *
     * Prerequisites:
     * - Database is accessible
     * - Intake exists in database
     *
     * Test Steps:
     * 1. Create a new course with valid data
     * 2. Save the course using courseService
     * 3. Verify the course was saved correctly
     *
     * Expected Results:
     * - Course is created successfully
     * - All course data is saved correctly
     * - Course can be retrieved from database
     */
    @Test
    @DisplayName("Test create course with valid data")
    void testCreateCourseWithValidData() {
        transactionTemplate.execute(status -> {
            // Create test intake
            Intake intake = new Intake();
            intake.setName("Test Intake");
            intake = intakeRepository.save(intake);

            // Create a new course with unique code
            String uniqueCode =
                "TEST_" + UUID.randomUUID().toString().substring(0, 4);
            Course course = new Course();
            course.setCourseCode(uniqueCode);
            course.setName("Test Course");
            course.setImgUrl("test.jpg");
            List<Intake> intakes = new ArrayList<>();
            intakes.add(intake);
            course.setIntakes(intakes);

            // Save the course
            courseService.saveCourse(course);

            // Verify the course was saved correctly
            Optional<Course> savedCourse = courseService.getCourseById(
                course.getId()
            );
            assertTrue(savedCourse.isPresent());
            assertEquals(uniqueCode, savedCourse.get().getCourseCode());
            assertEquals("Test Course", savedCourse.get().getName());
            assertEquals("test.jpg", savedCourse.get().getImgUrl());
            assertFalse(savedCourse.get().getIntakes().isEmpty());
            assertEquals(
                intake.getId(),
                savedCourse.get().getIntakes().get(0).getId()
            );

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_CST_002
     * Purpose: Test course retrieval by ID
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Retrieve course by ID
     *
     * Expected Results:
     * - Course is retrieved successfully
     * - Retrieved course data matches created course
     */
    @Test
    @DisplayName("Test get course by ID")
    void testGetCourseById() {
        transactionTemplate.execute(status -> {
            // Create test course
            String uniqueCode =
                "TEST_" + UUID.randomUUID().toString().substring(0, 4);
            Course course = createTestCourse(uniqueCode);
            courseService.saveCourse(course);

            // Get course by ID
            Optional<Course> retrievedCourse = courseService.getCourseById(
                course.getId()
            );

            // Verify course was retrieved correctly
            assertTrue(retrievedCourse.isPresent());
            assertEquals(course.getId(), retrievedCourse.get().getId());
            assertEquals(
                course.getCourseCode(),
                retrievedCourse.get().getCourseCode()
            );
            assertEquals(course.getName(), retrievedCourse.get().getName());

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_CST_003
     * Purpose: Test course existence check by code
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Check if course exists by code
     *
     * Expected Results:
     * - Course existence check returns correct result
     */
    @Test
    @DisplayName("Test check course existence by code")
    void testExistsByCode() {
        transactionTemplate.execute(status -> {
            // Create test course
            String uniqueCode =
                "TEST_" + UUID.randomUUID().toString().substring(0, 4);
            Course course = createTestCourse(uniqueCode);
            courseService.saveCourse(course);

            // Check if course exists
            assertTrue(courseService.existsByCode(uniqueCode));
            assertFalse(courseService.existsByCode("nonexistentCode"));

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_CST_004
     * Purpose: Test course pagination
     *
     * Prerequisites:
     * - Database is accessible
     * - Multiple test courses exist in database
     *
     * Test Steps:
     * 1. Create multiple test courses
     * 2. Retrieve courses with pagination
     *
     * Expected Results:
     * - Courses are retrieved with correct pagination
     */
    @Test
    @DisplayName("Test course pagination")
    void testGetCourseListByPage() {
        transactionTemplate.execute(status -> {
            // Create multiple test courses
            for (int i = 0; i < 5; i++) {
                String uniqueCode =
                    "TEST_" + UUID.randomUUID().toString().substring(0, 4);
                Course course = createTestCourse(uniqueCode);
                courseService.saveCourse(course);
                logger.info("Created test course with code: {}", uniqueCode);
            }

            // Test pagination
            Pageable pageable = PageRequest.of(0, 2);
            Page<Course> coursePage = courseService.getCourseListByPage(
                pageable
            );

            assertNotNull(coursePage);
            assertEquals(2, coursePage.getSize());

            assertTrue(coursePage.getTotalElements() >= 5);

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_CST_005
     * Purpose: Test course deletion
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Delete the course
     * 3. Verify course was deleted
     *
     * Expected Results:
     * - Course is deleted successfully
     * - Course cannot be retrieved after deletion
     */
    @Test
    @DisplayName("Test delete course")
    void testDeleteCourse() {
        transactionTemplate.execute(status -> {
            // Create test course
            String uniqueCode =
                "TEST_" + UUID.randomUUID().toString().substring(0, 4);
            Course course = createTestCourse(uniqueCode);
            courseService.saveCourse(course);

            // Delete course
            courseService.delete(course.getId());

            // Verify course was deleted
            assertFalse(courseService.existsById(course.getId()));
            assertFalse(
                courseService.getCourseById(course.getId()).isPresent()
            );

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_CST_006
     * Purpose: Test getting all courses
     *
     * Prerequisites:
     * - Database is accessible
     * - Multiple test courses exist in database
     *
     * Test Steps:
     * 1. Create multiple test courses
     * 2. Retrieve all courses
     *
     * Expected Results:
     * - All courses are retrieved successfully
     */
    @Test
    @DisplayName("Test get all courses")
    void testGetCourseList() {
        transactionTemplate.execute(status -> {
            // Create multiple test courses
            int numCourses = 5;
            for (int i = 0; i < numCourses; i++) {
                String uniqueCode =
                    "TEST_" + UUID.randomUUID().toString().substring(0, 4);
                Course course = createTestCourse(uniqueCode);
                courseService.saveCourse(course);
            }

            // Get all courses
            List<Course> courses = courseService.getCourseList();

            // Verify results
            assertNotNull(courses);
            assertTrue(courses.size() >= numCourses);
            assertTrue(
                courses
                    .stream()
                    .allMatch(course ->
                        course.getCourseCode().startsWith("TEST_")
                    )
            );

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_CST_007
     * Purpose: Test finding courses by intake ID
     *
     * Prerequisites:
     * - Database is accessible
     * - Test intake exists in database
     * - Multiple test courses exist for the intake
     *
     * Test Steps:
     * 1. Create a test intake
     * 2. Create multiple test courses for the intake
     * 3. Retrieve courses by intake ID
     *
     * Expected Results:
     * - Courses are retrieved successfully for the specified intake
     */
    @Test
    @DisplayName("Test find courses by intake ID")
    void testFindAllByIntakeId() {
        transactionTemplate.execute(status -> {
            // Create test intake
            final Intake intake = new Intake();
            intake.setName("Test Intake");
            final Intake savedIntake = intakeRepository.save(intake);

            // Create multiple test courses for the intake
            int numCourses = 3;
            for (int i = 0; i < numCourses; i++) {
                String uniqueCode =
                    "TEST_" + UUID.randomUUID().toString().substring(0, 4);
                Course course = new Course();
                course.setCourseCode(uniqueCode);
                course.setName("Test Course " + i);
                course.setImgUrl("test.jpg");
                List<Intake> intakes = new ArrayList<>();
                intakes.add(savedIntake);
                course.setIntakes(intakes);
                courseService.saveCourse(course);
            }

            // Get courses by intake ID
            List<Course> courses = courseService.findAllByIntakeId(
                savedIntake.getId()
            );

            // Verify results
            assertNotNull(courses);
            assertEquals(numCourses, courses.size());
            assertTrue(
                courses
                    .stream()
                    .allMatch(course ->
                        course
                            .getIntakes()
                            .stream()
                            .anyMatch(i -> i.getId().equals(savedIntake.getId())
                            )
                    )
            );

            return null;
        });
    }

    /**
     * Test Case ID: UT_CM_CST_008
     * Purpose: Test finding course by part ID
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course and part exist in database
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Create a test part associated with the course
     * 3. Retrieve course by part ID
     *
     * Expected Results:
     * - Course is retrieved successfully for the specified part
     */
    @Test
    @DisplayName("Test find course by part ID")
    void testFindCourseByPartId() {
        transactionTemplate.execute(status -> {
            // Create test course
            String uniqueCode =
                "TEST_" + UUID.randomUUID().toString().substring(0, 4);
            Course course = createTestCourse(uniqueCode);
            courseService.saveCourse(course);

            // Create test part
            Part part = new Part();
            part.setName("Test Part");
            part.setCourse(course);
            part = partRepository.save(part);

            // Get course by part ID
            Course foundCourse = courseService.findCourseByPartId(part.getId());

            // Verify results
            assertNotNull(foundCourse);
            assertEquals(course.getId(), foundCourse.getId());
            assertEquals(course.getCourseCode(), foundCourse.getCourseCode());

            return null;
        });
    }

    // Helper method to create test course
    private Course createTestCourse(String code) {
        // Create test intake
        Intake intake = new Intake();
        intake.setName("Test Intake");
        intake = intakeRepository.save(intake);

        Course course = new Course();
        course.setCourseCode(code);
        course.setName("Test Course");
        course.setImgUrl("test.jpg");
        List<Intake> intakes = new ArrayList<>();
        intakes.add(intake);
        course.setIntakes(intakes);

        return course;
    }
}
