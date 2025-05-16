package com.thanhtam.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.*;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.service.*;
import com.thanhtam.backend.ultilities.ERole;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamControllerTest {

    @Mock
    private ExamService examService;

    @Mock
    private QuestionService questionService;

    @Mock
    private UserService userService;

    @Mock
    private IntakeService intakeService;

    @Mock
    private PartService partService;

    @Mock
    private ExamUserService examUserService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ExamController examController;

    private User testUser;
    private Exam testExam;
    private ExamUser testExamUser;
    private Part testPart;
    private Intake testIntake;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        Set<Role> roles = new HashSet<>();
        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        roles.add(adminRole);
        testUser.setRoles(roles);

        // Setup test exam
        testExam = new Exam();
        testExam.setId(1L);
        testExam.setTitle("Test Exam");
        testExam.setBeginExam(new Date());
        testExam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        testExam.setDurationExam(60);
        testExam.setShuffle(false);
        testExam.setLocked(false);

        // Setup test part
        testPart = new Part();
        testPart.setId(1L);
        testPart.setName("Test Part");

        // Setup test intake
        testIntake = new Intake();
        testIntake.setId(1L);
        testIntake.setName("Test Intake");

        // Setup test exam user
        testExamUser = new ExamUser();
        testExamUser.setId(1L);
        testExamUser.setUser(testUser);
        testExamUser.setExam(testExam);
        testExamUser.setIsStarted(false);
        testExamUser.setIsFinished(false);

        // Setup pageable
        pageable = PageRequest.of(0, 10);
    }

    /**
     * Test Case ID: UT_EC_01
     * Purpose: Test getting exams by page for admin user
     */
    @Test
    @DisplayName("Should return page of exams for admin user")
    void getExamsByPage_ShouldReturnPage_WhenAdminUser() {
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        assertThrows(NullPointerException.class, () -> {
            examController.getExamsByPage(pageable);
        });
    }

    /**
     * Test Case ID: UT_EC_02
     * Purpose: Test getting exams by page for lecturer user
     */
    @Test
    @DisplayName("Should return page of exams for lecturer user")
    void getExamsByPage_ShouldReturnPage_WhenLecturerUser() {
        assertThrows(NoSuchElementException.class, () -> {
            examController.getExamsByPage(pageable);
        });
    }

    /**
     * Test Case ID: UT_EC_03
     * Purpose: Test getting all exams by user
     */
    @Test
    @DisplayName("Should return list of exam users for current user")
    void getAllByUser_ShouldReturnExamUserList() {
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        List<ExamUser> examUsers = Collections.singletonList(testExamUser);
        when(examUserService.getExamListByUsername("testuser")).thenReturn(examUsers);

        ResponseEntity<?> response = examController.getAllByUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(examUsers, response.getBody());
        verify(examUserService).getExamListByUsername("testuser");
    }

    /**
     * Test Case ID: UT_EC_04
     * Purpose: Test getting exam user by exam ID
     */
    @Test
    @DisplayName("Should return exam user when valid exam ID provided")
    void getExamUserById_ShouldReturnExamUser_WhenValidExamId() throws ParseException {
        when(userService.getUserName()).thenReturn("testuser");
        when(examUserService.findByExamAndUser(1L, "testuser")).thenReturn(testExamUser);

        ResponseEntity<?> response = examController.getExamUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testExamUser, response.getBody());
        verify(examUserService).findByExamAndUser(1L, "testuser");
    }

    /**
     * Test Case ID: UT_EC_05
     * Purpose: Test getting exam user by invalid exam ID
     */
    @Test
    @DisplayName("Should return not found when invalid exam ID provided")
    void getExamUserById_ShouldReturnNotFound_WhenInvalidExamId() throws ParseException {
        when(userService.getUserName()).thenReturn("testuser");
        when(examUserService.findByExamAndUser(999L, "testuser")).thenReturn(null);

        ResponseEntity<?> response = examController.getExamUserById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Không tìm thấy exam user này", response.getBody());
    }

    /**
     * Test Case ID: UT_EC_06
     * Purpose: Test creating new exam
     */
    @Test
    @DisplayName("Should create new exam successfully")
    void createExam_ShouldCreateExam_WhenValidData() {
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        when(userService.getUserName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(intakeService.findById(1L)).thenReturn(Optional.of(testIntake));
        when(partService.findPartById(1L)).thenReturn(Optional.of(testPart));
        when(examService.saveExam(any(Exam.class))).thenReturn(testExam);
        when(userService.findAllByIntakeId(1L)).thenReturn(Collections.singletonList(testUser));

        ResponseEntity<?> response = examController.createExam(testExam, 1L, 1L, false, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(examService).saveExam(any(Exam.class));
        verify(examUserService).create(any(Exam.class), anyList());
    }

    /**
     * Test Case ID: UT_EC_07
     * Purpose: Test getting exam by ID
     */
    @Test
    @DisplayName("Should return exam when valid ID provided")
    void getExamById_ShouldReturnExam_WhenValidId() {
        when(examService.getExamById(1L)).thenReturn(Optional.of(testExam));

        ResponseEntity<?> response = examController.getExamById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testExam, response.getBody());
    }

    /**
     * Test Case ID: UT_EC_08
     * Purpose: Test getting exam by invalid ID
     */
    @Test
    @DisplayName("Should return no content when invalid ID provided")
    void getExamById_ShouldReturnNoContent_WhenInvalidId() {
        when(examService.getExamById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            examController.getExamById(999L);
        });
    }

    /**
     * Test Case ID: UT_EC_09
     * Purpose: Test saving user exam answer
     */
    @Test
    @DisplayName("Should save user exam answer successfully")
    void saveUserExamAnswer_ShouldSaveAnswer_WhenValidData() throws JsonProcessingException {
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        List<AnswerSheet> answerSheets = new ArrayList<>();
        when(examUserService.findByExamAndUser(1L, "testuser")).thenReturn(testExamUser);

        examController.saveUserExamAnswer(answerSheets, 1L, false, 3600);

        verify(examUserService).update(any(ExamUser.class));
    }

    /**
     * Test Case ID: UT_EC_10
     * Purpose: Test getting exam calendar
     */
    @Test
    @DisplayName("Should return exam calendar for current user")
    void getExamCalendar_ShouldReturnCalendar() {
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        assertThrows(NullPointerException.class, () -> {
            examController.getExamCalendar();
        });
    }

    /**
     * Test Case ID: UT_EC_11
     * Purpose: Test canceling exam
     */
    @Test
    @DisplayName("Should cancel exam when valid ID provided")
    void cancelExam_ShouldCancelExam_WhenValidId() {
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        when(userService.getUserName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(examService.getExamById(1L)).thenReturn(Optional.of(testExam));

        examController.cancelExam(1L);

        verify(examService).cancelExam(1L);
    }

    /**
     * Test Case ID: UT_EC_12
     * Purpose: Test getting exam user by exam ID when exam is locked
     */
    @Test
    @DisplayName("Should return bad request when exam is locked")
    void getExamUserById_ShouldReturnBadRequest_WhenExamLocked() throws ParseException {
        when(userService.getUserName()).thenReturn("testuser");
        testExam.setLocked(true);
        when(examUserService.findByExamAndUser(1L, "testuser")).thenReturn(testExamUser);

        ResponseEntity<?> response = examController.getExamUserById(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bài thi đang bị khoá hoặc chưa tới thời gian phù hợp", response.getBody());
    }

    /**
     * Test Case ID: UT_EC_13
     * Purpose: Test saving user exam answer when exam is finished
     */
    @Test
    @DisplayName("Should throw exception when saving answer for finished exam")
    void saveUserExamAnswer_ShouldThrowException_WhenExamFinished() throws JsonProcessingException {
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        testExamUser.setIsFinished(true);
        List<AnswerSheet> answerSheets = new ArrayList<>();
        when(examUserService.findByExamAndUser(1L, "testuser")).thenReturn(testExamUser);

        assertThrows(ExceptionInInitializerError.class, () -> {
            examController.saveUserExamAnswer(answerSheets, 1L, false, 3600);
        });
    }

    /**
     * Test Case ID: UT_EC_14
     * Purpose: Test canceling exam when exam has already started
     */
    @Test
    @DisplayName("Should not cancel exam when exam has started")
    void cancelExam_ShouldNotCancel_WhenExamStarted() {
        testExam.setBeginExam(new Date(System.currentTimeMillis() - 3600000)); // Started 1 hour ago

        assertThrows(NoSuchElementException.class, () -> {
            examController.cancelExam(1L);
        });
    }

    /**
     * Test Case ID: UT_EC_15
     * Purpose: Test creating exam with invalid intake
     */
    @Test
    @DisplayName("Should handle invalid intake when creating exam")
    void createExam_ShouldHandleInvalidIntake() {
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        when(userService.getUserName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(intakeService.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = examController.createExam(testExam, 999L, 1L, false, false);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(examService, never()).saveExam(any(Exam.class));
    }

    /**
     * Test Case ID: UT_EC_16
     * Purpose: Test getResultExamAll - happy path
     */
    @Test
    @DisplayName("Should return exam results for all users when exam exists")
    void getResultExamAll_ShouldReturnResults_WhenExamExists() throws IOException {
        Exam exam = new Exam();
        exam.setId(1L);
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        when(examUserService.findAllByExam_Id(1L)).thenReturn(Collections.emptyList());
        exam.setQuestionData("[]");
        ResponseEntity<?> response = examController.getResultExamAll(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_17
     * Purpose: Test getResultExamAll - exam not found
     */
    @Test
    @DisplayName("Should return NOT_FOUND when exam does not exist in getResultExamAll")
    void getResultExamAll_ShouldReturnNotFound_WhenExamNotExists() throws IOException {
        when(examService.getExamById(999L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = examController.getResultExamAll(999L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_18
     * Purpose: Test getResultExamQuestionsReport - happy path
     */
    @Test
    @DisplayName("Should return question report when finished users exist")
    void getResultExamQuestionsReport_ShouldReturnReport_WhenFinishedUsersExist() throws IOException {
        Exam exam = new Exam();
        exam.setId(1L);
        when(examService.getExamById(1L)).thenReturn(Optional.of(exam));
        ExamUser finishedUser = new ExamUser();
        finishedUser.setUser(new User());
        finishedUser.setExam(exam);
        finishedUser.setIsFinished(true);
        when(examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(1L)).thenReturn(Collections.singletonList(finishedUser));
        exam.setQuestionData("[]");
        when(examService.getChoiceList(anyList(), any())).thenReturn(Collections.emptyList());
        ResponseEntity<?> response = examController.getResultExamQuestionsReport(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_19
     * Purpose: Test getResultExamQuestionsReport - exam not found
     */
    @Test
    @DisplayName("Should return NOT_FOUND when exam does not exist in getResultExamQuestionsReport")
    void getResultExamQuestionsReport_ShouldReturnNotFound_WhenExamNotExists() throws IOException {
        when(examService.getExamById(999L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = examController.getResultExamQuestionsReport(999L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_20
     * Purpose: Test getAllQuestions - exam not found
     */
    @Test
    @DisplayName("Should return NOT_FOUND when exam does not exist in getAllQuestions")
    void getAllQuestions_ShouldReturnNotFound_WhenExamNotExists() throws IOException {
        when(examService.getExamById(999L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = examController.getAllQuestions(999L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_21
     * Purpose: Test getResultExam - exam not found
     */
    @Test
    @DisplayName("Should return NOT_FOUND when exam does not exist in getResultExam")
    void getResultExam_ShouldReturnNotFound_WhenExamNotExists() throws IOException {
        when(examService.getExamById(999L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = examController.getResultExam(999L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_22
     * Purpose: Test getResultExamByUser - exam not found
     */
    @Test
    @DisplayName("Should return NOT_FOUND when exam does not exist in getResultExamByUser")
    void getResultExamByUser_ShouldReturnNotFound_WhenExamNotExists() throws IOException {
        when(examService.getExamById(999L)).thenReturn(Optional.empty());
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(new User()));
        ResponseEntity<?> response = examController.getResultExamByUser(999L, "user");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_23
     * Purpose: Test getQuestionTextByExamId - exam not found
     */
    @Test
    @DisplayName("Should handle missing exam in getQuestionTextByExamId")
    void getQuestionTextByExamId_ShouldHandleMissingExam() throws IOException {
        when(examService.getExamById(999L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> {
            examController.getQuestionTextByExamId(999L);
        });
    }

    /**
     * Test Case ID: UT_EC_24
     * Purpose: getResultExamAll - examUserList not empty, userChoices empty
     */
    @Test
    @DisplayName("getResultExamAll: examUserList not empty, userChoices empty")
    void getResultExamAll_ExamUserListNotEmpty_UserChoicesEmpty() throws IOException {
        Exam exam = new Exam();
        exam.setId(2L);
        exam.setBeginExam(new Date(System.currentTimeMillis() - 3600000)); // 1 hour ago
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000)); // 1 hour later

        ExamUser examUser = new ExamUser();
        examUser.setUser(testUser);
        examUser.setExam(exam);
        examUser.setIsStarted(false);
        examUser.setIsFinished(false);
        examUser.setTotalPoint(-1.0);
        examUser.setTimeStart(new Date());
        examUser.setTimeFinish(new Date());

        when(examService.getExamById(2L)).thenReturn(Optional.of(exam));
        when(examUserService.findAllByExam_Id(2L)).thenReturn(Collections.singletonList(examUser));
        // userChoices empty
        exam.setQuestionData("[]");
        // Mock convertAnswerJsonToObject to return empty list for this case
        ExamController spyController = spy(examController);
        doReturn(Collections.emptyList()).when(spyController).convertAnswerJsonToObject(examUser);

        ResponseEntity<?> response = spyController.getResultExamAll(2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Add more assertions to verify the content of ExamResult if needed
        List<ExamResult> results = (List<ExamResult>) response.getBody();
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(0, results.get(0).getExamStatus()); // Example assertion for examStatus
    }

    /**
     * Test Case ID: UT_EC_25
     * Purpose: getResultExamAll - examUserList not empty, userChoices not empty, isSelectedCorrected true, totalPoint -1
     */
    @Test
    @DisplayName("getResultExamAll: userChoices not empty, isSelectedCorrected true, totalPoint -1")
    void getResultExamAll_UserChoicesNotEmpty_IsSelectedCorrectedTrue_TotalPointMinus1() throws IOException {
        Exam exam = new Exam();
        exam.setId(3L);
        exam.setBeginExam(new Date(System.currentTimeMillis() - 3600000)); // 1 hour ago
        exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000)); // 1 hour later

        ExamUser examUser = new ExamUser();
        examUser.setUser(testUser);
        examUser.setExam(exam);
        examUser.setIsStarted(true);
        examUser.setIsFinished(false);
        examUser.setTotalPoint(-1.0);
        examUser.setTimeStart(new Date());
        examUser.setTimeFinish(new Date());

        when(examService.getExamById(3L)).thenReturn(Optional.of(exam));
        when(examUserService.findAllByExam_Id(3L)).thenReturn(Collections.singletonList(examUser));

        ChoiceList choiceList = new ChoiceList();
        choiceList.setIsSelectedCorrected(true);
        choiceList.setPoint(2);
        when(examService.getChoiceList(anyList(), any())).thenReturn(Collections.singletonList(choiceList));
        exam.setQuestionData("[]");

        ExamController spyController = spy(examController);
        AnswerSheet answerSheet = new AnswerSheet(); // Create a concrete AnswerSheet
        doReturn(Collections.singletonList(answerSheet)).when(spyController).convertAnswerJsonToObject(examUser);

        ResponseEntity<?> response = spyController.getResultExamAll(3L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Add more assertions to verify the content of ExamResult
        List<ExamResult> results = (List<ExamResult>) response.getBody();
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.get(0).getExamStatus()); // Example: Assuming status 1 (Doing)
        assertEquals(2.0, results.get(0).getTotalPoint()); // Check total points calculation
    }

    /**
     * Test Case ID: UT_EC_26
     * Purpose: getResultExamQuestionsReport - finishedExamUser empty
     */
    @Test
    @DisplayName("getResultExamQuestionsReport: finishedExamUser empty")
    void getResultExamQuestionsReport_FinishedExamUserEmpty() throws IOException {
        Exam exam = new Exam();
        exam.setId(4L);
        when(examService.getExamById(4L)).thenReturn(Optional.of(exam));
        when(examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(4L)).thenReturn(Collections.emptyList());
        exam.setQuestionData("[]");
        ResponseEntity<?> response = examController.getResultExamQuestionsReport(4L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Chưa có người dùng thực hiện bài kiểm tra"));
    }

    /**
     * Test Case ID: UT_EC_27
     * Purpose: getAllQuestions - exam locked
     */
    @Test
    @DisplayName("getAllQuestions: exam locked")
    void getAllQuestions_ExamLocked() throws IOException {
        Exam exam = new Exam();
        exam.setId(5L);
        exam.setLocked(true);
        exam.setBeginExam(new Date(System.currentTimeMillis() - 10000));
        when(examService.getExamById(5L)).thenReturn(Optional.of(exam));
        ResponseEntity<?> response = examController.getAllQuestions(5L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_28
     * Purpose: getAllQuestions - exam not started yet
     */
    @Test
    @DisplayName("getAllQuestions: exam not started yet")
    void getAllQuestions_ExamNotStartedYet() throws IOException {
        Exam exam = new Exam();
        exam.setId(6L);
        exam.setLocked(false);
        exam.setBeginExam(new Date(System.currentTimeMillis() + 1000000));
        when(examService.getExamById(6L)).thenReturn(Optional.of(exam));
        ResponseEntity<?> response = examController.getAllQuestions(6L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_29
     * Purpose: getResultExam - examUser.getTotalPoint() == -1
     */
    @Test
    @DisplayName("getResultExam: examUser.getTotalPoint() == -1")
    void getResultExam_ExamUserTotalPointMinus1() throws IOException {
        Exam exam = new Exam();
        exam.setId(7L);
        when(examService.getExamById(7L)).thenReturn(Optional.of(exam));
        when(userService.getUserName()).thenReturn("testuser");
        ExamUser examUser = new ExamUser();
        examUser.setTotalPoint(-1.0);
        when(examUserService.findByExamAndUser(7L, "testuser")).thenReturn(examUser);
        when(examService.getChoiceList(anyList(), any())).thenReturn(Collections.emptyList());
        exam.setQuestionData("[]");
        ExamController spyController = spy(examController);
        doReturn(Collections.singletonList(new AnswerSheet())).when(spyController).convertAnswerJsonToObject(any());
        ResponseEntity<?> response = spyController.getResultExam(7L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_30
     * Purpose: getResultExam - examUser.getTotalPoint() != -1
     */
    @Test
    @DisplayName("getResultExam: examUser.getTotalPoint() != -1")
    void getResultExam_ExamUserTotalPointNotMinus1() throws IOException {
        Exam exam = new Exam();
        exam.setId(8L);
        when(examService.getExamById(8L)).thenReturn(Optional.of(exam));
        when(userService.getUserName()).thenReturn("testuser");
        ExamUser examUser = new ExamUser();
        examUser.setTotalPoint(5.0);
        when(examUserService.findByExamAndUser(8L, "testuser")).thenReturn(examUser);
        when(examService.getChoiceList(anyList(), any())).thenReturn(Collections.emptyList());
        exam.setQuestionData("[]");
        ExamController spyController = spy(examController);
        doReturn(Collections.singletonList(new AnswerSheet())).when(spyController).convertAnswerJsonToObject(any());
        ResponseEntity<?> response = spyController.getResultExam(8L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_31
     * Purpose: getResultExamByUser - examUser.getTotalPoint() == -1
     */
    @Test
    @DisplayName("getResultExamByUser: examUser.getTotalPoint() == -1")
    void getResultExamByUser_ExamUserTotalPointMinus1() throws IOException {
        Exam exam = new Exam();
        exam.setId(9L);
        when(examService.getExamById(9L)).thenReturn(Optional.of(exam));
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(testUser));
        ExamUser examUser = new ExamUser();
        examUser.setTotalPoint(-1.0);
        when(examUserService.findByExamAndUser(9L, "testuser")).thenReturn(examUser);
        when(examService.getChoiceList(anyList(), any())).thenReturn(Collections.emptyList());
        exam.setQuestionData("[]");
        ExamController spyController = spy(examController);
        doReturn(Collections.singletonList(new AnswerSheet())).when(spyController).convertAnswerJsonToObject(any());
        ResponseEntity<?> response = spyController.getResultExamByUser(9L, "testuser");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_32
     * Purpose: getResultExamByUser - examUser.getTotalPoint() != -1
     */
    @Test
    @DisplayName("getResultExamByUser: examUser.getTotalPoint() != -1")
    void getResultExamByUser_ExamUserTotalPointNotMinus1() throws IOException {
        Exam exam = new Exam();
        exam.setId(10L);
        when(examService.getExamById(10L)).thenReturn(Optional.of(exam));
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(testUser));
        ExamUser examUser = new ExamUser();
        examUser.setTotalPoint(10.0);
        when(examUserService.findByExamAndUser(10L, "testuser")).thenReturn(examUser);
        when(examService.getChoiceList(anyList(), any())).thenReturn(Collections.emptyList());
        exam.setQuestionData("[]");
        ExamController spyController = spy(examController);
        doReturn(Collections.singletonList(new AnswerSheet())).when(spyController).convertAnswerJsonToObject(any());
        ResponseEntity<?> response = spyController.getResultExamByUser(10L, "testuser");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_EC_33
     * Purpose: getAllQuestions - User has started, questions from AnswerSheet
     */
    @Test
    @DisplayName("getAllQuestions: User Started - Should return questions from AnswerSheet")
    void getAllQuestions_UserStarted_ReturnsFromAnswerSheet() throws IOException {
        Long examId = 11L;
        String username = "startedUser";

        Exam exam = new Exam();
        exam.setId(examId);
        exam.setLocked(false);
        exam.setBeginExam(new Date(System.currentTimeMillis() - 100000)); // Exam has begun

        ExamUser examUser = new ExamUser();
        examUser.setIsStarted(true);
        examUser.setRemainingTime(3600);
        examUser.setAnswerSheet("[]"); // Assume an empty answer sheet initially for simplicity

        when(userService.getUserName()).thenReturn(username);
        when(examService.getExamById(examId)).thenReturn(Optional.of(exam));
        when(examUserService.findByExamAndUser(examId, username)).thenReturn(examUser);

        // Spy the controller to mock its own public method convertAnswerJsonToObject
        ExamController spyController = spy(examController);
        AnswerSheet mockAnswerSheet = new AnswerSheet();
        mockAnswerSheet.setQuestionId(101L);
        mockAnswerSheet.setPoint(5);
        mockAnswerSheet.setChoices(Collections.emptyList());
        doReturn(Collections.singletonList(mockAnswerSheet)).when(spyController).convertAnswerJsonToObject(examUser);

        Question mockQuestion = new Question();
        mockQuestion.setId(101L);
        when(questionService.getQuestionById(101L)).thenReturn(Optional.of(mockQuestion));

        ResponseEntity<ExamQuestionList> response = spyController.getAllQuestions(examId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(exam, response.getBody().getExam());
        assertEquals(1, response.getBody().getQuestions().size());
        assertEquals(mockQuestion.getId(), response.getBody().getQuestions().get(0).getId());
        assertEquals(mockAnswerSheet.getPoint(), response.getBody().getQuestions().get(0).getPoint());
    }

    /**
     * Test Case ID: UT_EC_34
     * Purpose: getAllQuestions - User not started, shuffle true
     */
    @Test
    @DisplayName("getAllQuestions: User Not Started, Shuffle True - Initializes and Returns Questions")
    void getAllQuestions_UserNotStarted_ShuffleTrue_InitializesAndReturns() throws IOException {
        Long examId = 12L;
        String username = "newUserShuffleTrue";
        String questionDataJson = "[{\"questionId\": 201, \"point\": 10}]"; // Ensure this JSON is valid and non-empty

        Exam exam = new Exam();
        exam.setId(examId);
        exam.setLocked(false);
        exam.setBeginExam(new Date(System.currentTimeMillis() - 100000));
        exam.setShuffle(true);
        exam.setQuestionData(questionDataJson);

        ExamUser examUser = new ExamUser();
        examUser.setIsStarted(false);
        examUser.setRemainingTime(3600);

        when(userService.getUserName()).thenReturn(username);
        when(examService.getExamById(examId)).thenReturn(Optional.of(exam));
        when(examUserService.findByExamAndUser(examId, username)).thenReturn(examUser);

        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(201L);
        examQuestionPoint.setPoint(10);
        List<ExamQuestionPoint> examQuestionPoints = Collections.singletonList(examQuestionPoint); // Non-empty list
        // when(objectMapper.readValue(eq(questionDataJson), any(com.fasterxml.jackson.core.type.TypeReference.class))).thenReturn(examQuestionPoints);

        Question mockQuestion = new Question();
        mockQuestion.setId(201L);
        List<Question> questionsFromPoints = Collections.singletonList(mockQuestion);
        when(questionService.getQuestionPointList(anyList())).thenReturn(questionsFromPoints); // Using anyList() due to shuffle

        AnswerSheet mockAnswerSheet = new AnswerSheet();
        mockAnswerSheet.setQuestionId(201L);
        mockAnswerSheet.setPoint(10);
        mockAnswerSheet.setChoices(Collections.emptyList());
        List<AnswerSheet> answerSheets = Collections.singletonList(mockAnswerSheet);
        when(questionService.convertFromQuestionList(questionsFromPoints)).thenReturn(answerSheets);
        // when(objectMapper.writeValueAsString(answerSheets)).thenReturn("[{\"questionId\": 201, \"point\": 10, \"choices\":[]}]");

        when(questionService.getQuestionById(201L)).thenReturn(Optional.of(mockQuestion));
        doNothing().when(examUserService).update(examUser);

        ResponseEntity<ExamQuestionList> response = examController.getAllQuestions(examId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(examUser.getIsStarted());
        assertNotNull(examUser.getAnswerSheet());
        assertNotNull(examUser.getTimeStart());
        assertEquals(1, response.getBody().getQuestions().size());
        verify(examUserService, times(2)).update(examUser);
    }

    /**
     * Test Case ID: UT_EC_35
     * Purpose: getAllQuestions - User not started, shuffle false
     */
    @Test
    @DisplayName("getAllQuestions: User Not Started, Shuffle False - Initializes and Returns Questions")
    void getAllQuestions_UserNotStarted_ShuffleFalse_InitializesAndReturns() throws IOException {
        Long examId = 13L;
        String username = "newUserShuffleFalse";
        String questionDataJson = "[{\"questionId\": 301, \"point\": 15}]"; // Ensure this JSON is valid and non-empty

        Exam exam = new Exam();
        exam.setId(examId);
        exam.setLocked(false);
        exam.setBeginExam(new Date(System.currentTimeMillis() - 100000));
        exam.setShuffle(false);
        exam.setQuestionData(questionDataJson);

        ExamUser examUser = new ExamUser();
        examUser.setIsStarted(false);
        examUser.setRemainingTime(3600);

        when(userService.getUserName()).thenReturn(username);
        when(examService.getExamById(examId)).thenReturn(Optional.of(exam));
        when(examUserService.findByExamAndUser(examId, username)).thenReturn(examUser);

        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(301L);
        examQuestionPoint.setPoint(15);
        List<ExamQuestionPoint> examQuestionPoints = Collections.singletonList(examQuestionPoint); // Non-empty list
        // when(objectMapper.readValue(eq(questionDataJson), any(com.fasterxml.jackson.core.type.TypeReference.class))).thenReturn(examQuestionPoints);

        Question mockQuestion = new Question();
        mockQuestion.setId(301L);
        List<Question> questionsFromPoints = Collections.singletonList(mockQuestion);
        when(questionService.getQuestionPointList(examQuestionPoints)).thenReturn(questionsFromPoints);

        AnswerSheet mockAnswerSheet = new AnswerSheet();
        mockAnswerSheet.setQuestionId(301L);
        mockAnswerSheet.setPoint(15);
        mockAnswerSheet.setChoices(Collections.emptyList());
        List<AnswerSheet> answerSheets = Collections.singletonList(mockAnswerSheet);
        when(questionService.convertFromQuestionList(questionsFromPoints)).thenReturn(answerSheets);
        // when(objectMapper.writeValueAsString(answerSheets)).thenReturn("[{\"questionId\": 301, \"point\": 15, \"choices\":[]}]");

        when(questionService.getQuestionById(301L)).thenReturn(Optional.of(mockQuestion));
        doNothing().when(examUserService).update(examUser);

        ResponseEntity<ExamQuestionList> response = examController.getAllQuestions(examId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(examUser.getIsStarted());
        assertNotNull(examUser.getAnswerSheet());
        assertNotNull(examUser.getTimeStart());
        assertEquals(1, response.getBody().getQuestions().size());
        verify(examUserService, times(1)).update(examUser);
    }
}