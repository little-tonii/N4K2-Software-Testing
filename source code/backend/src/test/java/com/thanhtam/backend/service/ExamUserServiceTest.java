package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.repository.PartRepository;
import com.thanhtam.backend.repository.UserRepository;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ExamUserServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ExamUserServiceTest.class);

    @Autowired
    private ExamUserService examUserService;

    @Autowired
    private ExamUserRepository examUserRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PartRepository partRepository;

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
                    // Clean up test exam users
                    List<ExamUser> testExamUsers = examUserRepository.findAll().stream()
                        .filter(eu -> eu.getUser() != null && eu.getUser().getUsername().startsWith("testUser"))
                        .collect(Collectors.toList());
                    examUserRepository.deleteAll(testExamUsers);

                    // Clean up test exams
                    List<Exam> testExams = examRepository.findAll().stream()
                        .filter(exam -> exam.getTitle() != null && exam.getTitle().startsWith("Test Exam"))
                        .collect(Collectors.toList());
                    examRepository.deleteAll(testExams);

                    // Clean up test users
                    List<User> testUsers = userRepository.findAll().stream()
                        .filter(user -> user.getUsername() != null && user.getUsername().startsWith("testUser"))
                        .collect(Collectors.toList());
                    userRepository.deleteAll(testUsers);

                    // Clean up test courses
                    List<Course> testCourses = courseRepository.findAll().stream()
                        .filter(course -> course.getCourseCode() != null && course.getCourseCode().startsWith("TEST"))
                        .collect(Collectors.toList());
                    courseRepository.deleteAll(testCourses);

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
     * Test Case ID: UT_EM_14
     * Purpose: Test creating exam user assignments
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test exam and users exist
     * 
     * Test Steps:
     * 1. Create test exam and users
     * 2. Create exam user assignments
     * 
     * Expected Results:
     * - Exam user assignments are created successfully
     * - All users are assigned to the exam
     */
    @Test
    @DisplayName("Test create exam user assignments")
    void testCreate() {
        transactionTemplate.execute(status -> {
            // Create test exam
            final Exam exam = createTestExam();
            final Exam savedExam = examRepository.save(exam);

            // Create test users
            List<User> users = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                User user = createTestUser("testUser_" + UUID.randomUUID().toString().substring(0, 8));
                users.add(userRepository.save(user));
            }

            // Create exam user assignments
            examUserService.create(savedExam, users);

            // Verify assignments
            List<ExamUser> examUsers = examUserRepository.findAllByExam_Id(savedExam.getId());
            assertEquals(users.size(), examUsers.size());
            assertTrue(examUsers.stream().allMatch(eu -> 
                users.stream().anyMatch(u -> u.getId().equals(eu.getUser().getId()))));
            assertTrue(examUsers.stream().allMatch(eu -> 
                eu.getRemainingTime() == savedExam.getDurationExam() * 60));
            assertTrue(examUsers.stream().allMatch(eu -> 
                eu.getTotalPoint() == -1.0));

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_15
     * Purpose: Test getting exam list by username
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test exam and user exist
     * 
     * Test Steps:
     * 1. Create test exam and user
     * 2. Create exam user assignment
     * 3. Get exam list by username
     * 
     * Expected Results:
     * - Exam list is retrieved successfully
     * - Only non-canceled exams are returned
     */
    @Test
    @DisplayName("Test get exam list by username")
    void testGetExamListByUsername() {
        transactionTemplate.execute(status -> {
            // Create test exam
            final Exam exam = createTestExam();
            final Exam savedExam = examRepository.save(exam);

            // Create test user
            User user = createTestUser("testUser_" + UUID.randomUUID().toString().substring(0, 8));
            user = userRepository.save(user);

            // Create exam user assignment
            List<User> users = new ArrayList<>();
            users.add(user);
            examUserService.create(savedExam, users);

            // Get exam list by username
            List<ExamUser> examUsers = examUserService.getExamListByUsername(user.getUsername());

            // Verify results
            assertNotNull(examUsers);
            assertFalse(examUsers.isEmpty());
            assertEquals(1, examUsers.size());
            assertEquals(savedExam.getId(), examUsers.get(0).getExam().getId());
            assertEquals(user.getId(), examUsers.get(0).getUser().getId());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_16
     * Purpose: Test finding exam user by exam ID and username
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test exam and user exist
     * 
     * Test Steps:
     * 1. Create test exam and user
     * 2. Create exam user assignment
     * 3. Find exam user by exam ID and username
     * 
     * Expected Results:
     * - Exam user is found successfully
     */
    @Test
    @DisplayName("Test find exam user by exam ID and username")
    void testFindByExamAndUser() {
        transactionTemplate.execute(status -> {
            // Create test exam
            Exam exam = createTestExam();
            exam = examRepository.save(exam);

            // Create test user
            User user = createTestUser("testUser_" + UUID.randomUUID().toString().substring(0, 8));
            user = userRepository.save(user);

            // Create exam user assignment
            List<User> users = new ArrayList<>();
            users.add(user);
            examUserService.create(exam, users);

            // Find exam user
            ExamUser examUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());

            // Verify results
            assertNotNull(examUser);
            assertEquals(exam.getId(), examUser.getExam().getId());
            assertEquals(user.getId(), examUser.getUser().getId());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_17
     * Purpose: Test updating exam user
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test exam and user exist
     * 
     * Test Steps:
     * 1. Create test exam and user
     * 2. Create exam user assignment
     * 3. Update exam user
     * 
     * Expected Results:
     * - Exam user is updated successfully
     */
    @Test
    @DisplayName("Test update exam user")
    void testUpdate() {
        transactionTemplate.execute(status -> {
            // Create test exam
            Exam exam = createTestExam();
            exam = examRepository.save(exam);

            // Create test user
            User user = createTestUser("testUser_" + UUID.randomUUID().toString().substring(0, 8));
            user = userRepository.save(user);

            // Create exam user assignment
            List<User> users = new ArrayList<>();
            users.add(user);
            examUserService.create(exam, users);

            // Get exam user
            ExamUser examUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());

            // Update exam user
            examUser.setTotalPoint(85.5);
            examUser.setIsFinished(true);
            examUserService.update(examUser);

            // Verify update
            ExamUser updatedExamUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());
            assertEquals(85.5, updatedExamUser.getTotalPoint());
            assertTrue(updatedExamUser.getIsFinished());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_18
     * Purpose: Test finding exam user by ID
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test exam and user exist
     * 
     * Test Steps:
     * 1. Create test exam and user
     * 2. Create exam user assignment
     * 3. Find exam user by ID
     * 
     * Expected Results:
     * - Exam user is found successfully
     */
    @Test
    @DisplayName("Test find exam user by ID")
    void testFindExamUserById() {
        transactionTemplate.execute(status -> {
            // Create test exam
            Exam exam = createTestExam();
            exam = examRepository.save(exam);

            // Create test user
            User user = createTestUser("testUser_" + UUID.randomUUID().toString().substring(0, 8));
            user = userRepository.save(user);

            // Create exam user assignment
            List<User> users = new ArrayList<>();
            users.add(user);
            examUserService.create(exam, users);

            // Get exam user
            ExamUser examUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());

            // Find by ID
            Optional<ExamUser> foundExamUser = examUserService.findExamUserById(examUser.getId());

            // Verify results
            assertTrue(foundExamUser.isPresent());
            assertEquals(examUser.getId(), foundExamUser.get().getId());
            assertEquals(exam.getId(), foundExamUser.get().getExam().getId());
            assertEquals(user.getId(), foundExamUser.get().getUser().getId());

            // Test with non-existent ID
            Optional<ExamUser> notFoundExamUser = examUserService.findExamUserById(99999L);
            assertFalse(notFoundExamUser.isPresent());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_19
     * Purpose: Test getting complete exams by course ID and username
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test exam, course, and user exist
     * 
     * Test Steps:
     * 1. Create test exam, course, and user
     * 2. Create exam user assignment
     * 3. Update exam user as completed
     * 4. Get complete exams
     * 
     * Expected Results:
     * - Complete exams are retrieved successfully
     */
    @Test
    @DisplayName("Test get complete exams by course ID and username")
    void testGetCompleteExams() {
        transactionTemplate.execute(status -> {
            // Create test course
            Course course = createTestCourse();
            course = courseRepository.save(course);

            // Create test exam
            Exam exam = createTestExam();
            exam.getPart().setCourse(course);
            exam = examRepository.save(exam);

            // Create test user
            User user = createTestUser("testUser_" + UUID.randomUUID().toString().substring(0, 8));
            user = userRepository.save(user);

            // Create exam user assignment
            List<User> users = new ArrayList<>();
            users.add(user);
            examUserService.create(exam, users);

            // Update exam user as completed
            ExamUser examUser = examUserService.findByExamAndUser(exam.getId(), user.getUsername());
            examUser.setTotalPoint(85.5);
            examUser.setIsFinished(true);
            examUserService.update(examUser);

            // Get complete exams
            List<ExamUser> completeExams = examUserService.getCompleteExams(course.getId(), user.getUsername());

            // Verify results
            assertNotNull(completeExams);
            assertFalse(completeExams.isEmpty());
            assertEquals(1, completeExams.size());
            assertEquals(exam.getId(), completeExams.get(0).getExam().getId());
            assertEquals(user.getId(), completeExams.get(0).getUser().getId());
            assertEquals(85.5, completeExams.get(0).getTotalPoint());
            assertTrue(completeExams.get(0).getIsFinished());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_20
     * Purpose: Test finding all exam users by exam ID
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test exam and users exist
     * 
     * Test Steps:
     * 1. Create test exam and users
     * 2. Create exam user assignments
     * 3. Find all exam users by exam ID
     * 
     * Expected Results:
     * - All exam users are found successfully
     */
    @Test
    @DisplayName("Test find all exam users by exam ID")
    void testFindAllByExamId() {
        transactionTemplate.execute(status -> {
            // Create test exam
            Exam exam = createTestExam();
            exam = examRepository.save(exam);

            // Create test users
            List<User> users = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                User user = createTestUser("testUser_" + UUID.randomUUID().toString().substring(0, 8));
                users.add(userRepository.save(user));
            }

            // Create exam user assignments
            examUserService.create(exam, users);

            // Find all exam users
            List<ExamUser> examUsers = examUserService.findAllByExam_Id(exam.getId());

            // Verify results
            assertNotNull(examUsers);
            assertEquals(users.size(), examUsers.size());
            assertTrue(examUsers.stream().allMatch(eu -> 
                users.stream().anyMatch(u -> u.getId().equals(eu.getUser().getId()))));

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_21
     * Purpose: Test finding finished exam users by exam ID
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test exam and users exist
     * 
     * Test Steps:
     * 1. Create test exam and users
     * 2. Create exam user assignments
     * 3. Mark some exam users as finished
     * 4. Find finished exam users
     * 
     * Expected Results:
     * - Only finished exam users are found
     */
    @Test
    @DisplayName("Test find finished exam users by exam ID")
    void testFindFinishedExamUsersByExamId() {
        transactionTemplate.execute(status -> {
            // Create test exam
            Exam exam = createTestExam();
            exam = examRepository.save(exam);

            // Create test users
            List<User> users = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                User user = createTestUser("testUser_" + UUID.randomUUID().toString().substring(0, 8));
                users.add(userRepository.save(user));
            }

            // Create exam user assignments
            examUserService.create(exam, users);

            // Mark some exam users as finished
            for (int i = 0; i < 2; i++) {
                ExamUser examUser = examUserService.findByExamAndUser(exam.getId(), users.get(i).getUsername());
                examUser.setIsFinished(true);
                examUser.setTotalPoint(85.5);
                examUserService.update(examUser);
            }

            // Find finished exam users
            List<ExamUser> finishedExamUsers = examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(exam.getId());

            // Verify results
            assertNotNull(finishedExamUsers);
            assertEquals(2, finishedExamUsers.size());
            assertTrue(finishedExamUsers.stream().allMatch(ExamUser::getIsFinished));
            assertTrue(finishedExamUsers.stream().allMatch(eu -> eu.getTotalPoint() == 85.5));

            return null;
        });
    }

    // Helper methods
    private Exam createTestExam() {
        // Create test intake
        Intake intake = new Intake();
        intake.setName("Test Intake_" + UUID.randomUUID().toString().substring(0, 8));
        intake.setIntakeCode("TEST_" + UUID.randomUUID().toString().substring(0, 4));
        intake = intakeRepository.save(intake);

        // Create test course
        Course course = createTestCourse();
        course = courseRepository.save(course);

        // Create test part
        Part part = new Part();
        part.setName("Test Part_" + UUID.randomUUID().toString().substring(0, 8));
        part.setCourse(course);
        part = partRepository.save(part);

        // Create exam
        Exam exam = new Exam();
        exam.setTitle("Test Exam_" + UUID.randomUUID().toString().substring(0, 8));
        exam.setIntake(intake);
        exam.setPart(part);
        exam.setShuffle(true);
        exam.setDurationExam(60);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setQuestionData("[]");
        exam.setCanceled(false);

        return exam;
    }

    private Course createTestCourse() {
        Course course = new Course();
        course.setCourseCode("TEST_" + UUID.randomUUID().toString().substring(0, 4));
        course.setName("Test Course");
        course.setImgUrl("test.jpg");
        return course;
    }

    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password123");
        return user;
    }
} 