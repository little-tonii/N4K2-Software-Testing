package com.thanhtam.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.thanhtam.backend.dto.*;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.service.*;
import com.thanhtam.backend.ultilities.ERole;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ExcelService excelService;

    @Mock
    private FilesStorageService filesStorageService;

    @InjectMocks
    private UserController userController;

    private User user;
    private Profile profile;
    private Role role;

    @BeforeEach
    void setUp() {
        // Initialize test data
        profile = new Profile();
        profile.setFirstName("John");
        profile.setLastName("Doe");

        role = new Role();
        role.setName(ERole.ROLE_STUDENT);

        user = new User();
        user.setId(1L);
        user.setUsername("johndoe");
        user.setEmail("johndoe@example.com");
        user.setPassword("password");
        user.setProfile(profile);
        user.setRoles(Collections.singleton(role));
    }

    @Test
    void testGetUser_UserExists() {
        // Mock service behavior
        when(userService.getUserByUsername("johndoe")).thenReturn(
            Optional.of(user)
        );

        // Call the controller method
        ResponseEntity<?> response = userController.getUser("johndoe");

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ServiceResult);

        // Extract the ServiceResult and verify its content
        ServiceResult result = (ServiceResult) response.getBody();
        assertEquals(
            "Lấy thông tin user johndoe thành công!",
            result.getMessage()
        );

        // Verify that the data in ServiceResult is the expected User object
        assertTrue(result.getData() instanceof Optional);
        Optional<User> resultUser = (Optional<User>) result.getData();
        assertTrue(resultUser.isPresent());
        assertEquals(user, resultUser.get());
    }

    @Test
    void testGetUser_UserNotFound() {
        // Mock service behavior
        when(userService.getUserByUsername("unknown")).thenReturn(
            Optional.empty()
        );

        // Call the controller method
        ResponseEntity<?> response = userController.getUser("unknown");

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult result = (ServiceResult) response.getBody();
        assertEquals(
            "Tên đăng nhâp unknown không tìm thấy!",
            result.getMessage()
        );
        assertNull(result.getData());
    }

    // Test for checkUsername method
    @Test
    void testCheckUsername_Exists() {
        // Mock service behavior
        when(userService.existsByUsername("johndoe")).thenReturn(true);

        // Call the controller method
        boolean result = userController.checkUsername("johndoe");

        // Verify the result
        assertTrue(result);
    }

    @Test
    void testCheckUsername_NotExists() {
        // Mock service behavior
        when(userService.existsByUsername("unknown")).thenReturn(false);

        // Call the controller method
        boolean result = userController.checkUsername("unknown");

        // Verify the result
        assertFalse(result);
    }

    // Test for updateEmail method
    @Test
    void testUpdateEmail_Success() {
        // Mock service behavior
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        when(
            passwordEncoder.matches("password", user.getPassword())
        ).thenReturn(true);

        // Prepare input
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail("newemail@example.com");
        emailUpdate.setPassword("password");

        // Call the controller method
        ResponseEntity<?> response = userController.updateEmail(
            emailUpdate,
            1L
        );

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult result = (ServiceResult) response.getBody();
        assertEquals("Update email successfully", result.getMessage());
        assertEquals("newemail@example.com", result.getData());
    }

    @Test
    void testUpdateEmail_WrongPassword() {
        // Mock service behavior
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        when(
            passwordEncoder.matches("wrongpassword", user.getPassword())
        ).thenReturn(false);

        // Prepare input
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail("newemail@example.com");
        emailUpdate.setPassword("wrongpassword");

        // Call the controller method
        ResponseEntity<?> response = userController.updateEmail(
            emailUpdate,
            1L
        );

        // Verify the response
        assertEquals(
            HttpStatus.EXPECTATION_FAILED,
            HttpStatus.valueOf(
                ((ServiceResult) response.getBody()).getStatusCode()
            )
        );
        assertEquals(
            "Password is wrong",
            ((ServiceResult) response.getBody()).getMessage()
        );
    }

    // Test for getUsersByPage method
    @Test
    void testGetUsersByPage() {
        // Mock service behavior
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        when(userService.findUsersByPage(any(Pageable.class))).thenReturn(
            userPage
        );

        // Call the controller method
        PageResult result = userController.getUsersByPage(Pageable.unpaged());

        // Verify the result
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    // Test for createUser method
    @Test
    void testCreateUser_Success() {
        // Mock service behavior
        when(userService.existsByUsername("johndoe")).thenReturn(false);
        when(userService.existsByEmail("johndoe@example.com")).thenReturn(
            false
        );
        when(userService.createUser(any(User.class))).thenReturn(user);

        // Call the controller method
        ResponseEntity<?> response = userController.createUser(user);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ServiceResult);
        ServiceResult result = (ServiceResult) response.getBody();
        assertEquals("User created successfully!", result.getMessage());
        assertEquals(user, result.getData());
    }

    @Test
    void testCreateUser_UsernameExists() {
        when(userService.existsByUsername("johndoe")).thenReturn(true);

        ResponseEntity<?> response = userController.createUser(user);

        assertEquals(
            HttpStatus.CONFLICT,
            HttpStatus.valueOf(
                ((ServiceResult) response.getBody()).getStatusCode()
            )
        );
        assertEquals(
            "Tên đăng nhập đã có người sử dụng!",
            ((ServiceResult) response.getBody()).getMessage()
        );
    }
}
