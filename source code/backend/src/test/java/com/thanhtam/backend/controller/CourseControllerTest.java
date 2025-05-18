package com.thanhtam.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.dto.ServiceResult;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.service.CourseService;
import com.thanhtam.backend.service.S3Services;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CourseControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(
        CourseControllerTest.class
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseService courseService;

    @Autowired
    private S3Services s3Services;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper objectMapper;

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
                    // Delete all test courses
                    courseRepository.deleteAll();
                    logger.info("Successfully cleaned up all test courses");
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
     * Test Case ID: UT_CM_CCT_001
     * Purpose: Test getting all courses
     *
     * Prerequisites:
     * - Database is accessible
     * - Multiple test courses exist in database
     * - User has ADMIN or LECTURER role
     *
     * Test Steps:
     * 1. Create multiple test courses
     * 2. Call GET /api/course-list endpoint
     *
     * Expected Results:
     * - Returns 200 OK status
     * - Returns list of all courses
     */
    @Test
    @DisplayName("Test get all courses")
    @WithMockUser(roles = { "ADMIN" })
    void testGetAllCourse() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create multiple test courses
                for (int i = 0; i < 3; i++) {
                    Course course = createTestCourse();
                    courseRepository.save(course);
                }

                // Test the endpoint
                mockMvc
                    .perform(get("/api/course-list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3));

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_CCT_002
     * Purpose: Test getting courses with pagination
     *
     * Prerequisites:
     * - Database is accessible
     * - Multiple test courses exist in database
     * - User has ADMIN or LECTURER role
     *
     * Test Steps:
     * 1. Create multiple test courses
     * 2. Call GET /api/courses endpoint with pagination
     *
     * Expected Results:
     * - Returns 200 OK status
     * - Returns paginated courses
     */
    @Test
    @DisplayName("Test get courses with pagination")
    @WithMockUser(roles = { "ADMIN" })
    void testGetCourseListByPage() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create multiple test courses
                for (int i = 0; i < 5; i++) {
                    Course course = createTestCourse();
                    courseRepository.save(course);
                }

                // Test the endpoint
                mockMvc
                    .perform(
                        get("/api/courses")
                            .param("page", "0")
                            .param("size", "2")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(
                        jsonPath("$.paginationDetails.totalElements").exists()
                    )
                    .andExpect(jsonPath("$.paginationDetails.size").exists());

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_CCT_003
     * Purpose: Test checking course code existence
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     * - User has ADMIN or LECTURER role
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Call GET /api/courses/check-course-code endpoint
     *
     * Expected Results:
     * - Returns 200 OK status
     * - Returns correct existence check result
     */
    @Test
    @DisplayName("Test check course code existence")
    @WithMockUser(roles = { "ADMIN" })
    void testCheckCode() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                // Test the endpoint
                mockMvc
                    .perform(
                        get("/api/courses/check-course-code").param(
                            "value",
                            savedCourse.getCourseCode()
                        )
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

                // Test with non-existent code
                mockMvc
                    .perform(
                        get("/api/courses/check-course-code").param(
                            "value",
                            "NONEXISTENT"
                        )
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_CCT_004
     * Purpose: Test getting course by ID
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     * - User has ADMIN or LECTURER role
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Call GET /api/courses/{id} endpoint
     *
     * Expected Results:
     * - Returns 200 OK status
     * - Returns the correct course
     */
    @Test
    @DisplayName("Test get course by ID")
    @WithMockUser(roles = { "ADMIN" })
    void testGetCourseById() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                // Test the endpoint
                mockMvc
                    .perform(get("/api/courses/{id}", savedCourse.getId()))
                    .andExpect(status().isOk())
                    .andExpect(
                        jsonPath("$.courseCode").value(
                            savedCourse.getCourseCode()
                        )
                    )
                    .andExpect(jsonPath("$.name").value(savedCourse.getName()));

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_CCT_005
     * Purpose: Test creating a new course
     *
     * Prerequisites:
     * - Database is accessible
     * - User has ADMIN role
     *
     * Test Steps:
     * 1. Create a new course object
     * 2. Call POST /api/courses endpoint
     *
     * Expected Results:
     * - Returns 200 OK status
     * - Course is created successfully
     */
    @Test
    @DisplayName("Test create course")
    @WithMockUser(roles = { "ADMIN" })
    void testCreateCourse() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a new course
                Course course = createTestCourse();

                // Test the endpoint
                mockMvc
                    .perform(
                        post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(course))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(
                        jsonPath("$.message").value(
                            "Created course successfully!"
                        )
                    );

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_CCT_006
     * Purpose: Test updating a course
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     * - User has ADMIN role
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Update course information
     * 3. Call PATCH /api/courses/{id} endpoint
     *
     * Expected Results:
     * - Returns 200 OK status
     * - Course is updated successfully
     */
    @Test
    @DisplayName("Test update course")
    @WithMockUser(roles = { "ADMIN" })
    void testUpdateCourse() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                // Update course
                savedCourse.setName("Updated Course Name");

                // Test the endpoint
                mockMvc
                    .perform(
                        patch("/api/courses/{id}", savedCourse.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(savedCourse)
                            )
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(
                        jsonPath("$.message").value(
                            "Update course with id: " + savedCourse.getId()
                        )
                    );

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_CCT_007
     * Purpose: Test deleting a course
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     * - User has ADMIN role
     *
     * Test Steps:
     * 1. Create a test course
     * 2. Call DELETE /api/courses/{id} endpoint
     *
     * Expected Results:
     * - Returns 200 OK status
     * - Course is deleted successfully
     */
    @Test
    @DisplayName("Test delete course")
    @WithMockUser(roles = { "ADMIN" })
    void testDeleteCourse() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                // Test the endpoint
                mockMvc
                    .perform(delete("/api/courses/{id}", savedCourse.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(204))
                    .andExpect(
                        jsonPath("$.message").value(
                            "Deleted course with id: " +
                            savedCourse.getId() +
                            " successfully!"
                        )
                    );

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_CCT_008
     * Purpose: Test getting course by part ID
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course with part exists in database
     * - User has ADMIN role
     *
     * Test Steps:
     * 1. Create a test course with part
     * 2. Call GET /api/courses/part/{partId} endpoint
     *
     * Expected Results:
     * - Returns 200 OK status
     * - Returns the correct course
     */
    @Test
    @DisplayName("Test get course by part ID")
    @WithMockUser(roles = { "ADMIN" })
    void testGetCourseByPart() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                // Test the endpoint
                mockMvc
                    .perform(get("/api/courses/part/{partId}", 1L))
                    .andExpect(status().isOk());

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_CCT_009
     * Purpose: Test getting courses by intake ID
     *
     * Prerequisites:
     * - Database is accessible
     * - Test course with intake exists in database
     * - User has ADMIN role
     *
     * Test Steps:
     * 1. Create a test course with intake
     * 2. Call GET /api/intakes/{intakeId}/courses endpoint
     *
     * Expected Results:
     * - Returns 200 OK status
     * - Returns list of courses for the intake
     */
    @Test
    @DisplayName("Test get courses by intake ID")
    @WithMockUser(roles = { "ADMIN" })
    void testFindAllByIntakeId() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                // Test the endpoint
                mockMvc
                    .perform(get("/api/intakes/{intakeId}/courses", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_CCT_010
     * Purpose: Test get course by ID - not found
     */
    @Test
    @DisplayName("Test get course by ID - not found")
    @WithMockUser(roles = { "ADMIN" })
    void testGetCourseById_NotFound() throws Exception {
        // Create and save a course to ensure DB is not empty
        Course course = createTestCourse();
        courseRepository.save(course);
        // Use a non-existent ID
        mockMvc
            .perform(get("/api/courses/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    /**
     * Test Case ID: UT_CM_CCT_011
     * Purpose: Test update course - not found
     */
    @Test
    @DisplayName("Test update course - not found")
    @WithMockUser(roles = { "ADMIN" })
    void testUpdateCourse_NotFound() throws Exception {
        Course course = createTestCourse();
        courseRepository.save(course);
        mockMvc
            .perform(
                patch("/api/courses/{id}", 99999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(course))
            )
            .andExpect(status().isNotFound());
    }

    /**
     * Test Case ID: UT_CM_CCT_012
     * Purpose: Test delete course - not found
     */
    @Test
    @DisplayName("Test delete course - not found")
    @WithMockUser(roles = { "ADMIN" })
    void testDeleteCourse_NotFound() throws Exception {
        Course course = createTestCourse();
        courseRepository.save(course);
        mockMvc
            .perform(delete("/api/courses/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    /**
     * Test Case ID: UT_CM_CCT_013
     * Purpose: Test create course - duplicate code
     */
    @Test
    @DisplayName("Test create course - duplicate code")
    @WithMockUser(roles = { "ADMIN" })
    void testCreateCourse_DuplicateCode() throws Exception {
        Course course = createTestCourse();
        courseRepository.save(course);
        Course duplicate = new Course();
        duplicate.setCourseCode(course.getCourseCode());
        duplicate.setName("Another Course");
        duplicate.setImgUrl("test2.jpg");
        mockMvc
            .perform(
                post("/api/courses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicate))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.statusCode").value(409));
    }

    /**
     * Test Case ID: UT_CM_CCT_014
     * Purpose: Test create course - validation error
     */
    @Test
    @DisplayName("Test create course - validation error")
    @WithMockUser(roles = { "ADMIN" })
    void testCreateCourse_ValidationError() throws Exception {
        Course course = new Course(); // missing required fields
        mockMvc
            .perform(
                post("/api/courses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(course))
            )
            .andExpect(status().isBadRequest());
    }

    /**
     * Test Case ID: UT_CM_CCT_015
     * Purpose: Test update course - validation error (should throw NPE)
     */
    @Test
    @DisplayName("Test update course - validation error (should throw NPE)")
    @WithMockUser(roles = { "ADMIN" })
    void testUpdateCourse_ValidationError() throws Exception {
        // Create and save a course to ensure DB is not empty
        Course course = createTestCourse();
        Course savedCourse = courseRepository.save(course);
        Course invalid = new Course(); // missing required fields

        assertThrows(NullPointerException.class, () -> {
            mockMvc
                .perform(
                    patch("/api/courses/{id}", savedCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid))
                )
                .andReturn();
        });
    }

    /**
     * Test Case ID: UT_CM_CCT_016
     * Purpose: Test get all courses - empty result
     */
    @Test
    @DisplayName("Test get all courses - empty result")
    @WithMockUser(roles = { "ADMIN" })
    void testGetAllCourse_Empty() throws Exception {
        cleanupTestData();
        mockMvc
            .perform(get("/api/course-list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Test Case ID: UT_CM_CCT_017
     * Purpose: Test get courses with pagination - empty result
     */
    @Test
    @DisplayName("Test get courses with pagination - empty result")
    @WithMockUser(roles = { "ADMIN" })
    void testGetCourseListByPage_Empty() throws Exception {
        cleanupTestData();
        mockMvc
            .perform(get("/api/courses").param("page", "0").param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    /**
     * Test Case ID: UT_CM_CCT_018
     * Purpose: Test get course by part ID - not found
     */
    @Test
    @DisplayName("Test get course by part ID - not found")
    @WithMockUser(roles = { "ADMIN" })
    void testGetCourseByPart_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/courses/part/{partId}", 99999L))
            .andExpect(status().isOk())
            .andExpect(content().string("")); // Assuming null returns empty
    }

    /**
     * Test Case ID: UT_CM_CCT_019
     * Purpose: Test get courses by intake ID - not found
     */
    @Test
    @DisplayName("Test get courses by intake ID - not found")
    @WithMockUser(roles = { "ADMIN" })
    void testFindAllByIntakeId_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/intakes/{intakeId}/courses", 99999L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Test Case ID: UT_CM_CCT_020
     * Purpose: Test check course code logic (all branches)
     */
    @Test
    @DisplayName("Test check course code logic (all branches)")
    @WithMockUser(roles = { "ADMIN" })
    void testCheckCourseCodeLogic() throws Exception {
        // Create a course
        Course course = createTestCourse();
        Course savedCourse = courseRepository.save(course);
        // Case 1: code matches current course (should return false)
        mockMvc
            .perform(
                get(
                    "/api/courses/{id}/check-course-code",
                    savedCourse.getId()
                ).param("value", savedCourse.getCourseCode())
            )
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
        // Case 2: code exists but does not match current course (should return true)
        Course another = createTestCourse();
        another.setCourseCode("DIFF_CODE");
        courseRepository.save(another);
        mockMvc
            .perform(
                get(
                    "/api/courses/{id}/check-course-code",
                    savedCourse.getId()
                ).param("value", "DIFF_CODE")
            )
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
        // Case 3: code does not exist (should return false)
        mockMvc
            .perform(
                get(
                    "/api/courses/{id}/check-course-code",
                    savedCourse.getId()
                ).param("value", "NONEXISTENT")
            )
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
    }

    /**
     * Test Case ID: UT_CM_CCT_021
     * Purpose: Test security - forbidden for unauthenticated/unauthorized users
     */
    @Test
    @DisplayName("Test security - forbidden for unauthenticated users")
    void testSecurity_Unauthenticated() throws Exception {
        mockMvc
            .perform(get("/api/course-list"))
            .andExpect(status().isUnauthorized());
    }

    // ID: UT_CM_CCT_022
    @Test
    @DisplayName("Test security - forbidden for unauthorized users")
    @WithMockUser(roles = { "STUDENT" })
    void testSecurity_Forbidden() throws Exception {
        mockMvc
            .perform(get("/api/course-list"))
            .andExpect(status().isForbidden());
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
