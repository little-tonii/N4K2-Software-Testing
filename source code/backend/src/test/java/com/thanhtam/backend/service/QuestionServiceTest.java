package com.thanhtam.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ExamQuestionPoint; // Assuming this import exists
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.ultilities.DifficultyLevel;
import com.thanhtam.backend.ultilities.EQTypeCode; // Import the enum
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Question question1;
    private Question question2;
    private Part part1;
    private QuestionType mcqType;
    private User user; // Assuming User entity exists and is needed for createdBy

    @BeforeEach
    void setUp() {
        // --- Assuming User entity setup ---
        user = new User(); // Replace with actual User construction if needed
        user.setId(1L);
        user.setUsername("testuser");
        // ---------------------------------

        Course course = new Course(); // Parts belong to Courses
        course.setId(1L);
        course.setName("Test Course");

        part1 = new Part();
        part1.setId(1L);
        part1.setName("Part 1");
        part1.setCourse(course); // Link part to course

        mcqType = new QuestionType();
        mcqType.setId(1L);
        // --- FIX 1: Use setTypeCode with the enum ---
        mcqType.setTypeCode(EQTypeCode.MC);
        mcqType.setDescription("Multiple Choice Question"); // Optional: Set description if needed

        // --- FIX 2: Constructor confirmed correct ---
        Choice choice1 = new Choice(1L, "Choice A", 1);
        Choice choice2 = new Choice(2L, "Choice B", 0);
        List<Choice> choices1 = new ArrayList<>(
            Arrays.asList(choice1, choice2)
        );

        question1 = new Question();
        question1.setId(1L);
        question1.setQuestionText("What is Java?");
        question1.setDifficultyLevel(DifficultyLevel.EASY);
        question1.setPart(part1);
        question1.setQuestionType(mcqType);
        question1.setChoices(choices1);
        question1.setCreatedBy(user); // Link question to user
        question1.setDeleted(false);

        // --- FIX 3: setQuestion calls confirmed unnecessary and removed ---
        // choice1.setQuestion(question1); // Removed
        // choice2.setQuestion(question1); // Removed

        question2 = new Question();
        question2.setId(2L);
        question2.setQuestionText("What is Spring?");
        question2.setDifficultyLevel(DifficultyLevel.MEDIUM);
        question2.setPart(part1);
        question2.setQuestionType(mcqType);
        question2.setCreatedBy(user); // Link question to user
        question2.setDeleted(false);
    }

    // ... (Các test case từ getQuestionById đến getQuestionByQuestionType giữ nguyên) ...
    @Test
    @DisplayName("Test getQuestionById - Found")
    void getQuestionById_WhenFound_ShouldReturnQuestion() {
        // Arrange: Define mock behavior
        when(questionRepository.findById(1L)).thenReturn(
            Optional.of(question1)
        );

        // Act: Call the service method
        Optional<Question> foundQuestion = questionService.getQuestionById(1L);

        // Assert: Check the results
        assertTrue(foundQuestion.isPresent(), "Question should be found");
        assertEquals(question1.getId(), foundQuestion.get().getId());
        assertEquals(
            question1.getQuestionText(),
            foundQuestion.get().getQuestionText()
        );

        // Verify: Check if repository method was called
        verify(questionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test getQuestionById - Not Found")
    void getQuestionById_WhenNotFound_ShouldReturnEmptyOptional() {
        // Arrange
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Question> foundQuestion = questionService.getQuestionById(99L);

        // Assert
        assertFalse(foundQuestion.isPresent(), "Question should not be found");

        // Verify
        verify(questionRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Test getQuestionByPart")
    void getQuestionByPart_ShouldReturnListOfQuestions() {
        // Arrange
        List<Question> expectedQuestions = Arrays.asList(question1, question2);
        when(questionRepository.findByPart(part1)).thenReturn(
            expectedQuestions
        );

        // Act
        List<Question> actualQuestions = questionService.getQuestionByPart(
            part1
        );

        // Assert
        assertNotNull(actualQuestions);
        assertEquals(2, actualQuestions.size());
        assertEquals(expectedQuestions, actualQuestions);

        // Verify
        verify(questionRepository, times(1)).findByPart(part1);
    }

    @Test
    @DisplayName("Test getQuestionByQuestionType")
    void getQuestionByQuestionType_ShouldReturnListOfQuestions() {
        // Arrange
        List<Question> expectedQuestions = Arrays.asList(question1, question2);
        when(questionRepository.findByQuestionType(mcqType)).thenReturn(
            expectedQuestions
        );

        // Act
        List<Question> actualQuestions =
            questionService.getQuestionByQuestionType(mcqType);

        // Assert
        assertNotNull(actualQuestions);
        assertEquals(2, actualQuestions.size());
        assertEquals(expectedQuestions, actualQuestions);

        // Verify
        verify(questionRepository, times(1)).findByQuestionType(mcqType);
    }

    @Test
    @DisplayName("Test getQuestionPointList")
    void getQuestionPointList_ShouldReturnQuestionsForGivenPoints() {
        // Arrange
        // --- FIX 4: Use setters for ExamQuestionPoint (assuming setters exist) ---
        ExamQuestionPoint point1 = new ExamQuestionPoint();
        point1.setQuestionId(1L); // CHECK DTO: Ensure this setter exists
        point1.setPoint(5); // CHECK DTO: Ensure this setter exists

        ExamQuestionPoint point2 = new ExamQuestionPoint();
        point2.setQuestionId(2L); // CHECK DTO: Ensure this setter exists
        point2.setPoint(10); // CHECK DTO: Ensure this setter exists

        List<ExamQuestionPoint> examQuestionPoints = Arrays.asList(
            point1,
            point2
        );

        when(questionRepository.findById(1L)).thenReturn(
            Optional.of(question1)
        );
        when(questionRepository.findById(2L)).thenReturn(
            Optional.of(question2)
        );

        // Act
        List<Question> actualQuestions = questionService.getQuestionPointList(
            examQuestionPoints
        );

        // Assert
        assertNotNull(actualQuestions);
        assertEquals(2, actualQuestions.size());
        assertTrue(actualQuestions.contains(question1));
        assertTrue(actualQuestions.contains(question2));

        // Verify
        verify(questionRepository, times(1)).findById(1L);
        verify(questionRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Test convertFromQuestionList")
    void convertFromQuestionList_ShouldReturnAnswerSheetsWithChoicesCorrectedSetToZero() {
        // Arrange
        question1.getChoices().forEach(c -> c.setIsCorrected(1));
        List<Question> questionList = Arrays.asList(question1);
        question1.setPoint(5);

        // Act
        List<AnswerSheet> answerSheets =
            questionService.convertFromQuestionList(questionList);

        // Assert
        assertNotNull(answerSheets);
        assertEquals(1, answerSheets.size());
        AnswerSheet sheet = answerSheets.get(0);
        assertEquals(question1.getId(), sheet.getQuestionId());
        assertEquals(question1.getPoint(), sheet.getPoint());
        assertNotNull(sheet.getChoices());
        assertEquals(2, sheet.getChoices().size());
        assertTrue(
            sheet
                .getChoices()
                .stream()
                .allMatch(choice -> choice.getIsCorrected() == 0)
        );
        // Check original question choices modification (as noted before)
        assertTrue(
            question1
                .getChoices()
                .stream()
                .allMatch(choice -> choice.getIsCorrected() == 0)
        );

        verifyNoInteractions(questionRepository);
    }

    @Test
    @DisplayName("Test getQuestionList")
    void getQuestionList_ShouldReturnAllQuestions() {
        // Arrange
        List<Question> expectedQuestions = Arrays.asList(question1, question2);
        when(questionRepository.findAll()).thenReturn(expectedQuestions);

        // Act
        List<Question> actualQuestions = questionService.getQuestionList();

        // Assert
        assertNotNull(actualQuestions);
        assertEquals(2, actualQuestions.size());
        assertEquals(expectedQuestions, actualQuestions);

        // Verify
        verify(questionRepository, times(1)).findAll();
    }

    // --- Tests for Paginated Methods ---

    @Test
    @DisplayName("Test findQuestionsByPart")
    void findQuestionsByPart_ShouldReturnPagedQuestions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Question> questionsInPage = Arrays.asList(question1, question2);
        Page<Question> expectedPage = new PageImpl<>(
            questionsInPage,
            pageable,
            questionsInPage.size()
        );
        when(
            questionRepository.findQuestionsByPart(pageable, part1)
        ).thenReturn(expectedPage);

        // Act
        Page<Question> actualPage = questionService.findQuestionsByPart(
            pageable,
            part1
        );

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getContent().size());
        assertEquals(expectedPage, actualPage);

        // Verify
        verify(questionRepository, times(1)).findQuestionsByPart(
            pageable,
            part1
        );
    }

    @Test
    @DisplayName("Test findQuestionsByPartAndDeletedFalse")
    void findQuestionsByPartAndDeletedFalse_ShouldReturnPagedQuestions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        // --- FIX 5: Use Arrays.asList instead of List.of ---
        List<Question> questionsInPage = Arrays.asList(question1); // Only non-deleted
        Page<Question> expectedPage = new PageImpl<>(
            questionsInPage,
            pageable,
            1
        );
        when(
            questionRepository.findQuestionsByPartAndDeletedFalse(
                pageable,
                part1
            )
        ).thenReturn(expectedPage);

        // Act
        Page<Question> actualPage =
            questionService.findQuestionsByPartAndDeletedFalse(pageable, part1);

        // Assert
        assertNotNull(actualPage);
        assertEquals(1, actualPage.getNumberOfElements());
        assertEquals(expectedPage, actualPage);

        // Verify
        verify(questionRepository, times(1)).findQuestionsByPartAndDeletedFalse(
            pageable,
            part1
        );
    }

    @Test
    @DisplayName(
        "Test findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse"
    )
    void findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse_ShouldReturnPagedQuestions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Long partId = part1.getId();
        String username = user.getUsername();
        List<Question> questionsInPage = Arrays.asList(question1, question2); // Assuming both match
        Page<Question> expectedPage = new PageImpl<>(
            questionsInPage,
            pageable,
            2
        );
        when(
            questionRepository.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(
                pageable,
                partId,
                username
            )
        ).thenReturn(expectedPage);

        // Act
        Page<Question> actualPage =
            questionService.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(
                pageable,
                partId,
                username
            );

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getNumberOfElements());
        assertEquals(expectedPage, actualPage);

        // Verify
        verify(
            questionRepository,
            times(1)
        ).findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(
            pageable,
            partId,
            username
        );
    }

    @Test
    @DisplayName("Test findAllQuestions")
    void findAllQuestions_ShouldReturnPagedQuestions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Question> questionsInPage = Arrays.asList(question1, question2);
        Page<Question> expectedPage = new PageImpl<>(
            questionsInPage,
            pageable,
            2
        );
        when(questionRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Question> actualPage = questionService.findAllQuestions(pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getNumberOfElements());
        assertEquals(expectedPage, actualPage);

        // Verify
        verify(questionRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test findQuestionTextById")
    void findQuestionTextById_ShouldReturnQuestionText() {
        // Arrange
        Long questionId = 1L;
        String expectedText = "What is Java?";
        when(questionRepository.findQuestionTextById(questionId)).thenReturn(
            expectedText
        );

        // Act
        String actualText = questionService.findQuestionTextById(questionId);

        // Assert
        assertEquals(expectedText, actualText);

        // Verify
        verify(questionRepository, times(1)).findQuestionTextById(questionId);
    }

    @Test
    @DisplayName("Test findQuestionsByPart_IdAndCreatedBy_Username")
    void findQuestionsByPart_IdAndCreatedBy_Username_ShouldReturnPagedQuestions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Long partId = part1.getId();
        String username = user.getUsername();
        List<Question> questionsInPage = Arrays.asList(question1, question2); // Assuming both match
        Page<Question> expectedPage = new PageImpl<>(
            questionsInPage,
            pageable,
            2
        );
        when(
            questionRepository.findQuestionsByPart_IdAndCreatedBy_Username(
                pageable,
                partId,
                username
            )
        ).thenReturn(expectedPage);

        // Act
        Page<Question> actualPage =
            questionService.findQuestionsByPart_IdAndCreatedBy_Username(
                pageable,
                partId,
                username
            );

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getNumberOfElements());
        assertEquals(expectedPage, actualPage);

        // Verify
        verify(
            questionRepository,
            times(1)
        ).findQuestionsByPart_IdAndCreatedBy_Username(
            pageable,
            partId,
            username
        );
    }

    @Test
    @DisplayName("Test findQuestionsByCreatedBy_Username")
    void findQuestionsByCreatedBy_Username_ShouldReturnPagedQuestions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String username = user.getUsername();
        List<Question> questionsInPage = Arrays.asList(question1, question2); // Assuming both match
        Page<Question> expectedPage = new PageImpl<>(
            questionsInPage,
            pageable,
            2
        );
        when(
            questionRepository.findQuestionsByCreatedBy_Username(
                pageable,
                username
            )
        ).thenReturn(expectedPage);

        // Act
        Page<Question> actualPage =
            questionService.findQuestionsByCreatedBy_Username(
                pageable,
                username
            );

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getNumberOfElements());
        assertEquals(expectedPage, actualPage);

        // Verify
        verify(questionRepository, times(1)).findQuestionsByCreatedBy_Username(
            pageable,
            username
        );
    }

    // --- Tests for Modifying Methods ---

    @Test
    @DisplayName("Test save - Easy Question")
    void save_EasyQuestion_ShouldSetPointTo5AndCallRepositorySave() {
        // Arrange
        Question newQuestion = new Question();
        newQuestion.setQuestionText("New Easy Q");
        newQuestion.setDifficultyLevel(DifficultyLevel.EASY);
        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(
            Question.class
        );

        // Act
        questionService.save(newQuestion);

        // Verify
        verify(questionRepository, times(1)).save(questionCaptor.capture());

        // Assert
        Question savedQuestion = questionCaptor.getValue();
        assertNotNull(savedQuestion);
        assertEquals(
            5,
            savedQuestion.getPoint(),
            "Point for EASY question should be 5"
        );
        assertEquals(DifficultyLevel.EASY, savedQuestion.getDifficultyLevel());
        assertEquals("New Easy Q", savedQuestion.getQuestionText());
    }

    // ... (Các test case save cho MEDIUM, HARD, Default giữ nguyên) ...
    @Test
    @DisplayName("Test save - Medium Question")
    void save_MediumQuestion_ShouldSetPointTo10AndCallRepositorySave() {
        // Arrange
        Question newQuestion = new Question();
        newQuestion.setQuestionText("New Medium Q");
        newQuestion.setDifficultyLevel(DifficultyLevel.MEDIUM);
        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(
            Question.class
        );

        // Act
        questionService.save(newQuestion);

        // Verify
        verify(questionRepository, times(1)).save(questionCaptor.capture());

        // Assert
        Question savedQuestion = questionCaptor.getValue();
        assertEquals(
            10,
            savedQuestion.getPoint(),
            "Point for MEDIUM question should be 10"
        );
    }

    @Test
    @DisplayName("Test save - Hard Question")
    void save_HardQuestion_ShouldSetPointTo15AndCallRepositorySave() {
        // Arrange
        Question newQuestion = new Question();
        newQuestion.setQuestionText("New Hard Q");
        newQuestion.setDifficultyLevel(DifficultyLevel.HARD);
        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(
            Question.class
        );

        // Act
        questionService.save(newQuestion);

        // Verify
        verify(questionRepository, times(1)).save(questionCaptor.capture());

        // Assert
        Question savedQuestion = questionCaptor.getValue();
        assertEquals(
            15,
            savedQuestion.getPoint(),
            "Point for HARD question should be 15"
        );
    }

    @Test
    @DisplayName("Test save - Default/Unknown Difficulty")
    void save_DefaultDifficulty_ShouldSetPointTo0AndCallRepositorySave() {
        // Arrange
        Question newQuestion = new Question();
        newQuestion.setQuestionText("New Unknown Difficulty Q");
        newQuestion.setDifficultyLevel(null); // Or some other value not handled explicitly
        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(
            Question.class
        );

        // Act
        questionService.save(newQuestion);

        // Verify
        verify(questionRepository, times(1)).save(questionCaptor.capture());

        // Assert
        Question savedQuestion = questionCaptor.getValue();
        assertEquals(
            0,
            savedQuestion.getPoint(),
            "Point for default/unknown difficulty should be 0"
        );
    }

    // ... (Test case update và delete giữ nguyên) ...
    @Test
    @DisplayName("Test update")
    void update_ShouldCallRepositorySave() {
        // Arrange
        // question1 already exists from setup

        // Act
        questionService.update(question1);

        // Verify
        verify(questionRepository, times(1)).save(question1); // Ensure save is called with the exact object
    }

    @Test
    @DisplayName("Test delete")
    void delete_ShouldCallRepositoryDeleteById() {
        // Arrange
        Long idToDelete = 1L;
        // doNothing().when(questionRepository).deleteById(idToDelete); // Generally not needed unless testing exceptions

        // Act
        questionService.delete(idToDelete);

        // Verify
        verify(questionRepository, times(1)).deleteById(idToDelete);
    }
}
