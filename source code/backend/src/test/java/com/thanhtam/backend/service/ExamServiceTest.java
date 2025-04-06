package com.thanhtam.backend.service;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ChoiceList;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private IntakeRepository intakeRepository;

    @Mock
    private PartService partService;

    @Mock
    private UserService userService;

    @Mock
    private QuestionService questionService;

    @Mock
    private ChoiceService choiceService;

    @InjectMocks
    private ExamServiceImpl examService;

    private Exam testExam;
    private User testUser;
    private List<Exam> examList;
    private Question testQuestion;
    private Choice testChoice;
    private Part testPart;
    private Intake testIntake;
    private QuestionType testQuestionType;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testExam = new Exam();
        testExam.setId(1L);
        testExam.setTitle("Test Exam");
        testExam.setDurationExam(60);
        testExam.setCanceled(false);
        testExam.setBeginExam(new Date());
        testExam.setFinishExam(new Date(System.currentTimeMillis() + 3600000)); // 1 hour later
        testExam.setCreatedBy(testUser);

        testQuestionType = new QuestionType();
        testQuestionType.setId(1L);
        testQuestionType.setTypeCode(EQTypeCode.MC);

        testQuestion = new Question();
        testQuestion.setId(1L);
        testQuestion.setQuestionText("Test Question");
        testQuestion.setQuestionType(testQuestionType);

        testChoice = new Choice();
        testChoice.setId(1L);
        testChoice.setChoiceText("Test Choice");
        testChoice.setIsCorrected(1);
    }

    @Test
    @DisplayName("Test save exam")
    void testSaveExam() {
        // Arrange
        when(examRepository.save(any(Exam.class))).thenReturn(testExam);

        // Act
        Exam result = examService.saveExam(testExam);

        // Assert
        assertNotNull(result);
        assertEquals(testExam.getId(), result.getId());
        assertEquals(testExam.getTitle(), result.getTitle());
        verify(examRepository).save(testExam);
    }

    @Test
    @DisplayName("Test find all with pagination")
    void testFindAllWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Exam> examList = Collections.singletonList(testExam);
        Page<Exam> examPage = new PageImpl<>(examList, pageable, examList.size());
        when(examRepository.findAll(pageable)).thenReturn(examPage);

        // Act
        Page<Exam> result = examService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testExam.getId(), result.getContent().get(0).getId());
        verify(examRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Test cancel exam")
    void testCancelExam() {
        // Arrange
        Long examId = 1L;

        // Act
        examService.cancelExam(examId);

        // Assert
        verify(examRepository).cancelExam(examId);
    }

    @Test
    @DisplayName("Test get all exams")
    void testGetAll() {
        // Arrange
        List<Exam> examList = Collections.singletonList(testExam);
        when(examRepository.findAll()).thenReturn(examList);

        // Act
        List<Exam> result = examService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExam.getId(), result.get(0).getId());
        verify(examRepository).findAll();
    }

    @Test
    @DisplayName("Test get exam by id")
    void testGetExamById() {
        // Arrange
        when(examRepository.findById(1L)).thenReturn(Optional.of(testExam));

        // Act
        Optional<Exam> result = examService.getExamById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testExam.getId(), result.get().getId());
        verify(examRepository).findById(1L);
    }

    @Test
    @DisplayName("Test find all by created by username")
    void testFindAllByCreatedBy_Username() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Exam> examList = Collections.singletonList(testExam);
        Page<Exam> examPage = new PageImpl<>(examList, pageable, examList.size());
        when(examRepository.findAllByCreatedBy_Username(pageable, "testuser")).thenReturn(examPage);

        // Act
        Page<Exam> result = examService.findAllByCreatedBy_Username(pageable, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testExam.getId(), result.getContent().get(0).getId());
        verify(examRepository).findAllByCreatedBy_Username(pageable, "testuser");
    }

    @Test
    @DisplayName("Test getChoiceList with valid data")
    void testGetChoiceList() {
        // Arrange
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(5);
        
        List<Choice> choices = new ArrayList<>();
        Choice choice = new Choice();
        choice.setId(1L);
        choice.setChoiceText("Test Choice");
        choice.setIsCorrected(1);
        choices.add(choice);
        answerSheet.setChoices(choices);
        userChoices.add(answerSheet);

        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint point = new ExamQuestionPoint();
        point.setQuestionId(1L);
        point.setPoint(5);
        examQuestionPoints.add(point);

        // Setup question type
        QuestionType questionType = new QuestionType();
        questionType.setId(1L);
        questionType.setTypeCode(EQTypeCode.MC);
        testQuestion.setQuestionType(questionType);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(testQuestion));
        when(choiceService.findIsCorrectedById(1L)).thenReturn(1);

        // Act
        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        
        ChoiceList choiceList = result.get(0);
        assertNotNull(choiceList.getQuestion());
        assertEquals(5, choiceList.getPoint());
        assertNotNull(choiceList.getChoices());
        assertEquals(1, choiceList.getChoices().size());
        
        verify(questionService).getQuestionById(1L);
        verify(choiceService).findIsCorrectedById(1L);
    }

    @Test
    @DisplayName("Test getChoiceList with empty choices")
    void testGetChoiceList_EmptyChoices() {
        // Arrange
        List<AnswerSheet> userChoices = new ArrayList<>();
        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();

        // Act
        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test getChoiceList with non-existent question")
    void testGetChoiceList_NonExistentQuestion() {
        // Arrange
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(5);
        userChoices.add(answerSheet);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            examService.getChoiceList(userChoices, Collections.emptyList());
        });

        verify(questionService).getQuestionById(1L);
    }

    @Test
    @DisplayName("Test getChoiceList with True/False question type")
    void testGetChoiceList_TrueFalseType() {
        // Arrange
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(5);
        
        List<Choice> choices = new ArrayList<>();
        Choice choice = new Choice();
        choice.setId(1L);
        choice.setChoiceText("True");
        choice.setIsCorrected(1);
        choices.add(choice);
        answerSheet.setChoices(choices);
        userChoices.add(answerSheet);

        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint point = new ExamQuestionPoint();
        point.setQuestionId(1L);
        point.setPoint(5);
        examQuestionPoints.add(point);

        // Setup question type
        QuestionType questionType = new QuestionType();
        questionType.setId(1L);
        questionType.setTypeCode(EQTypeCode.TF);
        testQuestion.setQuestionType(questionType);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(testQuestion));
        when(choiceService.findChoiceTextById(1L)).thenReturn("True");

        // Act
        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        
        ChoiceList choiceList = result.get(0);
        assertTrue(choiceList.getIsSelectedCorrected());
        verify(questionService).getQuestionById(1L);
        verify(choiceService).findChoiceTextById(1L);
    }

    @Test
    @DisplayName("Test getChoiceList with Multiple Select question type")
    void testGetChoiceList_MultipleSelectType() {
        // Arrange
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(5);
        
        List<Choice> choices = new ArrayList<>();
        Choice choice = new Choice();
        choice.setId(1L);
        choice.setChoiceText("Option A");
        choice.setIsCorrected(1);
        choices.add(choice);
        answerSheet.setChoices(choices);
        userChoices.add(answerSheet);

        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint point = new ExamQuestionPoint();
        point.setQuestionId(1L);
        point.setPoint(5);
        examQuestionPoints.add(point);

        // Setup question type
        QuestionType questionType = new QuestionType();
        questionType.setId(1L);
        questionType.setTypeCode(EQTypeCode.MS);
        testQuestion.setQuestionType(questionType);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(testQuestion));
        when(choiceService.findIsCorrectedById(1L)).thenReturn(1);

        // Act
        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        
        ChoiceList choiceList = result.get(0);
        assertTrue(choiceList.getIsSelectedCorrected());
        verify(questionService).getQuestionById(1L);
        verify(choiceService).findIsCorrectedById(1L);
    }

    @Test
    public void testGetChoiceList_TrueFalseType_WrongAnswer() {
        // Setup
        Question question = new Question();
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.TF);
        question.setQuestionType(questionType);
        question.setId(1L);

        Choice userChoice = new Choice();
        userChoice.setId(1L);
        userChoice.setChoiceText("False");

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(0);
        answerSheet.setChoices(Collections.singletonList(userChoice));

        // Mock
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findChoiceTextById(1L)).thenReturn("True");

        // Execute
        List<ChoiceList> result = examService.getChoiceList(
            Collections.singletonList(answerSheet),
            Collections.emptyList()
        );

        // Verify
        assertFalse(result.get(0).getIsSelectedCorrected());
    }

    @Test
    public void testGetChoiceList_MultipleChoiceType_PartiallyCorrect() {
        // Setup
        Question question = new Question();
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MC);
        question.setQuestionType(questionType);
        question.setId(1L);

        Choice userChoice1 = new Choice();
        userChoice1.setId(1L);
        userChoice1.setIsCorrected(0);

        Choice userChoice2 = new Choice();
        userChoice2.setId(2L);
        userChoice2.setIsCorrected(1);

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(1);
        answerSheet.setChoices(Arrays.asList(userChoice1, userChoice2));

        // Mock
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findIsCorrectedById(1L)).thenReturn(1);
        when(choiceService.findIsCorrectedById(2L)).thenReturn(0);

        // Execute
        List<ChoiceList> result = examService.getChoiceList(
            Collections.singletonList(answerSheet),
            Collections.emptyList()
        );

        // Verify
        assertFalse(result.get(0).getIsSelectedCorrected());
    }

    @Test
    public void testGetChoiceList_MultipleSelectType_MissedCorrectAnswer() {
        // Setup
        Question question = new Question();
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MS);
        question.setQuestionType(questionType);
        question.setId(1L);

        Choice userChoice1 = new Choice();
        userChoice1.setId(1L);
        userChoice1.setIsCorrected(0);

        Choice userChoice2 = new Choice();
        userChoice2.setId(2L);
        userChoice2.setIsCorrected(0);

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(0);
        answerSheet.setChoices(Arrays.asList(userChoice1, userChoice2));

        // Mock
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findIsCorrectedById(1L)).thenReturn(1);
        when(choiceService.findIsCorrectedById(2L)).thenReturn(0);

        // Execute
        List<ChoiceList> result = examService.getChoiceList(
            Collections.singletonList(answerSheet),
            Collections.emptyList()
        );

        // Verify
        assertFalse(result.get(0).getIsSelectedCorrected());
    }

    @Test
    public void testGetChoiceList_MultipleChoiceType_WrongAnswer() {
        // Setup
        Question question = new Question();
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MC);
        question.setQuestionType(questionType);
        question.setId(1L);

        Choice userChoice = new Choice();
        userChoice.setId(1L);
        userChoice.setIsCorrected(1);

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(0);
        answerSheet.setChoices(Collections.singletonList(userChoice));

        // Mock
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findIsCorrectedById(1L)).thenReturn(0);

        // Execute
        List<ChoiceList> result = examService.getChoiceList(
            Collections.singletonList(answerSheet),
            Collections.emptyList()
        );

        // Verify
        assertFalse(result.get(0).getIsSelectedCorrected());
    }

    @Test
    @DisplayName("Test getChoiceList when question is not found")
    void testGetChoiceList_QuestionNotFound() {
        // Arrange
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(5);
        userChoices.add(answerSheet);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            examService.getChoiceList(userChoices, Collections.emptyList());
        });
    }

    @Test
    @DisplayName("Test getChoiceList when choice text is not found")
    void testGetChoiceList_ChoiceTextNotFound() {
        // Setup
        Question question = new Question();
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.TF);
        question.setQuestionType(questionType);
        question.setId(1L);

        Choice userChoice = new Choice();
        userChoice.setId(1L);
        userChoice.setChoiceText("Test");

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(0);
        answerSheet.setChoices(Collections.singletonList(userChoice));

        // Mock
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findChoiceTextById(1L)).thenReturn(null);

        // Execute
        List<ChoiceList> result = examService.getChoiceList(
            Collections.singletonList(answerSheet),
            Collections.emptyList()
        );

        // Verify
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertFalse(result.get(0).getIsSelectedCorrected());
    }

    @Test
    @DisplayName("Test getChoiceList when isCorrected is not found")
    void testGetChoiceList_ChoiceIsCorrectedNotFound() {
        // Setup
        Question question = new Question();
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MC);
        question.setQuestionType(questionType);
        question.setId(1L);

        Choice userChoice = new Choice();
        userChoice.setId(1L);
        userChoice.setIsCorrected(1);

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(0);
        answerSheet.setChoices(Collections.singletonList(userChoice));

        // Mock
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findIsCorrectedById(1L)).thenReturn(null);

        // Execute & Assert
        assertThrows(NullPointerException.class, () -> {
            examService.getChoiceList(
                Collections.singletonList(answerSheet),
                Collections.emptyList()
            );
        });
    }

    @Test
    @DisplayName("Test getChoiceList with empty choices list")
    void testGetChoiceList_EmptyChoicesList() {
        // Arrange
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(5);
        answerSheet.setChoices(new ArrayList<>());
        userChoices.add(answerSheet);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(testQuestion));

        // Act
        List<ChoiceList> result = examService.getChoiceList(userChoices, Collections.emptyList());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getChoices().isEmpty());
    }

    @Test
    @DisplayName("Test getChoiceList with null choices")
    void testGetChoiceList_NullChoices() {
        // Arrange
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(5);
        answerSheet.setChoices(null);
        userChoices.add(answerSheet);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(testQuestion));

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            examService.getChoiceList(userChoices, Collections.emptyList());
        });
    }

    @Test
    @DisplayName("Test getChoiceList with unknown question type")
    void testGetChoiceList_UnknownQuestionType() {
        // Setup
        Question question = new Question();
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(null);
        question.setQuestionType(questionType);
        question.setId(1L);

        Choice userChoice = new Choice();
        userChoice.setId(1L);
        userChoice.setIsCorrected(1);

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(0);
        answerSheet.setChoices(Collections.singletonList(userChoice));

        // Mock
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));

        // Execute & Assert
        assertThrows(NullPointerException.class, () -> {
            examService.getChoiceList(
                Collections.singletonList(answerSheet),
                Collections.emptyList()
            );
        });
    }

    @Test
    @DisplayName("Test getChoiceList when question is null")
    void testGetChoiceList_NullQuestion() {
        // Setup
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(5);
        userChoices.add(answerSheet);

        when(questionService.getQuestionById(1L)).thenReturn(Optional.empty());

        // Execute & Assert
        assertThrows(NoSuchElementException.class, () -> {
            examService.getChoiceList(userChoices, Collections.emptyList());
        });
    }

    @Test
    @DisplayName("Test getChoiceList when isCorrected is null")
    void testGetChoiceList_NullIsCorrected() {
        // Setup
        Question question = new Question();
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MC);
        question.setQuestionType(questionType);
        question.setId(1L);

        Choice userChoice = new Choice();
        userChoice.setId(1L);
        userChoice.setIsCorrected(1);

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(0);
        answerSheet.setChoices(Collections.singletonList(userChoice));

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findIsCorrectedById(1L)).thenReturn(null);

        // Execute & Assert
        assertThrows(NullPointerException.class, () -> {
            examService.getChoiceList(
                Collections.singletonList(answerSheet),
                Collections.emptyList()
            );
        });
    }

    @Test
    @DisplayName("Test getChoiceList when choice text is null")
    void testGetChoiceList_NullChoiceText() {
        // Setup
        Question question = new Question();
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.TF);
        question.setQuestionType(questionType);
        question.setId(1L);

        Choice userChoice = new Choice();
        userChoice.setId(1L);
        userChoice.setChoiceText("Test");

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(0);
        answerSheet.setChoices(Collections.singletonList(userChoice));

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));
        when(choiceService.findChoiceTextById(1L)).thenReturn(null);

        // Execute & Assert
        assertThrows(NullPointerException.class, () -> {
            examService.getChoiceList(
                Collections.singletonList(answerSheet),
                Collections.emptyList()
            );
        });
    }

    @Test
    @DisplayName("Test getChoiceList when question type is null")
    void testGetChoiceList_NullQuestionType() {
        // Setup
        Question question = new Question();
        question.setQuestionType(null);
        question.setId(1L);

        Choice userChoice = new Choice();
        userChoice.setId(1L);
        userChoice.setIsCorrected(1);

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(1L);
        answerSheet.setPoint(0);
        answerSheet.setChoices(Collections.singletonList(userChoice));

        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(question));

        // Execute & Assert
        assertThrows(NullPointerException.class, () -> {
            examService.getChoiceList(
                Collections.singletonList(answerSheet),
                Collections.emptyList()
            );
        });
    }
} 