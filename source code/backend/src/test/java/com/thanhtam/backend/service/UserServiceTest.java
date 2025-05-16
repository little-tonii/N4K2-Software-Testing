package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.mail.MessagingException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        transactionTemplate.execute(status -> {
            // Find and delete all test users
            List<User> testUsers = userRepository.findAll().stream()
                .filter(user -> user.getUsername().startsWith("testUser_") ||
                              user.getUsername().startsWith("activeUser_") ||
                              user.getUsername().startsWith("deletedUser_") ||
                              user.getUsername().startsWith("searchUser_"))
                .collect(Collectors.toList());
            userRepository.deleteAll(testUsers);
            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_01
     * Purpose: Test user creation with valid data
     * 
     * Prerequisites:
     * - Database is accessible
     * - ROLE_STUDENT exists in database
     * 
     * Test Steps:
     * 1. Create a new user with valid data
     * 2. Save the user using userService
     * 3. Verify the user was saved correctly
     * 
     * Expected Results:
     * - User is created successfully
     * - All user data is saved correctly
     * - User can be retrieved from database
     */
    @Test
    @DisplayName("Test create user with valid data")
    void testCreateUserWithValidData() {
        transactionTemplate.execute(status -> {
            // Ensure ROLE_STUDENT exists
            Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                    .orElseGet(() -> {
                        Role newRole = new Role(null, ERole.ROLE_STUDENT);
                        return roleRepository.save(newRole);
                    });

            // Create a profile
            Profile profile = new Profile();
            profile.setFirstName("Test");
            profile.setLastName("User");

            // Create roles
            Set<Role> roles = new HashSet<>();
            roles.add(studentRole);

            // Create a new user with unique username
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User user = new User();
            user.setUsername(uniqueUsername);
            user.setEmail(uniqueUsername + "@example.com");
            user.setPassword("password123");
            user.setProfile(profile);
            user.setRoles(roles);

            // Save the user
            User savedUser = userService.createUser(user);

            // Verify the user was saved correctly
            assertNotNull(savedUser);
            assertNotNull(savedUser.getId());
            assertEquals(uniqueUsername, savedUser.getUsername());
            assertEquals(uniqueUsername + "@example.com", savedUser.getEmail());
            assertNotNull(savedUser.getProfile());
            assertEquals("Test", savedUser.getProfile().getFirstName());
            assertEquals("User", savedUser.getProfile().getLastName());
            assertNotNull(savedUser.getRoles());
            assertFalse(savedUser.getRoles().isEmpty());
            assertEquals(ERole.ROLE_STUDENT, savedUser.getRoles().iterator().next().getName());

            // Verify we can retrieve the user from the database
            User retrievedUser = userRepository.findByUsername(uniqueUsername)
                    .orElse(null);
            
            assertNotNull(retrievedUser);
            assertEquals(savedUser.getId(), retrievedUser.getId());
            assertEquals(savedUser.getUsername(), retrievedUser.getUsername());
            assertEquals(savedUser.getEmail(), retrievedUser.getEmail());
            assertNotNull(retrievedUser.getProfile());
            assertEquals(savedUser.getProfile().getFirstName(), retrievedUser.getProfile().getFirstName());
            assertEquals(savedUser.getProfile().getLastName(), retrievedUser.getProfile().getLastName());
            assertNotNull(retrievedUser.getRoles());
            assertFalse(retrievedUser.getRoles().isEmpty());
            assertEquals(ERole.ROLE_STUDENT, retrievedUser.getRoles().iterator().next().getName());

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_02
     * Purpose: Test user retrieval by username
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Retrieve user by username
     * 
     * Expected Results:
     * - User is retrieved successfully
     * - Retrieved user data matches created user
     */
    @Test
    @DisplayName("Test get user by username")
    void testGetUserByUsername() {
        transactionTemplate.execute(status -> {
            // Create test user with unique username
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUser(uniqueUsername);
            User savedUser = userService.createUser(testUser);

            // Get user by username
            Optional<User> retrievedUser = userService.getUserByUsername(uniqueUsername);

            // Verify user was retrieved correctly
            assertTrue(retrievedUser.isPresent());
            assertEquals(savedUser.getId(), retrievedUser.get().getId());
            assertEquals(savedUser.getUsername(), retrievedUser.get().getUsername());
            assertEquals(savedUser.getEmail(), retrievedUser.get().getEmail());

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_03
     * Purpose: Test user existence check by username
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Check if user exists by username
     * 
     * Expected Results:
     * - User existence check returns correct result
     */
    @Test
    @DisplayName("Test check user existence by username")
    void testExistsByUsername() {
        transactionTemplate.execute(status -> {
            // Create test user with unique username
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUser(uniqueUsername);
            userService.createUser(testUser);

            // Check if user exists
            assertTrue(userService.existsByUsername(uniqueUsername));
            assertFalse(userService.existsByUsername("nonexistentUser"));

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_04
     * Purpose: Test user existence check by email
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Check if user exists by email
     * 
     * Expected Results:
     * - User existence check returns correct result
     */
    @Test
    @DisplayName("Test check user existence by email")
    void testExistsByEmail() {
        transactionTemplate.execute(status -> {
            // Create test user with unique username and email
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            String uniqueEmail = uniqueUsername + "@example.com";
            User testUser = createTestUser(uniqueUsername);
            testUser.setEmail(uniqueEmail);
            userService.createUser(testUser);

            // Check if user exists
            assertTrue(userService.existsByEmail(uniqueEmail));
            assertFalse(userService.existsByEmail("nonexistent@example.com"));

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_05
     * Purpose: Test user pagination
     * 
     * Prerequisites:
     * - Database is accessible
     * - Multiple test users exist in database
     * 
     * Test Steps:
     * 1. Create multiple test users
     * 2. Retrieve users with pagination
     * 
     * Expected Results:
     * - Users are retrieved with correct pagination
     */
    @Test
    @DisplayName("Test user pagination")
    void testFindUsersByPage() {
        transactionTemplate.execute(status -> {
            // Create multiple test users with unique usernames
            for (int i = 0; i < 5; i++) {
                String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
                User testUser = createTestUser(uniqueUsername);
                testUser.setEmail(uniqueUsername + "@example.com");
                userService.createUser(testUser);
            }

            // Test pagination
            Pageable pageable = PageRequest.of(0, 2);
            Page<User> userPage = userService.findUsersByPage(pageable);

            assertNotNull(userPage);
            assertEquals(2, userPage.getSize());
            assertTrue(userPage.getTotalElements() >= 5);

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_06
     * Purpose: Test user update
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Update user information
     * 3. Verify update was successful
     * 
     * Expected Results:
     * - User is updated successfully
     * - Updated data is saved correctly
     */
    @Test
    @DisplayName("Test update user")
    void testUpdateUser() {
        transactionTemplate.execute(status -> {
            // Create test user with unique username
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUser(uniqueUsername);
            User savedUser = userService.createUser(testUser);

            // Update user
            savedUser.getProfile().setFirstName("Updated");
            savedUser.getProfile().setLastName("Name");
            userService.updateUser(savedUser);

            // Verify update
            Optional<User> updatedUser = userService.getUserByUsername(uniqueUsername);
            assertTrue(updatedUser.isPresent());
            assertEquals("Updated", updatedUser.get().getProfile().getFirstName());
            assertEquals("Name", updatedUser.get().getProfile().getLastName());

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_07
     * Purpose: Test finding users by deleted status
     * 
     * Prerequisites:
     * - Database is accessible
     * - Multiple test users exist in database
     * 
     * Test Steps:
     * 1. Create multiple test users
     * 2. Set some users as deleted
     * 3. Retrieve users by deleted status
     * 
     * Expected Results:
     * - Users are retrieved correctly based on deleted status
     */
    @Test
    @DisplayName("Test find users by deleted status")
    void testFindUsersByDeletedStatus() {
        transactionTemplate.execute(status -> {
            // Create test users with unique usernames
            String activeUsername = "activeUser_" + UUID.randomUUID().toString().substring(0, 8);
            String deletedUsername = "deletedUser_" + UUID.randomUUID().toString().substring(0, 8);

            User activeUser = createTestUser(activeUsername);
            activeUser.setEmail(activeUsername + "@example.com");
            userService.createUser(activeUser);

            User deletedUser = createTestUser(deletedUsername);
            deletedUser.setEmail(deletedUsername + "@example.com");
            deletedUser.setDeleted(true);
            userService.createUser(deletedUser);

            // Test finding active users
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> activeUsers = userService.findUsersDeletedByPage(pageable, false);
            assertTrue(activeUsers.getContent().stream()
                    .anyMatch(user -> user.getUsername().equals(activeUsername)));
            assertFalse(activeUsers.getContent().stream()
                    .anyMatch(user -> user.getUsername().equals(deletedUsername)));

            // Test finding deleted users
            Page<User> deletedUsers = userService.findUsersDeletedByPage(pageable, true);
            assertTrue(deletedUsers.getContent().stream()
                    .anyMatch(user -> user.getUsername().equals(deletedUsername)));
            assertFalse(deletedUsers.getContent().stream()
                    .anyMatch(user -> user.getUsername().equals(activeUsername)));

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_08
     * Purpose: Test finding users by username search
     * 
     * Prerequisites:
     * - Database is accessible
     * - Multiple test users exist in database
     * 
     * Test Steps:
     * 1. Create multiple test users with different usernames
     * 2. Search users by username pattern
     * 
     * Expected Results:
     * - Users are retrieved correctly based on username search
     */
    @Test
    @DisplayName("Test find users by username search")
    void testFindUsersByUsernameSearch() {
        transactionTemplate.execute(status -> {
            // Create test users with unique usernames
            String searchPrefix = "searchUser_" + UUID.randomUUID().toString().substring(0, 8);
            for (int i = 0; i < 3; i++) {
                String uniqueUsername = searchPrefix + "_" + i;
                User testUser = createTestUser(uniqueUsername);
                testUser.setEmail(uniqueUsername + "@example.com");
                userService.createUser(testUser);
            }

            // Test searching users
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> searchResults = userService.findAllByDeletedAndUsernameContains(false, searchPrefix, pageable);

            assertNotNull(searchResults);
            assertEquals(3, searchResults.getTotalElements());
            assertTrue(searchResults.getContent().stream()
                    .allMatch(user -> user.getUsername().startsWith(searchPrefix)));

            return null;
        });
    }

    // Helper method to create test user
    private User createTestUser(String username) {
        Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                .orElseGet(() -> {
                    Role newRole = new Role(null, ERole.ROLE_STUDENT);
                    return roleRepository.save(newRole);
                });

        Profile profile = new Profile();
        profile.setFirstName("Test");
        profile.setLastName("User");

        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password123");
        user.setProfile(profile);
        user.setRoles(roles);

        return user;
    }
} 