package com.thanhtam.backend.service;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.ultilities.DifficultyLevel;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Question question;
    private Part part;
    private User user;
    private Choice choice1;
    private Choice choice2;
    private QuestionType questionType;

    @BeforeEach
    void setUp() {
        // Setup test data
        user = new User();
        user.setUsername("testuser");

        part = new Part();
        part.setId(1L);
        part.setName("Test Part");

        questionType = new QuestionType();
        questionType.setId(1L);
        questionType.setTypeCode(EQTypeCode.MC);
        questionType.setDescription("Multiple Choice");

        choice1 = new Choice();
        choice1.setId(1L);
        choice1.setChoiceText("Choice 1");
        choice1.setIsCorrected(1);

        choice2 = new Choice();
        choice2.setId(2L);
        choice2.setChoiceText("Choice 2");
        choice2.setIsCorrected(0);

        question = new Question();
        question.setId(1L);
        question.setQuestionText("Test Question");
        question.setDifficultyLevel(DifficultyLevel.EASY);
        question.setPart(part);
        question.setCreatedBy(user);
        question.setQuestionType(questionType);
        question.setChoices(Arrays.asList(choice1, choice2));
    }

    /**
     * Test Case ID: UT_QBM_07
     * Purpose: Test retrieving a question by its ID
     * 
     * Prerequisites:
     * - Question exists in repository
     * 
     * Test Steps:
     * 1. Mock repository to return a question
     * 2. Call getQuestionById
     * 
     * Expected Results:
     * - Returns the correct question
     */
    @Test
    @DisplayName("Should return question when valid ID is provided")
    void getQuestionById_ShouldReturnQuestion_WhenValidIdProvided() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        Optional<Question> result = questionService.getQuestionById(1L);

        assertTrue(result.isPresent());
        assertEquals(question, result.get());
        verify(questionRepository).findById(1L);
    }

    /**
     * Test Case ID: UT_QBM_08
     * Purpose: Test retrieving questions by part
     */
    @Test
    @DisplayName("Should return questions when valid part is provided")
    void getQuestionByPart_ShouldReturnQuestions_WhenValidPartProvided() {
        List<Question> expectedQuestions = Arrays.asList(question);
        when(questionRepository.findByPart(part)).thenReturn(expectedQuestions);

        List<Question> result = questionService.getQuestionByPart(part);

        assertEquals(expectedQuestions, result);
        verify(questionRepository).findByPart(part);
    }

    /**
     * Test Case ID: UT_QBM_09
     * Purpose: Test retrieving questions by question type
     */
    @Test
    @DisplayName("Should return questions when valid question type is provided")
    void getQuestionByQuestionType_ShouldReturnQuestions_WhenValidTypeProvided() {
        List<Question> expectedQuestions = Arrays.asList(question);
        when(questionRepository.findByQuestionType(questionType)).thenReturn(expectedQuestions);

        List<Question> result = questionService.getQuestionByQuestionType(questionType);

        assertEquals(expectedQuestions, result);
        verify(questionRepository).findByQuestionType(questionType);
    }

    /**
     * Test Case ID: UT_QBM_10
     * Purpose: Test converting question list to answer sheets
     */
    @Test
    @DisplayName("Should convert questions to answer sheets correctly")
    void convertFromQuestionList_ShouldConvertCorrectly() {
        List<Question> questions = Arrays.asList(question);
        
        List<AnswerSheet> result = questionService.convertFromQuestionList(questions);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(question.getId(), result.get(0).getQuestionId());
        assertEquals(2, result.get(0).getChoices().size());
        result.get(0).getChoices().forEach(choice -> assertEquals(0, choice.getIsCorrected()));
    }

    /**
     * Test Case ID: UT_QBM_11
     * Purpose: Test saving a question with different difficulty levels
     */
    @Test
    @DisplayName("Should set correct points based on difficulty level")
    void save_ShouldSetCorrectPoints_BasedOnDifficultyLevel() {
        // Test EASY difficulty
        question.setDifficultyLevel(DifficultyLevel.EASY);
        questionService.save(question);
        assertEquals(5, question.getPoint());

        // Test MEDIUM difficulty
        question.setDifficultyLevel(DifficultyLevel.MEDIUM);
        questionService.save(question);
        assertEquals(10, question.getPoint());

        // Test HARD difficulty
        question.setDifficultyLevel(DifficultyLevel.HARD);
        questionService.save(question);
        assertEquals(15, question.getPoint());

        verify(questionRepository, times(3)).save(any(Question.class));
    }

    /**
     * Test Case ID: UT_QBM_12
     * Purpose: Test pagination functionality
     */
    @Test
    @DisplayName("Should return paginated questions")
    void findAllQuestions_ShouldReturnPaginatedQuestions() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Question> questions = Arrays.asList(question);
        Page<Question> questionPage = new PageImpl<>(questions);
        when(questionRepository.findAll(pageable)).thenReturn(questionPage);

        Page<Question> result = questionService.findAllQuestions(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(questionRepository).findAll(pageable);
    }

    /**
     * Test Case ID: UT_QBM_13
     * Purpose: Test finding questions by part and username
     */
    @Test
    @DisplayName("Should return questions by part and username")
    void findQuestionsByPart_IdAndCreatedBy_Username_ShouldReturnQuestions() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Question> questions = Arrays.asList(question);
        Page<Question> questionPage = new PageImpl<>(questions);
        when(questionRepository.findQuestionsByPart_IdAndCreatedBy_Username(pageable, 1L, "testuser"))
            .thenReturn(questionPage);

        Page<Question> result = questionService.findQuestionsByPart_IdAndCreatedBy_Username(pageable, 1L, "testuser");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(questionRepository).findQuestionsByPart_IdAndCreatedBy_Username(pageable, 1L, "testuser");
    }

    /**
     * Test Case ID: UT_QBM_14
     * Purpose: Test deleting a question
     */
    @Test
    @DisplayName("Should delete question when valid ID is provided")
    void delete_ShouldDeleteQuestion_WhenValidIdProvided() {
        doNothing().when(questionRepository).deleteById(1L);

        questionService.delete(1L);

        verify(questionRepository).deleteById(1L);
    }

    /**
     * Test Case ID: UT_QBM_15
     * Purpose: Test getting question point list
     */
    @Test
    @DisplayName("Should return questions from exam question points")
    void getQuestionPointList_ShouldReturnQuestions() {
        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(1L);
        List<ExamQuestionPoint> examQuestionPoints = Arrays.asList(examQuestionPoint);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        List<Question> result = questionService.getQuestionPointList(examQuestionPoints);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(question, result.get(0));
        verify(questionRepository).findById(1L);
    }

    /**
     * Test Case ID: UT_QBM_16
     * Purpose: Test finding non-deleted questions by part
     * 
     * Prerequisites:
     * - Questions exist in repository, some marked as deleted
     * 
     * Test Steps:
     * 1. Mock repository to return a page of questions
     * 2. Call findQuestionsByPartAndDeletedFalse
     * 
     * Expected Results:
     * - Returns a page of non-deleted questions for the specified part
     */
    @Test
    @DisplayName("Should return non-deleted questions by part")
    void findQuestionsByPartAndDeletedFalse_ShouldReturnNonDeletedQuestions() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Question> questions = Arrays.asList(question);
        Page<Question> questionPage = new PageImpl<>(questions);
        when(questionRepository.findQuestionsByPartAndDeletedFalse(pageable, part))
            .thenReturn(questionPage);

        Page<Question> result = questionService.findQuestionsByPartAndDeletedFalse(pageable, part);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(questionRepository).findQuestionsByPartAndDeletedFalse(pageable, part);
    }
} 