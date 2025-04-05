package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.PasswordResetToken;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.PasswordResetTokenRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserByUsername() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @Transactional
    void testCreateUser() {
    User user = new User();
    user.setUsername("testUser");
    user.setEmail("test@example.com");

    Role role = new Role();
    role.setName(ERole.ROLE_STUDENT);

    // Mock roleService để trả về role khi gọi findByName
    when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(role));
    when(passwordEncoder.encode(user.getUsername())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);

    User result = userService.createUser(user);

    assertNotNull(result);
    assertEquals("testUser", result.getUsername());
    verify(userRepository, times(1)).save(any(User.class));
}

    @Test
    void testExistsByUsername() {
        String username = "testUser";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        boolean exists = userService.existsByUsername(username);

        assertTrue(exists);
        verify(userRepository, times(1)).existsByUsername(username);
    }

    @Test
    void testExistsByEmail() {
        String email = "test@example.com";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean exists = userService.existsByEmail(email);

        assertTrue(exists);
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    @Transactional
    void testRequestPasswordReset() throws Exception {
    String email = "test@example.com";
    User user = new User();
    user.setEmail(email);
    user.setId(1L);

    PasswordResetToken passwordResetToken = new PasswordResetToken();
    passwordResetToken.setToken("mockToken");
    passwordResetToken.setUser(user);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(passwordResetToken);

    doNothing().when(emailService).resetPassword(email, "mockToken");

    boolean result = userService.requestPasswordReset(email);

    assertTrue(result);
    verify(userRepository, times(1)).findByEmail(email);
    verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
    }
}