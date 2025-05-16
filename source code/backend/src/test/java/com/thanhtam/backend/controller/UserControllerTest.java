package com.thanhtam.backend.controller;

import com.thanhtam.backend.dto.*;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.service.ExcelService;
import com.thanhtam.backend.service.FilesStorageService;
import com.thanhtam.backend.service.RoleService;
import com.thanhtam.backend.service.UserService;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.ArgumentCaptor;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleService roleService;

    @Mock
    private ExcelService excelService;

    @Mock
    private FilesStorageService filesStorageService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test Case ID: UT_AM_38
     * Purpose: Test getting user profile by username
     * 
     * Prerequisites:
     * - UserService is mocked
     * - Test user exists
     * 
     * Test Steps:
     * 1. Mock userService to return a test user
     * 2. Call getUser endpoint with username
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - Returns correct user data
     */
    @Test
    @DisplayName("Test get user profile by username")
    void testGetUserProfile() {
        // Arrange
        String username = "testuser";
        User testUser = createTestUser(username);
        when(userService.getUserByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = userController.getUser(username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertNotNull(result.getData());
    }

    /**
     * Test Case ID: UT_AM_39
     * Purpose: Test getting user profile with empty username (should fetch current user)
     * 
     * Prerequisites:
     * - UserService is mocked
     * - Current user is set up in userService
     * 
     * Test Steps:
     * 1. Mock userService.getUserName() to return a specific username
     * 2. Mock userService.getUserByUsername() for that specific username
     * 3. Call getUser endpoint with an empty string
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - Returns correct user data for the current user
     */
    @Test
    @DisplayName("Test get user profile with empty username")
    void testGetUserProfile_EmptyUsername() {
        // Arrange
        String currentUsername = "currentUser";
        User testUser = createTestUser(currentUsername);
        when(userService.getUserName()).thenReturn(currentUsername);
        when(userService.getUserByUsername(currentUsername)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = userController.getUser("");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals("Lấy thông tin user " + currentUsername + " thành công!", result.getMessage());
        assertNotNull(result.getData());
        verify(userService).getUserName();
        verify(userService).getUserByUsername(currentUsername);
    }

    /**
     * Test Case ID: UT_AM_40
     * Purpose: Test getting user profile when user is not found
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Mock userService.getUserByUsername() to return Optional.empty()
     * 2. Call getUser endpoint with a non-existent username
     * 
     * Expected Results:
     * - Returns 200 OK status (as per current implementation which wraps NOT_FOUND in ServiceResult)
     * - ServiceResult contains NOT_FOUND status and appropriate message
     */
    @Test
    @DisplayName("Test get user profile - User Not Found")
    void testGetUserProfile_UserNotFound() {
        // Arrange
        String username = "nonexistentuser";
        when(userService.getUserByUsername(username)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = userController.getUser(username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Controller wraps this
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals("Tên đăng nhập " + username + " không tìm thấy!", result.getMessage());
        assertNull(result.getData());
        verify(userService).getUserByUsername(username);
    }

    /**
     * Test Case ID: UT_AM_41
     * Purpose: Test checking username existence
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Mock userService.existsByUsername
     * 2. Call checkUsername endpoint
     * 
     * Expected Results:
     * - Returns correct boolean value
     */
    @Test
    @DisplayName("Test check username existence")
    void testCheckUsername() {
        // Arrange
        String username = "testuser";
        when(userService.existsByUsername(username)).thenReturn(true);

        // Act
        boolean result = userController.checkUsername(username);

        // Assert
        assertTrue(result);
        verify(userService).existsByUsername(username);
    }

    /**
     * Test Case ID: UT_AM_42
     * Purpose: Test updating user email
     * 
     * Prerequisites:
     * - UserService is mocked
     * - PasswordEncoder is mocked
     * - Test user exists
     * 
     * Test Steps:
     * 1. Create test user and email update data
     * 2. Mock password verification
     * 3. Call updateEmail endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - Email is updated successfully
     */
    @Test
    @DisplayName("Test update user email")
    void testUpdateEmail() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String password = "password123";
        User testUser = createTestUser("testuser");
        testUser.setId(userId);
        testUser.setPassword("encodedPassword");

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail(newEmail);
        emailUpdate.setPassword(password);

        when(userService.findUserById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.updateEmail(emailUpdate, userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        verify(userService).updateUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_43
     * Purpose: Test update user email - Wrong Password
     * 
     * Prerequisites:
     * - UserService is mocked
     * - PasswordEncoder is mocked
     * - Test user exists
     * 
     * Test Steps:
     * 1. Create test user and email update data
     * 2. Mock passwordEncoder.matches to return false
     * 3. Call updateEmail endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - ServiceResult contains EXPECTATION_FAILED status and appropriate message
     * - userService.updateUser is not called
     */
    @Test
    @DisplayName("Test update user email - Wrong Password")
    void testUpdateEmail_WrongPassword() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String wrongPassword = "wrongPassword";
        User testUser = createTestUser("testuser");
        testUser.setId(userId);
        testUser.setPassword("encodedPassword");

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail(newEmail);
        emailUpdate.setPassword(wrongPassword);

        when(userService.findUserById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, testUser.getPassword())).thenReturn(false);

        // Act
        ResponseEntity<?> response = userController.updateEmail(emailUpdate, userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.EXPECTATION_FAILED.value(), result.getStatusCode());
        assertEquals("Password is wrong", result.getMessage());
        verify(userService, never()).updateUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_44
     * Purpose: Test update user email - User Not Found
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Mock userService.findUserById to return Optional.empty()
     * 2. Call updateEmail endpoint
     * 
     * Expected Results:
     * - Throws NoSuchElementException (due to .get() call on empty Optional)
     */
    @Test
    @DisplayName("Test update user email - User Not Found")
    void testUpdateEmail_UserNotFound() {
        // Arrange
        Long userId = 999L; // Non-existent user ID
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail("anyemail@example.com");
        emailUpdate.setPassword("anypassword");

        when(userService.findUserById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            userController.updateEmail(emailUpdate, userId);
        });
        verify(userService, never()).updateUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_45
     * Purpose: Test update user password
     * 
     * Prerequisites:
     * - UserService is mocked
     * - PasswordEncoder is mocked
     * - Test user exists
     * 
     * Test Steps:
     * 1. Create test user and password update data
     * 2. Mock password verification
     * 3. Call updatePass endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - Password is updated successfully
     */
    @Test
    @DisplayName("Test update user password")
    void testUpdatePassword() {
        // Arrange
        Long userId = 1L;
        String currentPassword = "currentPass";
        String newPassword = "newPass";
        User testUser = createTestUser("testuser");
        testUser.setId(userId);
        testUser.setPassword("encodedCurrentPass");

        PasswordUpdate passwordUpdate = new PasswordUpdate();
        passwordUpdate.setCurrentPassword(currentPassword);
        passwordUpdate.setNewPassword(newPassword);

        when(userService.findUserById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPass");

        // Act
        ResponseEntity<?> response = userController.updatePass(passwordUpdate, userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        verify(userService).updateUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_46
     * Purpose: Test update user password - Wrong Current Password
     * 
     * Prerequisites:
     * - UserService is mocked
     * - PasswordEncoder is mocked
     * - Test user exists
     * 
     * Test Steps:
     * 1. Create test user and password update data with wrong current password
     * 2. Mock passwordEncoder.matches to return false
     * 3. Call updatePass endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - ServiceResult contains BAD_REQUEST status and appropriate message
     * - userService.updateUser is not called
     */
    @Test
    @DisplayName("Test update user password - Wrong Current Password")
    void testUpdatePassword_WrongCurrentPassword() {
        // Arrange
        Long userId = 1L;
        String wrongCurrentPassword = "wrongCurrentPass";
        String newPassword = "newPass";
        User testUser = createTestUser("testuser");
        testUser.setId(userId);
        testUser.setPassword("encodedCorrectCurrentPass");

        PasswordUpdate passwordUpdate = new PasswordUpdate();
        passwordUpdate.setCurrentPassword(wrongCurrentPassword);
        passwordUpdate.setNewPassword(newPassword);

        when(userService.findUserById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongCurrentPassword, testUser.getPassword())).thenReturn(false);

        // Act
        ResponseEntity<?> response = userController.updatePass(passwordUpdate, userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatusCode());
        assertEquals("Wrong password, please check again!", result.getMessage());
        verify(userService, never()).updateUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_47
     * Purpose: Test update user password - New Password Same as Current
     * 
     * Prerequisites:
     * - UserService is mocked
     * - PasswordEncoder is mocked
     * - Test user exists
     * 
     * Test Steps:
     * 1. Create test user and password update data with new password same as current
     * 2. Mock passwordEncoder.matches to return true
     * 3. Call updatePass endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - ServiceResult contains CONFLICT status and appropriate message
     * - userService.updateUser is not called
     */
    @Test
    @DisplayName("Test update user password - New Password Same as Current")
    void testUpdatePassword_NewPasswordSameAsCurrent() {
        // Arrange
        Long userId = 1L;
        String currentPassword = "currentPass";
        User testUser = createTestUser("testuser");
        testUser.setId(userId);
        testUser.setPassword("encodedCurrentPass");

        PasswordUpdate passwordUpdate = new PasswordUpdate();
        passwordUpdate.setCurrentPassword(currentPassword);
        passwordUpdate.setNewPassword(currentPassword); // New password is same as current

        when(userService.findUserById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.updatePass(passwordUpdate, userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.CONFLICT.value(), result.getStatusCode());
        assertEquals("This is old password", result.getMessage());
        verify(userService, never()).updateUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_48
     * Purpose: Test update user password - User Not Found
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Mock userService.findUserById to return Optional.empty()
     * 2. Call updatePass endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - ServiceResult contains INTERNAL_SERVER_ERROR status (due to .get() on empty Optional causing exception caught by generic catch)
     */
    @Test
    @DisplayName("Test update user password - User Not Found")
    void testUpdatePassword_UserNotFound() {
        // Arrange
        Long userId = 999L; // Non-existent user ID
        PasswordUpdate passwordUpdate = new PasswordUpdate();
        passwordUpdate.setCurrentPassword("anyCurrentPass");
        passwordUpdate.setNewPassword("anyNewPass");

        when(userService.findUserById(userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = userController.updatePass(passwordUpdate, userId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatusCode());
        // We can't assert the exact exception message as it might be vague from .get()
        verify(userService, never()).updateUser(any(User.class));
    }
    
    /**
     * Test Case ID: UT_AM_49
     * Purpose: Test update user password - Unexpected Exception
     * 
     * Prerequisites:
     * - UserService is mocked to throw an exception on findUserById
     * 
     * Test Steps:
     * 1. Mock userService.findUserById to throw a RuntimeException
     * 2. Call updatePass endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - ServiceResult contains INTERNAL_SERVER_ERROR status and the exception message
     */
    @Test
    @DisplayName("Test update user password - Unexpected Exception")
    void testUpdatePassword_UnexpectedException() {
        // Arrange
        Long userId = 1L;
        PasswordUpdate passwordUpdate = new PasswordUpdate();
        passwordUpdate.setCurrentPassword("currentPass");
        passwordUpdate.setNewPassword("newPass");
        String exceptionMessage = "Database connection lost";

        when(userService.findUserById(userId)).thenThrow(new RuntimeException(exceptionMessage));

        // Act
        ResponseEntity<?> response = userController.updatePass(passwordUpdate, userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatusCode());
        assertEquals(exceptionMessage, result.getMessage());
        verify(userService, never()).updateUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_50
     * Purpose: Test get users with pagination
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Create test users
     * 2. Mock userService.findUsersByPage
     * 3. Call getUsersByPage endpoint
     * 
     * Expected Results:
     * - Returns correct page result
     */
    @Test
    @DisplayName("Test get users with pagination")
    void testGetUsersByPage() {
        // Arrange
        List<User> users = Arrays.asList(
            createTestUser("user1"),
            createTestUser("user2")
        );
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userService.findUsersByPage(pageable)).thenReturn(userPage);

        // Act
        PageResult result = userController.getUsersByPage(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getData().size());
        verify(userService).findUsersByPage(pageable);
    }

    /**
     * Test Case ID: UT_AM_51
     * Purpose: Test create new user
     * 
     * Prerequisites:
     * - UserService is mocked
     * - RoleService is mocked
     * 
     * Test Steps:
     * 1. Create test user data
     * 2. Mock userService.existsByUsername and existsByEmail
     * 3. Call createUser endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - User is created successfully
     */
    @Test
    @DisplayName("Test create new user")
    void testCreateUser() {
        // Arrange
        User newUser = createTestUser("newuser");
        when(userService.existsByUsername(newUser.getUsername())).thenReturn(false);
        when(userService.existsByEmail(newUser.getEmail())).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(newUser);

        // Act
        ResponseEntity<?> response = userController.createUser(newUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        verify(userService).createUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_52
     * Purpose: Test create user - Username Already Exists
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Mock userService.existsByUsername to return true
     * 2. Call createUser endpoint
     * 
     * Expected Results:
     * - Returns BAD_REQUEST status
     * - ServiceResult contains CONFLICT status and appropriate message
     * - userService.createUser is not called
     */
    @Test
    @DisplayName("Test create user - Username Already Exists")
    void testCreateUser_UsernameExists() {
        // Arrange
        User newUser = createTestUser("existinguser");
        newUser.setEmail("newuser@example.com");

        when(userService.existsByUsername("existinguser")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.createUser(newUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.CONFLICT.value(), result.getStatusCode());
        assertEquals("Tên đăng nhập đã có người sử dụng!", result.getMessage());
        verify(userService, never()).createUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_53
     * Purpose: Test create user - Email Already Exists
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Mock userService.existsByUsername to return false
     * 2. Mock userService.existsByEmail to return true
     * 3. Call createUser endpoint
     * 
     * Expected Results:
     * - Returns BAD_REQUEST status
     * - ServiceResult contains CONFLICT status and appropriate message
     * - userService.createUser is not called
     */
    @Test
    @DisplayName("Test create user - Email Already Exists")
    void testCreateUser_EmailExists() {
        // Arrange
        User newUser = createTestUser("newusername");
        newUser.setEmail("existingemail@example.com");

        when(userService.existsByUsername("newusername")).thenReturn(false);
        when(userService.existsByEmail("existingemail@example.com")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.createUser(newUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.CONFLICT.value(), result.getStatusCode());
        assertEquals("Email đã có người sử dụng!", result.getMessage());
        verify(userService, never()).createUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_54
     * Purpose: Test search users by username or email
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Create test users
     * 2. Mock userService.findAllByUsernameContainsOrEmailContains
     * 3. Call searchUsersByUsernameOrEmail endpoint
     * 
     * Expected Results:
     * - Returns correct search results
     */
    @Test
    @DisplayName("Test search users by username or email")
    void testSearchUsers() {
        // Arrange
        String searchKeyword = "test";
        List<User> users = Arrays.asList(
            createTestUser("testuser1"),
            createTestUser("testuser2")
        );
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userService.findAllByUsernameContainsOrEmailContains(searchKeyword, searchKeyword, pageable))
            .thenReturn(userPage);

        // Act
        PageResult result = userController.searchUsersByUsernameOrEmail(searchKeyword, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getData().size());
        verify(userService).findAllByUsernameContainsOrEmailContains(searchKeyword, searchKeyword, pageable);
    }

    /**
     * Test Case ID: UT_AM_55
     * Purpose: Test check email existence for update - User Not Found
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Mock userService.findUserById to return Optional.empty()
     * 2. Call checkExistsEmailUpdate endpoint
     * 
     * Expected Results:
     * - Throws NoSuchElementException (due to .get() call on empty Optional)
     */
    @Test
    @DisplayName("Test check email existence for update - User Not Found")
    void testCheckExistsEmailUpdate_UserNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;
        String emailValue = "someemail@example.com";
        
        // This setup is for the first condition: userService.existsByEmail(value)
        // To reach the .get() call, existsByEmail must be true.
        when(userService.existsByEmail(emailValue)).thenReturn(true);
        when(userService.findUserById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            userController.checkExistsEmailUpdate(emailValue, nonExistentUserId);
        });
    }

    /**
     * Test Case ID: UT_AM_56
     * Purpose: Test delete (temp) user - User Not Found
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Mock userService.findUserById to return Optional.empty()
     * 2. Call deleteTempUser endpoint
     * 
     * Expected Results:
     * - Throws NoSuchElementException (due to .get() call on empty Optional)
     */
    @Test
    @DisplayName("Test delete (temp) user - User Not Found")
    void testDeleteTempUser_UserNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;
        boolean deletedStatus = true;

        when(userService.findUserById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            userController.deleteTempUser(nonExistentUserId, deletedStatus);
        });
        verify(userService, never()).updateUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_57
     * Purpose: Test update user - Null Password (No Change)
     * 
     * Prerequisites:
     * - UserService and PasswordEncoder are mocked
     * - Test user exists
     * 
     * Test Steps:
     * 1. Create test user and UserUpdate object with null password
     * 2. Mock userService.findUserById
     * 3. Call updateUser endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - User details are updated, password remains unchanged (not encoded/set)
     * - passwordEncoder.encode is not called
     */
    @Test
    @DisplayName("Test update user - Null Password (No Change)")
    void testUpdateUser_NullPassword() {
        // Arrange
        Long userId = 1L;
        User existingUser = createTestUser("testuser");
        existingUser.setId(userId);
        existingUser.setPassword("existingEncodedPassword");
        Profile existingProfile = new Profile();
        existingProfile.setId(10L);
        existingProfile.setFirstName("OldFirst");
        existingProfile.setLastName("OldLast");
        existingUser.setProfile(existingProfile);

        UserUpdate userUpdateReq = new UserUpdate();
        userUpdateReq.setEmail("newemail@example.com");
        userUpdateReq.setPassword(null); // Password not provided for update
        Profile updatedProfileDetails = new Profile();
        updatedProfileDetails.setFirstName("NewFirst");
        updatedProfileDetails.setLastName("NewLast");
        userUpdateReq.setProfile(updatedProfileDetails);

        when(userService.findUserById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        ResponseEntity<?> response = userController.updateUser(userUpdateReq, userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceResult result = (ServiceResult) response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatusCode());

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateUser(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();

        assertEquals("newemail@example.com", capturedUser.getEmail());
        assertEquals("existingEncodedPassword", capturedUser.getPassword()); // Password should not have changed
        assertEquals("NewFirst", capturedUser.getProfile().getFirstName());
        assertEquals("NewLast", capturedUser.getProfile().getLastName());
        assertEquals(existingProfile.getId(), capturedUser.getProfile().getId()); // Ensure Profile ID is preserved

        verify(passwordEncoder, never()).encode(anyString());
    }

    /**
     * Test Case ID: UT_AM_58
     * Purpose: Test update user - User Not Found
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Mock userService.findUserById to return Optional.empty()
     * 2. Call updateUser endpoint
     * 
     * Expected Results:
     * - Throws NoSuchElementException
     */
    @Test
    @DisplayName("Test update user - User Not Found")
    void testUpdateUser_UserNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;
        UserUpdate userUpdateReq = new UserUpdate(); // Details don't matter much here
        userUpdateReq.setEmail("any@any.com");
        userUpdateReq.setProfile(new Profile());

        when(userService.findUserById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            userController.updateUser(userUpdateReq, nonExistentUserId);
        });
        verify(userService, never()).updateUser(any(User.class));
    }

    // Helper method to create test user
    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password123");

        Profile profile = new Profile();
        profile.setFirstName("Test");
        profile.setLastName("User");
        user.setProfile(profile);

        Set<Role> roles = new HashSet<>();
        Role studentRole = new Role();
        studentRole.setName(ERole.ROLE_STUDENT);
        roles.add(studentRole);
        user.setRoles(roles);

        return user;
    }
} 