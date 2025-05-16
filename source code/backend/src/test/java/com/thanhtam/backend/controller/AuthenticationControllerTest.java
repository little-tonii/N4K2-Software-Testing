package com.thanhtam.backend.controller;

import com.thanhtam.backend.config.JwtUtils;
import com.thanhtam.backend.dto.LoginUser;
import com.thanhtam.backend.dto.OperationStatusDto;
import com.thanhtam.backend.dto.PasswordResetDto;
import com.thanhtam.backend.dto.PasswordResetRequest;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.payload.response.JwtResponse;
import com.thanhtam.backend.service.UserDetailsImpl;
import com.thanhtam.backend.service.UserService;
import com.thanhtam.backend.ultilities.ERole;
import com.thanhtam.backend.ultilities.RequestOperationName;
import com.thanhtam.backend.ultilities.RequestOperationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.mail.MessagingException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AuthenticationControllerTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test Case ID: UT_AM_33
     * Purpose: Test successful user authentication
     * 
     * Prerequisites:
     * - UserService is mocked
     * - AuthenticationManager is mocked
     * - JwtUtils is mocked
     * - Test user exists and is not deleted
     * 
     * Test Steps:
     * 1. Create test user and login data
     * 2. Mock authentication process
     * 3. Call authenticateUser endpoint
     * 
     * Expected Results:
     * - Returns 200 OK status
     * - Returns JWT token and user details
     */
    @Test
    @DisplayName("Test successful user authentication")
    void testAuthenticateUserSuccess() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String jwtToken = "test.jwt.token";
        
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername(username);
        loginUser.setPassword(password);

        User testUser = createTestUser(username);
        testUser.setDeleted(false);

        UserDetailsImpl userDetails = new UserDetailsImpl(
            testUser.getId(),
            testUser.getUsername(),
            testUser.getEmail(),
            testUser.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        when(userService.getUserByUsername(username)).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(jwtToken);
        doNothing().when(userService).updateUser(any(User.class));

        // Act
        ResponseEntity<?> response = authenticationController.authenticateUser(loginUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertNotNull(jwtResponse);
        assertEquals(jwtToken, jwtResponse.getAccessToken());
        assertEquals(username, jwtResponse.getUsername());
        verify(userService).updateUser(any(User.class));
    }

    /**
     * Test Case ID: UT_AM_34
     * Purpose: Test authentication with non-existent user
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Create login data for non-existent user
     * 2. Mock userService to return empty
     * 3. Call authenticateUser endpoint
     * 
     * Expected Results:
     * - Returns 400 Bad Request status
     */
    @Test
    @DisplayName("Test authentication with non-existent user")
    void testAuthenticateUserNotFound() {
        // Arrange
        String username = "nonexistentuser";
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername(username);
        loginUser.setPassword("password123");

        when(userService.getUserByUsername(username)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authenticationController.authenticateUser(loginUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_AM_35
     * Purpose: Test authentication with deleted user
     * 
     * Prerequisites:
     * - UserService is mocked
     * - Test user exists but is deleted
     * 
     * Test Steps:
     * 1. Create test user and login data
     * 2. Mock userService to return deleted user
     * 3. Call authenticateUser endpoint
     * 
     * Expected Results:
     * - Returns 400 Bad Request status
     */
    @Test
    @DisplayName("Test authentication with deleted user")
    void testAuthenticateUserDeleted() {
        // Arrange
        String username = "deleteduser";
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername(username);
        loginUser.setPassword("password123");

        User deletedUser = createTestUser(username);
        deletedUser.setDeleted(true);

        when(userService.getUserByUsername(username)).thenReturn(Optional.of(deletedUser));

        // Act
        ResponseEntity<?> response = authenticationController.authenticateUser(loginUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test Case ID: UT_AM_36
     * Purpose: Test password reset request
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Create password reset request
     * 2. Mock userService.requestPasswordReset
     * 3. Call resetPasswordRequest endpoint
     * 
     * Expected Results:
     * - Returns success operation status
     */
    @Test
    @DisplayName("Test password reset request")
    void testPasswordResetRequest() throws MessagingException {
        // Arrange
        String email = "test@example.com";
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail(email);

        when(userService.requestPasswordReset(email)).thenReturn(true);

        // Act
        OperationStatusDto result = authenticationController.resetPasswordRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals(RequestOperationName.REQUEST_PASSWORD_RESET.name(), result.getOperationName());
        assertEquals(RequestOperationStatus.SUCCESS.name(), result.getOperationResult());
        verify(userService).requestPasswordReset(email);
    }

    /**
     * Test Case ID: UT_AM_37
     * Purpose: Test password reset
     * 
     * Prerequisites:
     * - UserService is mocked
     * 
     * Test Steps:
     * 1. Create password reset data
     * 2. Mock userService.resetPassword
     * 3. Call resetPassword endpoint
     * 
     * Expected Results:
     * - Returns success operation status
     */
    @Test
    @DisplayName("Test password reset")
    void testPasswordReset() {
        // Arrange
        String token = "reset.token";
        String newPassword = "newPassword123";
        PasswordResetDto resetDto = new PasswordResetDto();
        resetDto.setToken(token);
        resetDto.setPassword(newPassword);

        when(userService.resetPassword(token, newPassword)).thenReturn(true);

        // Act
        OperationStatusDto result = authenticationController.resetPassword(resetDto);

        // Assert
        assertNotNull(result);
        assertEquals(RequestOperationName.PASSWORD_RESET.name(), result.getOperationName());
        assertEquals(RequestOperationStatus.SUCCESS.name(), result.getOperationResult());
        verify(userService).resetPassword(token, newPassword);
    }

    // Helper method to create test user
    private User createTestUser(String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("encodedPassword");

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