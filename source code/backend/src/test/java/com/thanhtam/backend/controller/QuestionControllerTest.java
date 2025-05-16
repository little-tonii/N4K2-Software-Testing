package com.thanhtam.backend.controller;

import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.dto.ServiceResult;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.service.*;
import com.thanhtam.backend.ultilities.EQTypeCode;
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
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private PartService partService;

    @Mock
    private QuestionTypeService questionTypeService;

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private QuestionController questionController;

    private User testUser;
    private Role adminRole;
    private Role lecturerRole;
    private Pageable pageable;
    private Question testQuestion;
    private Part testPart;
    private QuestionType testQuestionType;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        
        lecturerRole = new Role();
        lecturerRole.setName(ERole.ROLE_LECTURER);

        pageable = PageRequest.of(0, 10);

        testQuestion = new Question();
        testQuestion.setId(1L);
        testQuestion.setQuestionText("Sample Question");
        
        testPart = new Part();
        testPart.setId(1L);
        testPart.setName("Sample Part");

        testQuestionType = new QuestionType();
        testQuestionType.setId(1L);
        testQuestionType.setTypeCode(EQTypeCode.MC);
        testQuestionType.setDescription("Multiple Choice");

        // Common security context setup
        // when(securityContext.getAuthentication()).thenReturn(authentication);
        // SecurityContextHolder.setContext(securityContext);
        // when(authentication.getName()).thenReturn(testUser.getUsername());
        // when(userService.getUserByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
    }

    // Basic helper to setup user roles
    private void setupUserRoles(User user, ERole... roles) {
        Set<Role> userRoles = new HashSet<>();
        for (ERole eRole : roles) {
            if (eRole == ERole.ROLE_ADMIN) {
                userRoles.add(adminRole);
            } else if (eRole == ERole.ROLE_LECTURER) {
                userRoles.add(lecturerRole);
            }
            // Add other roles if needed
        }
        user.setRoles(userRoles);
    }
    
    // Helper to mock security context for a given user
    private void mockSecurityContext(User user) {
        // when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext); // Essential for @PreAuthorize
        // when(authentication.getName()).thenReturn(user.getUsername());
        when(userService.getUserName()).thenReturn(user.getUsername()); // For direct calls to userService.getUserName()
        when(userService.getUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    /**
     * Test Case ID: UT_QBM_01_001
     * Purpose: Test get all questions successfully
     * Prerequisites:
     *  - questionService.getQuestionList returns a list of questions
     * Test Steps:
     *  1. Mock questionService.getQuestionList()
     *  2. Call controller.getAllQuestion()
     * Expected Results:
     *  - Returns 200 OK
     *  - Body contains the list of questions
     */
    @Test
    @DisplayName("Get All Questions - Success")
    void getAllQuestion_Success() {
        // Arrange
        List<Question> questions = Arrays.asList(testQuestion, new Question());
        when(questionService.getQuestionList()).thenReturn(questions);
        // Note: @PreAuthorize is handled by Spring Security context, not directly tested here unless we mock roles specifically for access denial.
        // For success cases, we assume the user has the required role.

        // Act
        ResponseEntity<ServiceResult> response = questionController.getAllQuestion();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode());
        assertEquals("Get question bank successfully!", response.getBody().getMessage());
        assertEquals(questions, response.getBody().getData());
        verify(questionService).getQuestionList();
    }

    /**
     * Test Case ID: UT_QBM_01_002
     * Purpose: Test get all questions when list is empty
     * Prerequisites:
     *  - questionService.getQuestionList returns an empty list
     * Test Steps:
     *  1. Mock questionService.getQuestionList() to return Collections.emptyList()
     *  2. Call controller.getAllQuestion()
     * Expected Results:
     *  - Returns 200 OK
     *  - Body contains an empty list
     */
    @Test
    @DisplayName("Get All Questions - Empty List")
    void getAllQuestion_EmptyList() {
        // Arrange
        when(questionService.getQuestionList()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ServiceResult> response = questionController.getAllQuestion();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode());
        assertEquals("Get question bank successfully!", response.getBody().getMessage());
        assertTrue(((List<?>) response.getBody().getData()).isEmpty());
        verify(questionService).getQuestionList();
    }

    /**
     * Test Case ID: UT_QBM_03_001
     * Purpose: Test get question by ID successfully
     * Prerequisites:
     *  - questionService.getQuestionById returns an Optional with a question
     * Test Steps:
     *  1. Mock questionService.getQuestionById()
     *  2. Call controller.getQuestionById()
     * Expected Results:
     *  - Returns 200 OK
     *  - Body contains the question
     */
    @Test
    @DisplayName("Get Question By ID - Success")
    void getQuestionById_Success() {
        // Arrange
        Long questionId = 1L;
        when(questionService.getQuestionById(questionId)).thenReturn(Optional.of(testQuestion));

        // Act
        ResponseEntity<?> response = questionController.getQuestionById(questionId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testQuestion, response.getBody());
        verify(questionService).getQuestionById(questionId);
    }

    /**
     * Test Case ID: UT_QBM_03_002
     * Purpose: Test get question by ID when question is not found
     * Prerequisites:
     *  - questionService.getQuestionById returns an empty Optional
     * Test Steps:
     *  1. Mock questionService.getQuestionById() to return Optional.empty()
     *  2. Call controller.getQuestionById()
     * Expected Results:
     *  - Returns 200 OK (controller wraps NOT_FOUND in ServiceResult)
     *  - Body is a ServiceResult with NOT_FOUND status
     */
    @Test
    @DisplayName("Get Question By ID - Not Found")
    void getQuestionById_NotFound() {
        // Arrange
        Long questionId = 99L;
        when(questionService.getQuestionById(questionId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = questionController.getQuestionById(questionId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult serviceResult = (ServiceResult) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), serviceResult.getStatusCode());
        assertEquals("Not found with id: " + questionId, serviceResult.getMessage());
        assertNull(serviceResult.getData());
        verify(questionService).getQuestionById(questionId);
    }

    /**
     * Test Case ID: UT_QBM_01_003
     * Purpose: Test get questions by part when partId is 0 and user is Admin
     * Prerequisites:
     *  - User is Admin
     *  - questionService.findAllQuestions returns a page of questions
     * Test Steps:
     *  1. Setup Admin user and mock security context
     *  2. Mock roleService.findByName for ADMIN role
     *  3. Mock questionService.findAllQuestions()
     *  4. Call controller.getQuestionsByPart() with partId = 0
     * Expected Results:
     *  - Returns a PageResult with all questions
     */
    @Test
    @DisplayName("Get Questions By Part - Admin, All Parts (partId=0)")
    void getQuestionsByPart_Admin_AllParts() {
        // Arrange
        setupUserRoles(testUser, ERole.ROLE_ADMIN);
        mockSecurityContext(testUser);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        Page<Question> expectedPage = new PageImpl<>(Collections.singletonList(testQuestion), pageable, 1);
        when(questionService.findAllQuestions(pageable)).thenReturn(expectedPage);

        // Act
        PageResult result = questionController.getQuestionsByPart(pageable, 0L);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage.getContent(), result.getData());
        assertEquals(expectedPage.getTotalElements(), result.getPaginationDetails().getTotalCount());
        verify(questionService).findAllQuestions(pageable);
    }

    /**
     * Test Case ID: UT_QBM_01_004
     * Purpose: Test get questions by part when partId is 0 and user is not Admin (Lecturer)
     * Prerequisites:
     *  - User is Lecturer (not Admin)
     *  - questionService.findQuestionsByCreatedBy_Username returns a page of questions
     * Test Steps:
     *  1. Setup Lecturer user and mock security context
     *  2. Mock roleService.findByName for ADMIN role (to determine not-admin)
     *  3. Mock questionService.findQuestionsByCreatedBy_Username()
     *  4. Call controller.getQuestionsByPart() with partId = 0
     * Expected Results:
     *  - Returns a PageResult with questions created by the user
     */
    @Test
    @DisplayName("Get Questions By Part - Lecturer, All Parts (partId=0) - Created By User")
    void getQuestionsByPart_Lecturer_AllParts_CreatedByUser() {
        // Arrange
        setupUserRoles(testUser, ERole.ROLE_LECTURER); // Not an admin
        mockSecurityContext(testUser);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole)); // For the isAdmin check

        Page<Question> expectedPage = new PageImpl<>(Collections.singletonList(testQuestion), pageable, 1);
        when(questionService.findQuestionsByCreatedBy_Username(pageable, testUser.getUsername())).thenReturn(expectedPage);

        // Act
        PageResult result = questionController.getQuestionsByPart(pageable, 0L);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage.getContent(), result.getData());
        verify(questionService).findQuestionsByCreatedBy_Username(pageable, testUser.getUsername());
    }

    /**
     * Test Case ID: UT_QBM_01_005
     * Purpose: Test get questions by part when partId is specified and user is Admin
     * Prerequisites:
     *  - User is Admin
     *  - partService.findPartById returns a Part
     *  - questionService.findQuestionsByPart returns a page of questions
     * Test Steps:
     *  1. Setup Admin user and mock security context
     *  2. Mock roleService.findByName for ADMIN role
     *  3. Mock partService.findPartById()
     *  4. Mock questionService.findQuestionsByPart()
     *  5. Call controller.getQuestionsByPart() with specific partId
     * Expected Results:
     *  - Returns a PageResult with questions for the specified part
     */
    @Test
    @DisplayName("Get Questions By Part - Admin, Specific Part")
    void getQuestionsByPart_Admin_SpecificPart() {
        // Arrange
        Long specificPartId = 1L;
        setupUserRoles(testUser, ERole.ROLE_ADMIN);
        mockSecurityContext(testUser);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(partService.findPartById(specificPartId)).thenReturn(Optional.of(testPart));

        Page<Question> expectedPage = new PageImpl<>(Collections.singletonList(testQuestion), pageable, 1);
        when(questionService.findQuestionsByPart(pageable, testPart)).thenReturn(expectedPage);

        // Act
        PageResult result = questionController.getQuestionsByPart(pageable, specificPartId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage.getContent(), result.getData());
        verify(partService).findPartById(specificPartId);
        verify(questionService).findQuestionsByPart(pageable, testPart);
    }

    /**
     * Test Case ID: UT_QBM_01_006
     * Purpose: Test get questions by part when partId is specified and user is not Admin (Lecturer)
     * Prerequisites:
     *  - User is Lecturer
     *  - questionService.findQuestionsByPart_IdAndCreatedBy_Username returns a page of questions
     * Test Steps:
     *  1. Setup Lecturer user and mock security context
     *  2. Mock roleService.findByName for ADMIN role
     *  3. Mock questionService.findQuestionsByPart_IdAndCreatedBy_Username()
     *  4. Call controller.getQuestionsByPart() with specific partId
     * Expected Results:
     *  - Returns a PageResult with questions for the specified part created by the user
     */
    @Test
    @DisplayName("Get Questions By Part - Lecturer, Specific Part - Created By User")
    void getQuestionsByPart_Lecturer_SpecificPart_CreatedByUser() {
        // Arrange
        Long specificPartId = 1L;
        setupUserRoles(testUser, ERole.ROLE_LECTURER);
        mockSecurityContext(testUser);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        Page<Question> expectedPage = new PageImpl<>(Collections.singletonList(testQuestion), pageable, 1);
        when(questionService.findQuestionsByPart_IdAndCreatedBy_Username(pageable, specificPartId, testUser.getUsername()))
            .thenReturn(expectedPage);

        // Act
        PageResult result = questionController.getQuestionsByPart(pageable, specificPartId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage.getContent(), result.getData());
        verify(questionService).findQuestionsByPart_IdAndCreatedBy_Username(pageable, specificPartId, testUser.getUsername());
    }
    
    /**
     * Test Case ID: UT_QBM_01_007
     * Purpose: Test get questions by part when partId is specified, user is Admin, but part not found
     * Prerequisites:
     *  - User is Admin
     *  - partService.findPartById returns Optional.empty()
     * Test Steps:
     *  1. Setup Admin user and mock security context
     *  2. Mock roleService.findByName for ADMIN role
     *  3. Mock partService.findPartById() to return Optional.empty()
     *  4. Call controller.getQuestionsByPart() with specific partId
     * Expected Results:
     *  - Throws NoSuchElementException because of .get() on empty Optional for Part
     */
    @Test
    @DisplayName("Get Questions By Part - Admin, Specific Part - Part Not Found")
    void getQuestionsByPart_Admin_SpecificPart_PartNotFound() {
        // Arrange
        Long nonExistentPartId = 99L;
        setupUserRoles(testUser, ERole.ROLE_ADMIN);
        mockSecurityContext(testUser);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(partService.findPartById(nonExistentPartId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            questionController.getQuestionsByPart(pageable, nonExistentPartId);
        });
        verify(partService).findPartById(nonExistentPartId);
        verify(questionService, never()).findQuestionsByPart(any(Pageable.class), any(Part.class));
    }

    /**
     * Test Case ID: UT_QBM_01_008
     * Purpose: Test get questions by part (not deleted) when user is Admin
     * Prerequisites:
     *  - User is Admin
     *  - partService.findPartById returns a Part
     *  - questionService.findQuestionsByPartAndDeletedFalse returns a page of questions
     * Test Steps:
     *  1. Setup Admin user and mock security context
     *  2. Mock roleService.findByName for ADMIN role
     *  3. Mock partService.findPartById()
     *  4. Mock questionService.findQuestionsByPartAndDeletedFalse()
     *  5. Call controller.getQuestionsByPartNotDeleted()
     * Expected Results:
     *  - Returns a PageResult with non-deleted questions for the specified part
     */
    @Test
    @DisplayName("Get Questions By Part (Not Deleted) - Admin, Specific Part")
    void getQuestionsByPartNotDeleted_Admin_SpecificPart() {
        // Arrange
        Long specificPartId = 1L;
        setupUserRoles(testUser, ERole.ROLE_ADMIN);
        mockSecurityContext(testUser);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(partService.findPartById(specificPartId)).thenReturn(Optional.of(testPart));

        Page<Question> expectedPage = new PageImpl<>(Collections.singletonList(testQuestion), pageable, 1);
        when(questionService.findQuestionsByPartAndDeletedFalse(pageable, testPart)).thenReturn(expectedPage);

        // Act
        PageResult result = questionController.getQuestionsByPartNotDeleted(pageable, specificPartId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage.getContent(), result.getData());
        assertEquals(expectedPage.getTotalElements(), result.getPaginationDetails().getTotalCount());
        verify(partService).findPartById(specificPartId);
        verify(questionService).findQuestionsByPartAndDeletedFalse(pageable, testPart);
    }

    /**
     * Test Case ID: UT_QBM_01_009
     * Purpose: Test get questions by part (not deleted) when user is not Admin (Lecturer)
     * Prerequisites:
     *  - User is Lecturer
     *  - questionService.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse returns a page of questions
     * Test Steps:
     *  1. Setup Lecturer user and mock security context
     *  2. Mock roleService.findByName for ADMIN role
     *  3. Mock questionService.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse()
     *  4. Call controller.getQuestionsByPartNotDeleted()
     * Expected Results:
     *  - Returns a PageResult with non-deleted questions for the specified part created by the user
     */
    @Test
    @DisplayName("Get Questions By Part (Not Deleted) - Lecturer, Specific Part - Created By User")
    void getQuestionsByPartNotDeleted_Lecturer_SpecificPart_CreatedByUser() {
        // Arrange
        Long specificPartId = 1L;
        setupUserRoles(testUser, ERole.ROLE_LECTURER);
        mockSecurityContext(testUser);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        Page<Question> expectedPage = new PageImpl<>(Collections.singletonList(testQuestion), pageable, 1);
        when(questionService.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(pageable, specificPartId, testUser.getUsername()))
            .thenReturn(expectedPage);

        // Act
        PageResult result = questionController.getQuestionsByPartNotDeleted(pageable, specificPartId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage.getContent(), result.getData());
        assertEquals(expectedPage.getTotalElements(), result.getPaginationDetails().getTotalCount());
        verify(questionService).findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(pageable, specificPartId, testUser.getUsername());
    }

    /**
     * Test Case ID: UT_QBM_01_010
     * Purpose: Test get questions by part (not deleted) when partId is specified, user is Admin, but part not found
     * Prerequisites:
     *  - User is Admin
     *  - partService.findPartById returns Optional.empty()
     * Test Steps:
     *  1. Setup Admin user and mock security context
     *  2. Mock roleService.findByName for ADMIN role
     *  3. Mock partService.findPartById() to return Optional.empty()
     *  4. Call controller.getQuestionsByPartNotDeleted() with specific partId
     * Expected Results:
     *  - Throws NoSuchElementException because of .get() on empty Optional for Part
     */
    @Test
    @DisplayName("Get Questions By Part (Not Deleted) - Admin, Specific Part - Part Not Found")
    void getQuestionsByPartNotDeleted_Admin_SpecificPart_PartNotFound() {
        // Arrange
        Long nonExistentPartId = 99L;
        setupUserRoles(testUser, ERole.ROLE_ADMIN);
        mockSecurityContext(testUser);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(partService.findPartById(nonExistentPartId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            questionController.getQuestionsByPartNotDeleted(pageable, nonExistentPartId);
        });
        verify(partService).findPartById(nonExistentPartId);
        verify(questionService, never()).findQuestionsByPartAndDeletedFalse(any(Pageable.class), any(Part.class));
    }

    /**
     * Test Case ID: UT_QBM_01_011
     * Purpose: Test get questions by question type successfully
     * Prerequisites:
     *  - questionTypeService.existsById returns true
     *  - questionTypeService.getQuestionTypeById returns a QuestionType
     *  - questionService.getQuestionByQuestionType returns a list of questions
     * Test Steps:
     *  1. Mock services as per prerequisites
     *  2. Call controller.getQuestionByQuestionType()
     * Expected Results:
     *  - Returns 200 OK
     *  - Body is a ServiceResult with the list of questions
     */
    @Test
    @DisplayName("Get Question By Question Type - Success")
    void getQuestionByQuestionType_Success() {
        // Arrange
        Long typeId = 1L;
        List<Question> expectedQuestions = Collections.singletonList(testQuestion);

        when(questionTypeService.existsById(typeId)).thenReturn(true);
        when(questionTypeService.getQuestionTypeById(typeId)).thenReturn(Optional.of(testQuestionType));
        when(questionService.getQuestionByQuestionType(testQuestionType)).thenReturn(expectedQuestions);

        // Act
        ResponseEntity<?> response = questionController.getQuestionByQuestionType(typeId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult serviceResult = (ServiceResult) response.getBody();
        assertEquals(HttpStatus.OK.value(), serviceResult.getStatusCode());
        assertEquals("Get question list with question type id: " + typeId, serviceResult.getMessage());
        assertEquals(expectedQuestions, serviceResult.getData());
        verify(questionTypeService).existsById(typeId);
        verify(questionTypeService).getQuestionTypeById(typeId);
        verify(questionService).getQuestionByQuestionType(testQuestionType);
    }

    /**
     * Test Case ID: UT_QBM_01_012
     * Purpose: Test get questions by question type when type does not exist
     * Prerequisites:
     *  - questionTypeService.existsById returns false
     * Test Steps:
     *  1. Mock questionTypeService.existsById() to return false
     *  2. Call controller.getQuestionByQuestionType()
     * Expected Results:
     *  - Returns 200 OK
     *  - Body is a ServiceResult with NOT_FOUND status
     */
    @Test
    @DisplayName("Get Question By Question Type - Type Not Found (existsById false)")
    void getQuestionByQuestionType_TypeNotFound_ExistsFalse() {
        // Arrange
        Long nonExistentTypeId = 99L;
        when(questionTypeService.existsById(nonExistentTypeId)).thenReturn(false);

        // Act
        ResponseEntity<?> response = questionController.getQuestionByQuestionType(nonExistentTypeId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult serviceResult = (ServiceResult) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), serviceResult.getStatusCode());
        assertEquals("Not found question type with id: " + nonExistentTypeId, serviceResult.getMessage());
        assertNull(serviceResult.getData());
        verify(questionTypeService).existsById(nonExistentTypeId);
        verify(questionTypeService, never()).getQuestionTypeById(anyLong());
        verify(questionService, never()).getQuestionByQuestionType(any(QuestionType.class));
    }

    /**
     * Test Case ID: UT_QBM_01_013
     * Purpose: Test get questions by question type when type exists but service returns empty Optional (defensive)
     * Prerequisites:
     *  - questionTypeService.existsById returns true
     *  - questionTypeService.getQuestionTypeById returns Optional.empty()
     * Test Steps:
     *  1. Mock services as per prerequisites
     *  2. Call controller.getQuestionByQuestionType()
     * Expected Results:
     *  - Throws NoSuchElementException due to .get() on empty Optional for QuestionType
     */
    @Test
    @DisplayName("Get Question By Question Type - Type Exists, Service Returns Empty (Defensive)")
    void getQuestionByQuestionType_TypeExists_ServiceReturnsEmpty() {
        // Arrange
        Long typeId = 1L;
        when(questionTypeService.existsById(typeId)).thenReturn(true);
        when(questionTypeService.getQuestionTypeById(typeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            questionController.getQuestionByQuestionType(typeId);
        });
        verify(questionTypeService).existsById(typeId);
        verify(questionTypeService).getQuestionTypeById(typeId);
        verify(questionService, never()).getQuestionByQuestionType(any(QuestionType.class));
    }

    /**
     * Test Case ID: UT_QBM_02_001
     * Purpose: Test create question successfully
     * Prerequisites:
     *  - Services (questionType, part, question) are mocked to return valid objects
     * Test Steps:
     *  1. Mock services for finding QuestionType, Part, and saving/retrieving Question
     *  2. Call controller.createQuestion()
     * Expected Results:
     *  - Returns the created Question object
     *  - Question object has correct Part, QuestionType, and deleted status set
     */
    @Test
    @DisplayName("Create Question - Success")
    void createQuestion_Success() {
        // Arrange
        Question newQuestion = new Question();
        newQuestion.setQuestionText("Newly created question");
        String questionTypeCodeStr = "MC";
        Long partId = 1L;

        when(questionTypeService.getQuestionTypeByCode(EQTypeCode.MC)).thenReturn(Optional.of(testQuestionType));
        when(partService.findPartById(partId)).thenReturn(Optional.of(testPart));
        // Mock the save to do nothing, as we verify setters and the final get call
        doNothing().when(questionService).save(any(Question.class)); 
        // When getQuestionById is called with the (yet unknown) ID of the newQ, return it.
        // We use an ArgumentCaptor on save to get the question with its ID set by the service (if it were real).
        // For simplicity here, we assume the passed 'newQuestion' has its ID set by the save operation implicitly.
        // A more elaborate test might involve capturing the argument to save, setting an ID, and then returning it for getQuestionById.
        // However, the controller itself calls getQuestionById(question.getId()) on the SAME question object it passed to save.
        // So, if 'save' hypothetically sets the ID on 'newQuestion', then getQuestionById(newQuestion.getId()) would be called.
        // Let's assume 'newQuestion' will have an ID (e.g., 2L) after save for the getQuestionById mock.
        newQuestion.setId(2L); // Simulate ID being set after save
        when(questionService.getQuestionById(2L)).thenReturn(Optional.of(newQuestion)); 

        // Act
        Question createdQuestion = questionController.createQuestion(newQuestion, questionTypeCodeStr, partId);

        // Assert
        assertNotNull(createdQuestion);
        assertEquals(newQuestion.getQuestionText(), createdQuestion.getQuestionText());
        assertEquals(testQuestionType, createdQuestion.getQuestionType());
        assertEquals(testPart, createdQuestion.getPart());
        assertFalse(createdQuestion.isDeleted()); // Check deleted status is set to false
        assertEquals(2L, createdQuestion.getId()); // Verify the ID we simulated

        verify(questionTypeService).getQuestionTypeByCode(EQTypeCode.MC);
        verify(partService).findPartById(partId);
        verify(questionService).save(newQuestion); // Verify that the newQuestion object was passed to save
        verify(questionService).getQuestionById(2L); // Verify retrieval after save
    }

    /**
     * Test Case ID: UT_QBM_02_002
     * Purpose: Test create question when QuestionType is not found
     * Prerequisites:
     *  - questionTypeService.getQuestionTypeByCode returns Optional.empty()
     * Test Steps:
     *  1. Mock questionTypeService to return empty Optional for type code
     *  2. Call controller.createQuestion()
     * Expected Results:
     *  - Throws NoSuchElementException
     */
    @Test
    @DisplayName("Create Question - QuestionType Not Found")
    void createQuestion_QuestionTypeNotFound() {
        // Arrange
        Question newQuestion = new Question();
        String questionTypeCodeStr = "INVALID_TYPE";
        Long partId = 1L;

        when(questionTypeService.getQuestionTypeByCode(any(EQTypeCode.class))).thenReturn(Optional.empty());
        // Need to handle EQTypeCode.valueOf() potentially throwing IllegalArgumentException for "INVALID_TYPE"
        // For this test, let's assume a valid EQTypeCode that simply isn't found by the service.
        String validButNotFoundTypeCodeStr = "TF"; 

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            questionController.createQuestion(newQuestion, validButNotFoundTypeCodeStr, partId);
        });
        verify(questionTypeService).getQuestionTypeByCode(EQTypeCode.TF);
        verify(partService, never()).findPartById(anyLong());
        verify(questionService, never()).save(any(Question.class));
    }

    /**
     * Test Case ID: UT_QBM_02_003
     * Purpose: Test create question when Part is not found
     * Prerequisites:
     *  - questionTypeService returns valid QuestionType
     *  - partService.findPartById returns Optional.empty()
     * Test Steps:
     *  1. Mock partService to return empty Optional for part ID
     *  2. Call controller.createQuestion()
     * Expected Results:
     *  - Throws NoSuchElementException
     */
    @Test
    @DisplayName("Create Question - Part Not Found")
    void createQuestion_PartNotFound() {
        // Arrange
        Question newQuestion = new Question();
        String questionTypeCodeStr = "MC";
        Long nonExistentPartId = 99L;

        when(questionTypeService.getQuestionTypeByCode(EQTypeCode.MC)).thenReturn(Optional.of(testQuestionType));
        when(partService.findPartById(nonExistentPartId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            questionController.createQuestion(newQuestion, questionTypeCodeStr, nonExistentPartId);
        });
        verify(questionTypeService).getQuestionTypeByCode(EQTypeCode.MC);
        verify(partService).findPartById(nonExistentPartId);
        verify(questionService, never()).save(any(Question.class));
    }
    
    /**
     * Test Case ID: UT_QBM_02_004
     * Purpose: Test create question when retrieval after save fails (defensive)
     * Prerequisites:
     *  - All services for creation are valid, but questionService.getQuestionById after save returns empty
     * Test Steps:
     *  1. Mock services for successful creation path up to save
     *  2. Mock questionService.getQuestionById (called after save) to return Optional.empty()
     *  3. Call controller.createQuestion()
     * Expected Results:
     *  - Throws NoSuchElementException
     */
    @Test
    @DisplayName("Create Question - Retrieval After Save Fails")
    void createQuestion_RetrievalAfterSaveFails() {
        // Arrange
        Question newQuestion = new Question();
        newQuestion.setQuestionText("Another new question");
        newQuestion.setId(3L); // Assume ID is set by save, used for subsequent get
        String questionTypeCodeStr = "MC";
        Long partId = 1L;

        when(questionTypeService.getQuestionTypeByCode(EQTypeCode.MC)).thenReturn(Optional.of(testQuestionType));
        when(partService.findPartById(partId)).thenReturn(Optional.of(testPart));
        doNothing().when(questionService).save(newQuestion);
        when(questionService.getQuestionById(newQuestion.getId())).thenReturn(Optional.empty()); // Fails here

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            questionController.createQuestion(newQuestion, questionTypeCodeStr, partId);
        });
        verify(questionService).save(newQuestion);
        verify(questionService).getQuestionById(newQuestion.getId());
    }

    /**
     * Test Case ID: UT_QBM_02_005
     * Purpose: Test create question with an invalid questionType string
     * Prerequisites: None beyond controller setup
     * Test Steps:
     *  1. Call controller.createQuestion() with an invalid questionType string
     * Expected Results:
     *  - Throws IllegalArgumentException when EQTypeCode.valueOf() fails
     */
    @Test
    @DisplayName("Create Question - Invalid QuestionType String")
    void createQuestion_InvalidQuestionTypeString() {
        // Arrange
        Question newQuestion = new Question();
        String invalidQuestionTypeCodeStr = "VERY_INVALID_TYPE_STRING";
        Long partId = 1L;

        // No mocking needed for services as it should fail before calling them.

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            questionController.createQuestion(newQuestion, invalidQuestionTypeCodeStr, partId);
        });
        verify(questionTypeService, never()).getQuestionTypeByCode(any(EQTypeCode.class));
        verify(partService, never()).findPartById(anyLong());
        verify(questionService, never()).save(any(Question.class));
    }

    /**
     * Test Case ID: UT_QBM_0X_001 (Assuming new module/number for update)
     * Purpose: Test update question successfully
     * Prerequisites:
     *  - questionService.getQuestionById (for pre-check) returns the question to be updated
     *  - questionService.save is mocked
     * Test Steps:
     *  1. Mock questionService.getQuestionById() for the existing question
     *  2. Mock questionService.save()
     *  3. Call controller.updateQuestion()
     * Expected Results:
     *  - Returns 200 OK
     *  - Body is a ServiceResult with the updated question data
     *  - The ID of the question in the request body is set to the path variable ID
     */
    @Test
    @DisplayName("Update Question - Success")
    void updateQuestion_Success() {
        // Arrange
        Long existingQuestionId = 1L;
        Question questionToUpdate = new Question(); // This is the request body
        questionToUpdate.setQuestionText("Updated question text");
        // other fields of questionToUpdate can be set if needed for a more complete test

        Question existingQuestion = new Question(); // This is what getQuestionById returns initially
        existingQuestion.setId(existingQuestionId);
        existingQuestion.setQuestionText("Original question text");

        when(questionService.getQuestionById(existingQuestionId)).thenReturn(Optional.of(existingQuestion));
        doNothing().when(questionService).save(any(Question.class)); // or mock it to return the saved entity

        // Act
        ResponseEntity<?> response = questionController.updateQuestion(questionToUpdate, existingQuestionId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult serviceResult = (ServiceResult) response.getBody();
        assertEquals(HttpStatus.OK.value(), serviceResult.getStatusCode());
        assertEquals("Get question with id: " + existingQuestionId, serviceResult.getMessage());
        
        assertNotNull(serviceResult.getData());
        assertTrue(serviceResult.getData() instanceof Question);
        Question responseQuestion = (Question) serviceResult.getData();
        assertEquals(existingQuestionId, responseQuestion.getId()); // Crucial: ID from path variable should be set
        assertEquals(questionToUpdate.getQuestionText(), responseQuestion.getQuestionText());

        ArgumentCaptor<Question> questionArgumentCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionService).save(questionArgumentCaptor.capture());
        assertEquals(existingQuestionId, questionArgumentCaptor.getValue().getId()); // Verify ID was set before save
        assertEquals("Updated question text", questionArgumentCaptor.getValue().getQuestionText());
    }

    /**
     * Test Case ID: UT_QBM_0X_002
     * Purpose: Test update question when question to be updated is not found
     * Prerequisites:
     *  - questionService.getQuestionById (for pre-check) returns Optional.empty()
     * Test Steps:
     *  1. Mock questionService.getQuestionById() to return empty Optional
     *  2. Call controller.updateQuestion()
     * Expected Results:
     *  - Returns 200 OK
     *  - Body is a ServiceResult with NOT_FOUND status
     */
    @Test
    @DisplayName("Update Question - Not Found")
    void updateQuestion_NotFound() {
        // Arrange
        Long nonExistentQuestionId = 99L;
        Question questionUpdateData = new Question(); // Request body, content doesn't matter much here

        when(questionService.getQuestionById(nonExistentQuestionId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = questionController.updateQuestion(questionUpdateData, nonExistentQuestionId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult serviceResult = (ServiceResult) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), serviceResult.getStatusCode());
        assertEquals("Not found with id: " + nonExistentQuestionId, serviceResult.getMessage());
        assertNull(serviceResult.getData());
        verify(questionService, never()).save(any(Question.class));
    }

    /**
     * Test Case ID: UT_QBM_0X_003 (Assuming new module/number for delete)
     * Purpose: Test delete temp question successfully
     * Prerequisites:
     *  - questionService.getQuestionById returns the question to be deleted
     *  - questionService.update is mocked
     * Test Steps:
     *  1. Mock questionService.getQuestionById()
     *  2. Mock questionService.update()
     *  3. Call controller.deleteTempQuestion()
     * Expected Results:
     *  - Returns 204 No Content
     *  - Question's deleted flag is set correctly before update is called
     */
    @Test
    @DisplayName("Delete Temp Question - Success")
    void deleteTempQuestion_Success() {
        // Arrange
        Long questionId = 1L;
        boolean deleteStatus = true;
        Question questionToMarkDeleted = new Question();
        questionToMarkDeleted.setId(questionId);
        questionToMarkDeleted.setDeleted(!deleteStatus); // Start with opposite status

        when(questionService.getQuestionById(questionId)).thenReturn(Optional.of(questionToMarkDeleted));
        doNothing().when(questionService).update(any(Question.class));

        // Act
        ResponseEntity<?> response = questionController.deleteTempQuestion(questionId, deleteStatus);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        ArgumentCaptor<Question> questionArgumentCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionService).update(questionArgumentCaptor.capture());
        Question capturedQuestion = questionArgumentCaptor.getValue();
        assertEquals(questionId, capturedQuestion.getId());
        assertEquals(deleteStatus, capturedQuestion.isDeleted());
    }
    
    /**
     * Test Case ID: UT_QBM_0X_004
     * Purpose: Test delete temp question successfully (setting deleted to false)
     * Prerequisites:
     *  - questionService.getQuestionById returns the question
     *  - questionService.update is mocked
     * Test Steps:
     *  1. Mock services
     *  2. Call controller.deleteTempQuestion() with deleted = false
     * Expected Results:
     *  - Returns 204 No Content
     *  - Question's deleted flag is set to false
     */
    @Test
    @DisplayName("Delete Temp Question - Success (Undelete)")
    void deleteTempQuestion_Success_Undelete() {
        // Arrange
        Long questionId = 1L;
        boolean deleteStatus = false; // Setting deleted to false
        Question questionToModify = new Question();
        questionToModify.setId(questionId);
        questionToModify.setDeleted(true); // Start with deleted true

        when(questionService.getQuestionById(questionId)).thenReturn(Optional.of(questionToModify));
        doNothing().when(questionService).update(any(Question.class));

        // Act
        ResponseEntity<?> response = questionController.deleteTempQuestion(questionId, deleteStatus);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ArgumentCaptor<Question> questionArgumentCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionService).update(questionArgumentCaptor.capture());
        Question capturedQuestion = questionArgumentCaptor.getValue();
        assertEquals(deleteStatus, capturedQuestion.isDeleted());
    }

    /**
     * Test Case ID: UT_QBM_0X_005
     * Purpose: Test delete temp question when question is not found
     * Prerequisites:
     *  - questionService.getQuestionById returns Optional.empty()
     * Test Steps:
     *  1. Mock questionService.getQuestionById() to return empty Optional
     *  2. Call controller.deleteTempQuestion()
     * Expected Results:
     *  - Throws NoSuchElementException
     */
    @Test
    @DisplayName("Delete Temp Question - Not Found")
    void deleteTempQuestion_NotFound() {
        // Arrange
        Long nonExistentQuestionId = 99L;
        boolean deleteStatus = true;

        when(questionService.getQuestionById(nonExistentQuestionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            questionController.deleteTempQuestion(nonExistentQuestionId, deleteStatus);
        });
        verify(questionService, never()).update(any(Question.class));
    }
} 