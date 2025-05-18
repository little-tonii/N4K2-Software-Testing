package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.PartRepository;
import com.thanhtam.backend.repository.QuestionTypeRepository;
import org.joda.time.DateTime;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class StatisticsServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceTest.class);

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamUserRepository examUserRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

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
                    // Clean up ALL test data
                    examUserRepository.deleteAll();
                    examRepository.deleteAll();
                    questionRepository.deleteAll();
                    userRepository.deleteAll();
                    partRepository.deleteAll();
                    courseRepository.deleteAll();
                    intakeRepository.deleteAll();
                    questionTypeRepository.deleteAll();
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
     * Test Case ID: UT_SM_01
     * Purpose: Test counting total exams
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Create multiple test exams
     * 2. Count total exams
     * 
     * Expected Results:
     * - Total exam count matches number of created exams
     */
    @Test
    @DisplayName("Test count total exams")
    void testCountExamTotal() {
        transactionTemplate.execute(status -> {
            // Create test exams
            int numExams = 3;
            for (int i = 0; i < numExams; i++) {
                Exam exam = createTestExam();
                examRepository.save(exam);
            }

            // Count total exams
            long totalExams = statisticsService.countExamTotal();

            // Verify results
            assertEquals(numExams, totalExams);

            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_02
     * Purpose: Test counting total questions
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Create multiple test questions
     * 2. Count total questions
     * 
     * Expected Results:
     * - Total question count matches number of created questions
     */
    @Test
    @DisplayName("Test count total questions")
    void testCountQuestionTotal() {
        transactionTemplate.execute(status -> {
            // Create test questions
            int numQuestions = 3;
            for (int i = 0; i < numQuestions; i++) {
                Question question = createTestQuestion();
                questionRepository.save(question);
            }

            // Count total questions
            long totalQuestions = statisticsService.countQuestionTotal();

            // Verify results
            assertEquals(numQuestions, totalQuestions);

            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_03
     * Purpose: Test counting total accounts
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Create multiple test users
     * 2. Count total accounts
     * 
     * Expected Results:
     * - Total account count matches number of created users
     */
    @Test
    @DisplayName("Test count total accounts")
    void testCountAccountTotal() {
        transactionTemplate.execute(status -> {
            // Create test users
            int numUsers = 3;
            for (int i = 0; i < numUsers; i++) {
                User user = createTestUser();
                userRepository.save(user);
            }

            // Count total accounts
            long totalAccounts = statisticsService.countAccountTotal();

            // Verify results
            assertEquals(numUsers, totalAccounts);

            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_04
     * Purpose: Test counting total exam users
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Create multiple test exam users
     * 2. Count total exam users
     * 
     * Expected Results:
     * - Total exam user count matches number of created exam users
     */
    @Test
    @DisplayName("Test count total exam users")
    void testCountExamUserTotal() {
        transactionTemplate.execute(status -> {
            // Create test exam users
            int numExamUsers = 3;
            for (int i = 0; i < numExamUsers; i++) {
                ExamUser examUser = createTestExamUser();
                // Ensure all relationships are set and saved
                examUser = examUserRepository.save(examUser);
            }

            // Count total exam users
            long totalExamUsers = statisticsService.countExamUserTotal();

            // Verify results
            assertEquals(numExamUsers, totalExamUsers);

            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_05
     * Purpose: Test getting exam user change percentage
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Create exam users for current week
     * 2. Create exam users for last week
     * 3. Get change percentage
     * 
     * Expected Results:
     * - Change percentage is calculated correctly
     */
    @Test
    @DisplayName("Test get exam user change percentage")
    void testGetChangeExamUser() {
        transactionTemplate.execute(status -> {
            // Create exam users for current week
            int currentWeekCount = 5;
            for (int i = 0; i < currentWeekCount; i++) {
                ExamUser examUser = createTestExamUser();
                examUser.setTimeFinish(new Date());
                examUser = examUserRepository.save(examUser);
            }

            // Create exam users for last week
            int lastWeekCount = 3;
            for (int i = 0; i < lastWeekCount; i++) {
                ExamUser examUser = createTestExamUser();
                examUser.setTimeFinish(new DateTime().minusWeeks(1).toDate());
                examUser = examUserRepository.save(examUser);
            }

            // Get change percentage
            Double changePercentage = statisticsService.getChangeExamUser();

            // Verify results with more lenient delta
            assertNotNull(changePercentage);
            assertEquals(66.67, changePercentage, 0.1); // Allow 0.1 difference

            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_06
     * Purpose: Test counting exam users for last seven days
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Create exam users for each of the last seven days
     * 2. Get count for last seven days
     * 
     * Expected Results:
     * - Counts match number of exam users per day
     */
    @Test
    @DisplayName("Test count exam users for last seven days")
    void testCountExamUserLastedSevenDaysTotal() {
        transactionTemplate.execute(status -> {
            // Create exam users for each of the last seven days
            int[] usersPerDay = {2, 3, 1, 4, 2, 3, 1}; // Total: 16 users
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < usersPerDay[i]; j++) {
                    ExamUser examUser = createTestExamUser();
                    examUser.setTimeFinish(new DateTime().minusDays(i).toDate());
                    examUser = examUserRepository.save(examUser);
                }
            }

            // Get counts for last seven days
            List<Long> counts = statisticsService.countExamUserLastedSevenDaysTotal();

            // Verify results
            assertNotNull(counts);
            assertEquals(16, counts.size());
            for (int i = 0; i < 7; i++) {
                assertEquals(usersPerDay[i], counts.get(i));
            }

            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_07
     * Purpose: Test getting question change percentage
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Create questions for current week
     * 2. Create questions for last week
     * 3. Get change percentage
     * 
     * Expected Results:
     * - Change percentage is calculated correctly
     */
    @Test
    @DisplayName("Test get question change percentage")
    void testGetChangeQuestion() {
        transactionTemplate.execute(status -> {
            // Create questions for current week
            int currentWeekCount = 4;
            for (int i = 0; i < currentWeekCount; i++) {
                Question question = createTestQuestion();
                question.setCreatedDate(new Date());
                question = questionRepository.save(question);
            }

            // Create questions for last week
            int lastWeekCount = 2;
            for (int i = 0; i < lastWeekCount; i++) {
                Question question = createTestQuestion();
                question.setCreatedDate(new DateTime().minusWeeks(1).toDate());
                question = questionRepository.save(question);
            }

            // Get change percentage
            Double changePercentage = statisticsService.getChangeQuestion();

            // Verify results with more lenient delta
            assertNotNull(changePercentage);
            assertEquals(100.0, changePercentage, 0.1); // Allow 0.1 difference

            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_08
     * Purpose: Test getting account change percentage
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Create users for current week
     * 2. Create users for last week
     * 3. Get change percentage
     * 
     * Expected Results:
     * - Change percentage is calculated correctly
     */
    @Test
    @DisplayName("Test get account change percentage")
    void testGetChangeAccount() {
        transactionTemplate.execute(status -> {
            // Create users for current week
            int currentWeekCount = 6;
            for (int i = 0; i < currentWeekCount; i++) {
                User user = createTestUser();
                user.setCreatedDate(new Date());
                userRepository.save(user);
            }

            // Create users for last week
            int lastWeekCount = 3;
            for (int i = 0; i < lastWeekCount; i++) {
                User user = createTestUser();
                user.setCreatedDate(new DateTime().minusWeeks(1).toDate());
                userRepository.save(user);
            }

            // Get change percentage
            Double changePercentage = statisticsService.getChangeAccount();

            // Verify results with more lenient delta
            assertNotNull(changePercentage);
            assertEquals(100.0, changePercentage, 0.1); // Allow 0.1 difference

            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_09
     * Purpose: Test getting exam change percentage
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Create exams for current week
     * 2. Create exams for last week
     * 3. Get change percentage
     * 
     * Expected Results:
     * - Change percentage is calculated correctly
     */
    @Test
    @DisplayName("Test get exam change percentage")
    void testGetChangeExam() {
        transactionTemplate.execute(status -> {
            // Create exams for current week
            int currentWeekCount = 3;
            for (int i = 0; i < currentWeekCount; i++) {
                Exam exam = createTestExam();
                exam.setCreatedDate(new Date());
                exam.setCanceled(true);
                exam = examRepository.save(exam);
            }

            // Create exams for last week
            int lastWeekCount = 1;
            for (int i = 0; i < lastWeekCount; i++) {
                Exam exam = createTestExam();
                exam.setCreatedDate(new DateTime().minusWeeks(1).toDate());
                exam.setCanceled(true);
                exam = examRepository.save(exam);
            }

            // Get change percentage
            Double changePercentage = statisticsService.getChangeExam();

            // Verify results with more lenient delta
            assertNotNull(changePercentage);
            assertEquals(200.0, changePercentage, 0.1); // Allow 0.1 difference

            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_10
     * Purpose: Test getChangeExamUser with both weeks zero
     */
    @Test
    @DisplayName("Test getChangeExamUser with both weeks zero")
    void testGetChangeExamUserBothZero() {
        transactionTemplate.execute(status -> {
            // No exam users at all
            Double change = statisticsService.getChangeExamUser();
            assertEquals(0.0, change);
            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_11
     * Purpose: Test getChangeExamUser with only last week data
     */
    @Test
    @DisplayName("Test getChangeExamUser with only last week data")
    void testGetChangeExamUserOnlyLastWeek() {
        transactionTemplate.execute(status -> {
            // Only last week
            ExamUser examUser = createTestExamUser();
            examUser.setTimeFinish(new DateTime().minusWeeks(1).toDate());
            examUserRepository.save(examUser);
            Double change = statisticsService.getChangeExamUser();
            assertEquals(-100.0, change);
            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_12
     * Purpose: Test getChangeExamUser with only current week data
     */
    @Test
    @DisplayName("Test getChangeExamUser with only current week data")
    void testGetChangeExamUserOnlyCurrentWeek() {
        transactionTemplate.execute(status -> {
            // Only current week
            ExamUser examUser = createTestExamUser();
            examUser.setTimeFinish(new Date());
            examUserRepository.save(examUser);
            Double change = statisticsService.getChangeExamUser();
            assertEquals(100.0, change);
            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_13
     * Purpose: Test getChangeQuestion with both weeks zero
     */
    @Test
    @DisplayName("Test getChangeQuestion with both weeks zero")
    void testGetChangeQuestionBothZero() {
        transactionTemplate.execute(status -> {
            Double change = statisticsService.getChangeQuestion();
            assertEquals(0.0, change);
            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_14
     * Purpose: Test getChangeAccount with both weeks zero
     */
    @Test
    @DisplayName("Test getChangeAccount with both weeks zero")
    void testGetChangeAccountBothZero() {
        transactionTemplate.execute(status -> {
            Double change = statisticsService.getChangeAccount();
            assertEquals(0.0, change);
            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_15
     * Purpose: Test getChangeExam with both weeks zero
     */
    @Test
    @DisplayName("Test getChangeExam with both weeks zero")
    void testGetChangeExamBothZero() {
        transactionTemplate.execute(status -> {
            Double change = statisticsService.getChangeExam();
            assertEquals(0.0, change);
            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_16
     * Purpose: Test countExamUserLastedSevenDaysTotal with no data
     */
    @Test
    @DisplayName("Test countExamUserLastedSevenDaysTotal with no data")
    void testCountExamUserLastedSevenDaysTotalEmpty() {
        transactionTemplate.execute(status -> {
            List<Long> counts = statisticsService.countExamUserLastedSevenDaysTotal();
            assertNotNull(counts);
            // Should be empty or all zeros depending on implementation
            // Accept either empty or all zeros
            assertTrue(counts.isEmpty() || counts.stream().allMatch(c -> c == 0));
            return null;
        });
    }

    /**
     * Test Case ID: UT_SM_17
     * Purpose: Test static date methods for null arguments
     */
    @Test
    @DisplayName("Test isSameDay throws on null")
    void testIsSameDayNull() {
        assertThrows(IllegalArgumentException.class, () -> StatisticsServiceImpl.isSameDay(null, new DateTime()));
        assertThrows(IllegalArgumentException.class, () -> StatisticsServiceImpl.isSameDay(new DateTime(), null));
    }
    @Test
    @DisplayName("Test isSameWeek throws on null")
    void testIsSameWeekNull() {
        assertThrows(IllegalArgumentException.class, () -> StatisticsServiceImpl.isSameWeek(null, new DateTime()));
        assertThrows(IllegalArgumentException.class, () -> StatisticsServiceImpl.isSameWeek(new DateTime(), null));
    }
    @Test
    @DisplayName("Test isLastWeek throws on null")
    void testIsLastWeekNull() {
        assertThrows(IllegalArgumentException.class, () -> StatisticsServiceImpl.isLastWeek(null, new DateTime()));
        assertThrows(IllegalArgumentException.class, () -> StatisticsServiceImpl.isLastWeek(new DateTime(), null));
    }

    // Helper methods
    private Exam createTestExam() {
        // Create and save Intake
        Intake intake = new Intake();
        intake.setName("Test Intake_" + UUID.randomUUID().toString().substring(0, 8));
        intake.setIntakeCode("TEST_" + UUID.randomUUID().toString().substring(0, 4));
        intake = intakeRepository.save(intake);

        // Create and save Course
        Course course = new Course();
        course.setCourseCode("TEST_" + UUID.randomUUID().toString().substring(0, 4));
        course.setName("Test Course");
        course.setImgUrl("test.jpg");
        course = courseRepository.save(course);

        // Create and save Part
        Part part = new Part();
        part.setName("Test Part_" + UUID.randomUUID().toString().substring(0, 8));
        part.setCourse(course);
        part = partRepository.save(part);

        // Now create Exam
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

    private Question createTestQuestion() {
        // Create and save Course
        Course course = new Course();
        course.setCourseCode("TEST_" + UUID.randomUUID().toString().substring(0, 4));
        course.setName("Test Course");
        course.setImgUrl("test.jpg");
        course = courseRepository.save(course);

        // Create and save Part
        Part part = new Part();
        part.setName("Test Part_" + UUID.randomUUID().toString().substring(0, 8));
        part.setCourse(course);
        part = partRepository.save(part);

        // Create and save QuestionType
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(null); // Set a valid EQTypeCode if required
        questionType.setDescription("Test Type");
        questionType = questionTypeRepository.save(questionType);

        Question question = new Question();
        question.setQuestionText("Test Question_" + UUID.randomUUID().toString().substring(0, 8));
        question.setCreatedDate(new Date());
        question.setPart(part);
        question.setQuestionType(questionType);
        // Set other required fields/relations here if needed
        return question;
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("testUser_" + UUID.randomUUID().toString().substring(0, 8));
        user.setEmail(user.getUsername() + "@example.com");
        user.setPassword("password123");
        user.setCreatedDate(new Date());
        user.setDeleted(false);
        return user;
    }

    private ExamUser createTestExamUser() {
        // Create and save Exam
        Exam exam = createTestExam();
        exam = examRepository.save(exam);

        // Create and save User
        User user = createTestUser();
        user = userRepository.save(user);

        ExamUser examUser = new ExamUser();
        examUser.setExam(exam);
        examUser.setUser(user);
        examUser.setTimeFinish(new Date());
        examUser.setTotalPoint(85.5);
        examUser.setIsFinished(true);
        return examUser;
    }
} 