package com.thanhtam.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.PartRepository;
import com.thanhtam.backend.service.CourseService;
import com.thanhtam.backend.service.PartService;
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

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PartControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(PartControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PartService partService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private CourseRepository courseRepository;

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
                    // Find all test parts
                    List<Part> testParts = partRepository.findAll().stream()
                        .filter(part -> part.getName() != null && (
                            part.getName().startsWith("testPart_") ||
                            part.getName().startsWith("searchPart_")))
                        .collect(Collectors.toList());

                    // Delete all test parts in a single transaction
                    for (Part part : testParts) {
                        try {
                            partRepository.delete(part);
                            logger.info("Successfully deleted test part: {}", part.getName());
                        } catch (Exception e) {
                            logger.error("Error deleting part {}: {}", part.getName(), e.getMessage());
                        }
                    }
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
     * Test Case ID: UT_CM_03_02_01
     * Purpose: Test getting parts by course with pagination
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     * - Multiple test parts exist in database
     * - User has ADMIN or LECTURER role
     * 
     * Test Steps:
     * 1. Create a test course
     * 2. Create multiple test parts
     * 3. Call GET /api/courses/{courseId}/parts endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - Returns paginated parts for the course
     */
    @Test
    @DisplayName("Test get parts by course with pagination")
    @WithMockUser(roles = {"ADMIN"})
    void testGetPartListByCourse() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                // Create multiple test parts
                for (int i = 0; i < 5; i++) {
                    String uniqueName = "testPart_" + UUID.randomUUID().toString().substring(0, 8);
                    Part part = new Part();
                    part.setName(uniqueName);
                    part.setCourse(savedCourse);
                    partService.savePart(part);
                }

                // Test the endpoint
                mockMvc.perform(get("/api/courses/{courseId}/parts", savedCourse.getId())
                        .param("page", "0")
                        .param("size", "2"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content").isArray())
                        .andExpect(jsonPath("$.totalElements").value(5))
                        .andExpect(jsonPath("$.size").value(2));

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_03_03_01
     * Purpose: Test getting all parts by course without pagination
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     * - Multiple test parts exist in database
     * - User has ADMIN or LECTURER role
     * 
     * Test Steps:
     * 1. Create a test course
     * 2. Create multiple test parts
     * 3. Call GET /api/courses/{courseId}/part-list endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - Returns all parts for the course
     */
    @Test
    @DisplayName("Test get all parts by course without pagination")
    @WithMockUser(roles = {"ADMIN"})
    void testGetPartListByCourseWithoutPagination() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                // Create multiple test parts
                for (int i = 0; i < 3; i++) {
                    String uniqueName = "testPart_" + UUID.randomUUID().toString().substring(0, 8);
                    Part part = new Part();
                    part.setName(uniqueName);
                    part.setCourse(savedCourse);
                    partService.savePart(part);
                }

                // Test the endpoint
                mockMvc.perform(get("/api/courses/{courseId}/part-list", savedCourse.getId()))
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
     * Test Case ID: UT_CM_03_04_01
     * Purpose: Test getting part by ID
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test part exists in database
     * - User has ADMIN or LECTURER role
     * 
     * Test Steps:
     * 1. Create a test course and part
     * 2. Call GET /api/parts/{id} endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - Returns the correct part
     */
    @Test
    @DisplayName("Test get part by ID")
    @WithMockUser(roles = {"ADMIN"})
    void testGetPartById() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course and part
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                String uniqueName = "testPart_" + UUID.randomUUID().toString().substring(0, 8);
                Part part = new Part();
                part.setName(uniqueName);
                part.setCourse(savedCourse);
                partService.savePart(part);

                // Test the endpoint
                mockMvc.perform(get("/api/parts/{id}", part.getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value(uniqueName))
                        .andExpect(jsonPath("$.course.id").value(savedCourse.getId()));

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_03_06_01
     * Purpose: Test updating part name
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test part exists in database
     * - User has ADMIN role
     * 
     * Test Steps:
     * 1. Create a test course and part
     * 2. Call PATCH /api/parts/{id} endpoint with new name
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - Part name is updated correctly
     */
    @Test
    @DisplayName("Test update part name")
    @WithMockUser(roles = {"ADMIN"})
    void testUpdatePartName() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course and part
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                String uniqueName = "testPart_" + UUID.randomUUID().toString().substring(0, 8);
                Part part = new Part();
                part.setName(uniqueName);
                part.setCourse(savedCourse);
                partService.savePart(part);

                String newName = "Updated Part Name";

                // Test the endpoint
                mockMvc.perform(patch("/api/parts/{id}", part.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newName))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value(newName));

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Test Case ID: UT_CM_03_01_01
     * Purpose: Test creating part by course
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test course exists in database
     * 
     * Test Steps:
     * 1. Create a test course
     * 2. Call POST /api/courses/{courseId}/parts endpoint with new part
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - Part is created and associated with the course
     */
    @Test
    @DisplayName("Test create part by course")
    void testCreatePartByCourse() throws Exception {
        transactionTemplate.execute(status -> {
            try {
                // Create a test course
                Course course = createTestCourse();
                Course savedCourse = courseRepository.save(course);

                // Create a new part
                String uniqueName = "testPart_" + UUID.randomUUID().toString().substring(0, 8);
                Part part = new Part();
                part.setName(uniqueName);

                // Test the endpoint
                mockMvc.perform(post("/api/courses/{courseId}/parts", savedCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(part)))
                        .andExpect(status().isOk());

                // Verify the part was created
                List<Part> parts = partService.getPartListByCourse(savedCourse);
                assertTrue(parts.stream().anyMatch(p -> p.getName().equals(uniqueName)));

                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Helper method to create test course
    private Course createTestCourse() {
        Course course = new Course();
        course.setCourseCode("TEST_" + UUID.randomUUID().toString().substring(0, 4));
        course.setName("Test Course " + UUID.randomUUID().toString().substring(0, 8));
        course.setImgUrl("test.jpg");
        return course;
    }
} 