package com.thanhtam.backend.service;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ChoiceList;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.Choice;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.repository.PartRepository;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.repository.QuestionTypeRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.ultilities.DifficultyLevel;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class ExamServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ExamServiceTest.class);

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    @MockBean
    private ChoiceService choiceService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    private CourseRepository courseRepository;

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
                    // Find all test exams
                    List<Exam> testExams = examRepository.findAll().stream()
                        .filter(exam -> exam.getTitle() != null && exam.getTitle().startsWith("Test Exam"))
                        .collect(Collectors.toList());

                    // Delete all test exams
                    for (Exam exam : testExams) {
                        examRepository.delete(exam);
                        logger.info("Successfully deleted test exam: {}", exam.getTitle());
                    }

                    // Clean up test intakes
                    List<Intake> testIntakes = intakeRepository.findAll().stream()
                        .filter(intake -> intake.getName() != null && intake.getName().startsWith("Test Intake"))
                        .collect(Collectors.toList());
                    for (Intake intake : testIntakes) {
                        intakeRepository.delete(intake);
                        logger.info("Successfully deleted test intake: {}", intake.getName());
                    }

                    // Clean up test parts
                    List<Part> testParts = partRepository.findAll().stream()
                        .filter(part -> part.getName() != null && part.getName().startsWith("Test Part"))
                        .collect(Collectors.toList());
                    for (Part part : testParts) {
                        partRepository.delete(part);
                        logger.info("Successfully deleted test part: {}", part.getName());
                    }

                    // Clean up test users
                    List<User> testUsers = userRepository.findAll().stream()
                        .filter(user -> user.getUsername() != null && user.getUsername().startsWith("testUser"))
                        .collect(Collectors.toList());
                    for (User user : testUsers) {
                        userRepository.delete(user);
                        logger.info("Successfully deleted test user: {}", user.getUsername());
                    }

                    // Clean up test questions
                    List<Question> testQuestions = questionRepository.findAll().stream()
                        .filter(question -> question.getQuestionText() != null && question.getQuestionText().startsWith("Test Question"))
                        .collect(Collectors.toList());
                    for (Question question : testQuestions) {
                        questionRepository.delete(question);
                        logger.info("Successfully deleted test question: {}", question.getQuestionText());
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
     * Test Case ID: UT_EM_01
     * Purpose: Test exam creation with valid data
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test intake and part exist
     * 
     * Test Steps:
     * 1. Create a new exam with valid data
     * 2. Save the exam using examService
     * 3. Verify the exam was saved correctly
     * 
     * Expected Results:
     * - Exam is created successfully
     * - All exam data is saved correctly
     * - Exam can be retrieved from database
     */
    @Test
    @DisplayName("Test create exam with valid data")
    void testCreateExamWithValidData() {
        transactionTemplate.execute(status -> {
            // Create test intake and part
            Intake intake = createTestIntake();
            Part part = createTestPart();
            
            // Create a new exam
            String uniqueTitle = "Test Exam_" + UUID.randomUUID().toString().substring(0, 8);
            Exam exam = new Exam();
            exam.setTitle(uniqueTitle);
            exam.setIntake(intake);
            exam.setPart(part);
            exam.setShuffle(true);
            exam.setDurationExam(60);
            exam.setBeginExam(new Date());
            exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000)); // 1 hour later
            exam.setQuestionData("[]");

            // Save the exam
            Exam savedExam = examService.saveExam(exam);

            // Verify the exam was saved correctly
            assertNotNull(savedExam);
            assertNotNull(savedExam.getId());
            assertEquals(uniqueTitle, savedExam.getTitle());
            assertEquals(intake.getId(), savedExam.getIntake().getId());
            assertEquals(part.getId(), savedExam.getPart().getId());
            assertTrue(savedExam.isShuffle());
            assertEquals(60, savedExam.getDurationExam());
            assertNotNull(savedExam.getBeginExam());
            assertNotNull(savedExam.getFinishExam());
            assertEquals("[]", savedExam.getQuestionData());

            // Verify we can retrieve the exam from the database
            Optional<Exam> retrievedExam = examService.getExamById(savedExam.getId());
            assertTrue(retrievedExam.isPresent());
            assertEquals(savedExam.getId(), retrievedExam.get().getId());
            assertEquals(savedExam.getTitle(), retrievedExam.get().getTitle());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_02
     * Purpose: Test exam retrieval by ID
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test exam exists in database
     * 
     * Test Steps:
     * 1. Create a test exam
     * 2. Retrieve exam by ID
     * 
     * Expected Results:
     * - Exam is retrieved successfully
     * - Retrieved exam data matches created exam
     */
    @Test
    @DisplayName("Test get exam by ID")
    void testGetExamById() {
        transactionTemplate.execute(status -> {
            // Create test exam
            Exam testExam = createTestExam();
            Exam savedExam = examService.saveExam(testExam);

            // Get exam by ID
            Optional<Exam> retrievedExam = examService.getExamById(savedExam.getId());

            // Verify exam was retrieved correctly
            assertTrue(retrievedExam.isPresent());
            assertEquals(savedExam.getId(), retrievedExam.get().getId());
            assertEquals(savedExam.getTitle(), retrievedExam.get().getTitle());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_03
     * Purpose: Test exam cancellation
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test exam exists in database
     * 
     * Test Steps:
     * 1. Create a test exam
     * 2. Cancel the exam
     * 3. Verify exam is marked as canceled
     * 
     * Expected Results:
     * - Exam is marked as canceled
     */
    @Test
    @DisplayName("Test cancel exam")
    void testCancelExam() {
        transactionTemplate.execute(status -> {
            // Create test exam
            Exam testExam = createTestExam();
            Exam savedExam = examService.saveExam(testExam);

            // Cancel the exam
            examService.cancelExam(savedExam.getId());

            // Verify exam is canceled
            Optional<Exam> canceledExam = examService.getExamById(savedExam.getId());
            assertTrue(canceledExam.isPresent());
            assertTrue(canceledExam.get().isCanceled());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_04
     * Purpose: Test finding exams by username
     * 
     * Prerequisites:
     * - Database is accessible
     * - Multiple test exams exist in database
     * 
     * Test Steps:
     * 1. Create multiple test exams
     * 2. Retrieve exams by username
     * 
     * Expected Results:
     * - Exams are retrieved correctly based on username
     */
    @Test
    @DisplayName("Test find exams by username")
    void testFindExamsByUsername() {
        transactionTemplate.execute(status -> {
            // Create test user
            String username = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User user = createTestUser(username);
            userRepository.save(user);

            // Create multiple test exams
            for (int i = 0; i < 3; i++) {
                Exam exam = createTestExam();
                exam.setCreatedBy(user);
                examService.saveExam(exam);
            }

            // Test pagination
            Pageable pageable = PageRequest.of(0, 10);
            Page<Exam> examPage = examService.findAllByCreatedBy_Username(pageable, username);

            assertNotNull(examPage);
            assertEquals(3, examPage.getTotalElements());
            assertTrue(examPage.getContent().stream()
                    .allMatch(exam -> exam.getCreatedBy().getUsername().equals(username)));

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_05
     * Purpose: Test getting choice list from answer sheet
     * 
     * Prerequisites:
     * - Database is accessible
     * - Question type exists in database
     * 
     * Test Steps:
     * 1. Create test question type and question
     * 2. Create test answer sheets and exam question points
     * 3. Get choice list
     * 
     * Expected Results:
     * - Choice list is generated correctly
     */
    @Test
    @DisplayName("Test get choice list")
    void testGetChoiceList() {
        transactionTemplate.execute(status -> {
            // Create test question type
            QuestionType questionType = new QuestionType();
            questionType.setTypeCode(EQTypeCode.TF);
            questionType.setDescription("True/False");
            questionType = questionTypeRepository.save(questionType);

            // Create test question
            Question question = new Question();
            question.setQuestionText("Test Question");
            question.setDifficultyLevel(DifficultyLevel.EASY);
            question.setPoint(1);
            question.setQuestionType(questionType);
            question = questionRepository.save(question);

            // Create test answer sheets
            List<AnswerSheet> answerSheets = new ArrayList<>();
            AnswerSheet answerSheet = new AnswerSheet();
            answerSheet.setQuestionId(question.getId());
            List<Choice> choices = new ArrayList<>();
            Choice choice = new Choice();
            choice.setId(1L);
            choice.setChoiceText("A");
            choices.add(choice);
            answerSheet.setChoices(choices);
            answerSheet.setPoint(1);
            answerSheets.add(answerSheet);

            // Create test exam question points
            List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
            ExamQuestionPoint point = new ExamQuestionPoint();
            point.setQuestionId(question.getId());
            point.setPoint(1);
            examQuestionPoints.add(point);

            // Get choice list
            List<ChoiceList> choiceLists = examService.getChoiceList(answerSheets, examQuestionPoints);

            assertNotNull(choiceLists);
            assertFalse(choiceLists.isEmpty());
            assertEquals(1, choiceLists.size());
            assertEquals(1, choiceLists.get(0).getPoint());
            assertNotNull(choiceLists.get(0).getQuestion());
            assertNotNull(choiceLists.get(0).getChoices());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_06
     * Purpose: Test getting all exams
     * 
     * Prerequisites:
     * - Database is accessible
     * - Multiple test exams exist in database
     * 
     * Test Steps:
     * 1. Create multiple test exams
     * 2. Get all exams
     * 
     * Expected Results:
     * - All exams are retrieved successfully
     */
    @Test
    @DisplayName("Test get all exams")
    void testGetAllExams() {
        transactionTemplate.execute(status -> {
            try {
                // Create test intake
                Intake intake = createTestIntake();
                
                // Create test course
                Course course = new Course();
                course.setCourseCode("TEST_" + UUID.randomUUID().toString().substring(0, 4));
                course.setName("Test Course");
                course.setImgUrl("test.jpg");
                List<Intake> intakes = new ArrayList<>();
                intakes.add(intake);
                course.setIntakes(intakes);
                course = courseRepository.save(course);
                
                // Create test part with course
                Part part = createTestPart();
                part.setCourse(course);
                part = partRepository.save(part);
                
                // Create multiple test exams
                for (int i = 0; i < 3; i++) {
                    Exam exam = createTestExam();
                    exam.setIntake(intake);
                    exam.setPart(part);
                    exam.setQuestionData("[]"); // Set empty question data
                    examService.saveExam(exam);
                }

                // Get all exams
                List<Exam> exams = examService.getAll();

                // Verify results
                assertNotNull(exams);
                assertTrue(exams.size() >= 3);
                assertTrue(exams.stream().allMatch(exam -> exam.getTitle().startsWith("Test Exam_")));

                return null;
            } catch (Exception e) {
                logger.error("Error in testGetAllExams: {}", e.getMessage());
                status.setRollbackOnly();
                throw e;
            }
        });
    }

    /**
     * Test Case ID: UT_EM_07
     * Purpose: Test getting choice list with True/False question type
     * 
     * Prerequisites:
     * - Database is accessible
     * - Question type exists in database
     * 
     * Test Steps:
     * 1. Create test question type and question
     * 2. Create test answer sheets and exam question points
     * 3. Get choice list
     * 
     * Expected Results:
     * - Choice list is generated correctly for True/False question
     */
    @Test
    @DisplayName("Test get choice list with True/False question")
    void testGetChoiceListWithTrueFalseQuestion() {
        transactionTemplate.execute(status -> {
            // Create test question type
            QuestionType questionType = new QuestionType();
            questionType.setTypeCode(EQTypeCode.TF);
            questionType.setDescription("True/False");
            questionType = questionTypeRepository.save(questionType);

            // Create test question
            Question question = new Question();
            question.setQuestionText("Test True/False Question");
            question.setDifficultyLevel(DifficultyLevel.EASY);
            question.setPoint(1);
            question.setQuestionType(questionType);
            question = questionRepository.save(question);

            // Create test answer sheets
            List<AnswerSheet> answerSheets = new ArrayList<>();
            AnswerSheet answerSheet = new AnswerSheet();
            answerSheet.setQuestionId(question.getId());
            List<Choice> choices = new ArrayList<>();
            Choice choice = new Choice();
            choice.setId(1L);
            choice.setChoiceText("True");
            choices.add(choice);
            answerSheet.setChoices(choices);
            answerSheet.setPoint(1);
            answerSheets.add(answerSheet);

            // Create test exam question points
            List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
            ExamQuestionPoint point = new ExamQuestionPoint();
            point.setQuestionId(question.getId());
            point.setPoint(1);
            examQuestionPoints.add(point);

            // Mock the choice service to return expected values
            when(choiceService.findChoiceTextById(1L)).thenReturn("True");

            // Get choice list
            List<ChoiceList> choiceLists = examService.getChoiceList(answerSheets, examQuestionPoints);

            // Verify results
            assertNotNull(choiceLists);
            assertFalse(choiceLists.isEmpty());
            assertEquals(1, choiceLists.size());
            assertEquals(1, choiceLists.get(0).getPoint());
            assertNotNull(choiceLists.get(0).getQuestion());
            assertNotNull(choiceLists.get(0).getChoices());
            assertEquals(1, choiceLists.get(0).getChoices().size());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_08
     * Purpose: Test getting choice list with Multiple Choice question type
     * 
     * Prerequisites:
     * - Database is accessible
     * - Question type exists in database
     * 
     * Test Steps:
     * 1. Create test question type and question
     * 2. Create test answer sheets and exam question points
     * 3. Get choice list
     * 
     * Expected Results:
     * - Choice list is generated correctly for Multiple Choice question
     */
    @Test
    @DisplayName("Test get choice list with Multiple Choice question")
    void testGetChoiceListWithMultipleChoiceQuestion() {
        transactionTemplate.execute(status -> {
            // Create test question type
            QuestionType questionType = new QuestionType();
            questionType.setTypeCode(EQTypeCode.MC);
            questionType.setDescription("Multiple Choice");
            questionType = questionTypeRepository.save(questionType);

            // Create test question
            Question question = new Question();
            question.setQuestionText("Test Multiple Choice Question");
            question.setDifficultyLevel(DifficultyLevel.EASY);
            question.setPoint(1);
            question.setQuestionType(questionType);
            question = questionRepository.save(question);

            // Create test answer sheets
            List<AnswerSheet> answerSheets = new ArrayList<>();
            AnswerSheet answerSheet = new AnswerSheet();
            answerSheet.setQuestionId(question.getId());
            List<Choice> choices = new ArrayList<>();
            Choice choice = new Choice();
            choice.setId(1L);
            choice.setChoiceText("A");
            choice.setIsCorrected(1);
            choices.add(choice);
            answerSheet.setChoices(choices);
            answerSheet.setPoint(1);
            answerSheets.add(answerSheet);

            // Create test exam question points
            List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
            ExamQuestionPoint point = new ExamQuestionPoint();
            point.setQuestionId(question.getId());
            point.setPoint(1);
            examQuestionPoints.add(point);

            // Mock the choice service to return expected values
            when(choiceService.findIsCorrectedById(1L)).thenReturn(1);
            when(choiceService.findChoiceTextById(1L)).thenReturn("A");

            // Get choice list
            List<ChoiceList> choiceLists = examService.getChoiceList(answerSheets, examQuestionPoints);

            // Verify results
            assertNotNull(choiceLists);
            assertFalse(choiceLists.isEmpty());
            assertEquals(1, choiceLists.size());
            assertEquals(1, choiceLists.get(0).getPoint());
            assertNotNull(choiceLists.get(0).getQuestion());
            assertNotNull(choiceLists.get(0).getChoices());
            assertEquals(1, choiceLists.get(0).getChoices().size());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_09
     * Purpose: Test getting choice list with Multiple Select question type
     * 
     * Prerequisites:
     * - Database is accessible
     * - Question type exists in database
     * 
     * Test Steps:
     * 1. Create test question type and question
     * 2. Create test answer sheets and exam question points
     * 3. Get choice list
     * 
     * Expected Results:
     * - Choice list is generated correctly for Multiple Select question
     */
    @Test
    @DisplayName("Test get choice list with Multiple Select question")
    void testGetChoiceListWithMultipleSelectQuestion() {
        transactionTemplate.execute(status -> {
            // Create test question type
            QuestionType questionType = new QuestionType();
            questionType.setTypeCode(EQTypeCode.MS);
            questionType.setDescription("Multiple Select");
            questionType = questionTypeRepository.save(questionType);

            // Create test question
            Question question = new Question();
            question.setQuestionText("Test Multiple Select Question");
            question.setDifficultyLevel(DifficultyLevel.EASY);
            question.setPoint(1);
            question.setQuestionType(questionType);
            question = questionRepository.save(question);

            // Create test answer sheets
            List<AnswerSheet> answerSheets = new ArrayList<>();
            AnswerSheet answerSheet = new AnswerSheet();
            answerSheet.setQuestionId(question.getId());
            List<Choice> choices = new ArrayList<>();
            Choice choice = new Choice();
            choice.setId(1L);
            choice.setChoiceText("A");
            choice.setIsCorrected(1);
            choices.add(choice);
            answerSheet.setChoices(choices);
            answerSheet.setPoint(1);
            answerSheets.add(answerSheet);

            // Create test exam question points
            List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
            ExamQuestionPoint point = new ExamQuestionPoint();
            point.setQuestionId(question.getId());
            point.setPoint(1);
            examQuestionPoints.add(point);

            // Mock the choice service to return expected values
            when(choiceService.findIsCorrectedById(1L)).thenReturn(1);
            when(choiceService.findChoiceTextById(1L)).thenReturn("A");

            // Get choice list
            List<ChoiceList> choiceLists = examService.getChoiceList(answerSheets, examQuestionPoints);

            // Verify results
            assertNotNull(choiceLists);
            assertFalse(choiceLists.isEmpty());
            assertEquals(1, choiceLists.size());
            assertEquals(1, choiceLists.get(0).getPoint());
            assertNotNull(choiceLists.get(0).getQuestion());
            assertNotNull(choiceLists.get(0).getChoices());
            assertEquals(1, choiceLists.get(0).getChoices().size());

            return null;
        });
    }

    /**
     * Test Case ID: UT_EM_10
     * Purpose: Test getting choice list with empty answer sheets
     * 
     * Prerequisites:
     * - None
     * 
     * Test Steps:
     * 1. Create empty answer sheets list
     * 2. Get choice list
     * 
     * Expected Results:
     * - Empty choice list is returned
     */
    @Test
    @DisplayName("Test get choice list with empty answer sheets")
    void testGetChoiceListWithEmptyAnswerSheets() {
        // Create empty answer sheets
        List<AnswerSheet> answerSheets = new ArrayList<>();
        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();

        // Get choice list
        List<ChoiceList> choiceLists = examService.getChoiceList(answerSheets, examQuestionPoints);

        // Verify results
        assertNotNull(choiceLists);
        assertTrue(choiceLists.isEmpty());
    }

    /**
     * Test Case ID: UT_EM_11
     * Purpose: Test getting choice list with null answer sheets
     * 
     * Prerequisites:
     * - None
     * 
     * Test Steps:
     * 1. Pass null answer sheets
     * 2. Get choice list
     * 
     * Expected Results:
     * - Show message "Answer sheet không được để trống"
     */
    @Test
    @DisplayName("Test get choice list with null answer sheets")
    void testGetChoiceListWithNullAnswerSheets() {
        // Test with null answer sheets
        try {
            examService.getChoiceList(null, new ArrayList<>());
        } catch (Exception e) {
            assertEquals("Answer sheet không được để trống", e.getMessage());
        }
    }

    /**
     * Test Case ID: UT_EM_12
     * Purpose: Test getting choice list with null exam question points
     * 
     * Prerequisites:
     * - None
     * 
     * Test Steps:
     * 1. Pass null exam question points
     * 2. Get choice list
     * 
     * Expected Results:
     * - Show message "Không tìm thấy thông tin câu hỏi"
     */
    @Test
    @DisplayName("Test get choice list with null exam question points")
    void testGetChoiceListWithNullExamQuestionPoints() {
        // Test with null exam question points
        try {
            examService.getChoiceList(new ArrayList<>(), null);
        } catch (Exception e) {
            assertEquals("Không tìm thấy thông tin câu hỏi", e.getMessage());
        }
    }

    // Helper methods
    private Exam createTestExam() {
        Intake intake = createTestIntake();
        Part part = createTestPart();
        
        Exam exam = new Exam();
        exam.setTitle("Test Exam_" + UUID.randomUUID().toString().substring(0, 8));
        exam.setIntake(intake);
        exam.setPart(part);
        exam.setShuffle(true);
        exam.setDurationExam(60);
        exam.setBeginExam(new Date());
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        exam.setQuestionData("[]");
        
        return exam;
    }

    private Intake createTestIntake() {
        Intake intake = new Intake();
        intake.setName("Test Intake_" + UUID.randomUUID().toString().substring(0, 8));
        return intakeRepository.save(intake);
    }

    private Part createTestPart() {
        Part part = new Part();
        part.setName("Test Part_" + UUID.randomUUID().toString().substring(0, 8));
        part.setCourse(null); // Set course to null since it's not required for this test
        return partRepository.save(part);
    }

    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password123");
        return user;
    }
} 