package com.thanhtam.backend.service;

import com.thanhtam.backend.config.JwtUtils;
import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.entity.PasswordResetToken;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.PasswordResetTokenRepository;
import com.thanhtam.backend.repository.ProfileRepository;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.test.context.support.WithMockUser;

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

    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        // Clean up any existing test data before each test
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            transactionTemplate.execute(status -> {
                try {
                    // 1. Identify all test users based on username prefixes
                    List<User> testUsers = userRepository.findAll().stream()
                        .filter(user -> user.getUsername() != null && (
                            user.getUsername().startsWith("testUser") ||
                            user.getUsername().startsWith("activeUser") ||
                            user.getUsername().startsWith("deletedUser") ||
                            user.getUsername().startsWith("searchUser") || // Existing prefixes
                            user.getUsername().startsWith("exportUser")))  // Added prefix
                        .collect(Collectors.toList());

                    if (testUsers.isEmpty()) {
                        logger.info("No test users found for cleanup based on prefixes.");
                        return null;
                    }
                    logger.info("Found {} test users for cleanup: {}", testUsers.size(), testUsers.stream().map(User::getUsername).collect(Collectors.toList()));

                    List<Long> testUserIds = testUsers.stream().map(User::getId).collect(Collectors.toList());
                    Set<Long> profileIdsToDelete = new HashSet<>();
                    for (User user : testUsers) {
                        if (user.getProfile() != null) {
                            profileIdsToDelete.add(user.getProfile().getId());
                        }
                    }

                    // 3. Delete PasswordResetTokens associated with these test users
                    List<PasswordResetToken> allTokens = passwordResetTokenRepository.findAll();
                    List<PasswordResetToken> tokensAssociatedWithTestUsers = allTokens.stream()
                        .filter(token -> token.getUser() != null && testUserIds.contains(token.getUser().getId()))
                        .collect(Collectors.toList());

                    if (!tokensAssociatedWithTestUsers.isEmpty()) {
                        passwordResetTokenRepository.deleteAll(tokensAssociatedWithTestUsers); // Batch delete tokens
                        logger.info("Successfully deleted {} password reset tokens for test users.", tokensAssociatedWithTestUsers.size());
                    } else {
                        logger.info("No password reset tokens found associated with the identified test users.");
                    }
                    
                    // 4. Delete the test users
                    userRepository.deleteAll(testUsers); // Batch delete users
                    logger.info("Successfully deleted {} test users.", testUsers.size());

                    // 5. Delete the associated profiles (if not cascaded)
                    if (!profileIdsToDelete.isEmpty()) {
                        // profileRepository.deleteAllById(profileIdsToDelete); // Prefer this if available
                        for (Long profileId : profileIdsToDelete) { // Fallback to individual deletion if needed
                            try {
                                profileRepository.deleteById(profileId);
                                logger.info("Successfully deleted profile with ID: {}", profileId);
                            } catch (Exception e) {
                                logger.error("Error deleting profile with ID {}: {}", profileId, e.getMessage(), e);
                            }
                        }
                        logger.info("Attempted to delete {} profiles.", profileIdsToDelete.size());
                    } else {
                        logger.info("No profiles found to delete for the identified test users.");
                    }

                } catch (Exception e) {
                    logger.error("Error during test data cleanup execution: {}", e.getMessage(), e);
                    if (status != null) status.setRollbackOnly(); // Mark transaction for rollback on any error
                }
                return null;
            });
        } catch (Exception e) {
            logger.error("Transaction error during test data cleanup: {}", e.getMessage(), e);
        }
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
     * Input:
     * - Username: "testUser_01"
     * - Email: "testUser_01@example.com"
     * - Password: "password123"
     * - Profile: "Test User"
     * - Role: "ROLE_STUDENT"
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
     * Purpose: Test user creation with an invalid email format
     *
     * Prerequisites:
     * - Database is accessible
     * - ROLE_STUDENT exists in database
     *
     * Test Steps:
     * 1. Attempt to create a new user with an invalid email format
     * 2. Verify that user creation fails
     *
     * Input:
     * - Username: "testUser_invalidEmail_02"
     * - Email: "invalid-email-format"
     * - Password: "password123"
     * - Profile: "Test User"
     * - Role: "ROLE_STUDENT"
     *
     * Expected Results:
     * - User creation throws an IllegalArgumentException or a suitable validation exception
     */
    @Test
    @DisplayName("Test create user with invalid email format")
    void testCreateUserWithInvalidEmailFormat() {
        // Prepare data that doesn't necessarily need its own transaction here
        Profile profile = new Profile();
        profile.setFirstName("Test");
        profile.setLastName("InvalidEmail");

        String uniqueUsername = "testUser_invalidEmail_" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail("invalid-email-format"); // Invalid email
        user.setPassword("password123");
        user.setProfile(profile); // Profile is transient until User is saved with it

        // Assert that the transactional operation throws an exception
        Exception thrownException = assertThrows(Exception.class, () -> {
            transactionTemplate.execute(status -> {
                // Ensure ROLE_STUDENT exists (this needs to be part of the same transaction)
                Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                        .orElseGet(() -> {
                            Role newRole = new Role(null, ERole.ROLE_STUDENT);
                            return roleRepository.save(newRole);
                        });

                Set<Role> roles = new HashSet<>();
                roles.add(studentRole);
                user.setRoles(roles);

                userService.createUser(user); // This call is expected to throw
                // If createUser does not throw, assertThrows will fail, which is correct.
                // If createUser throws, the exception propagates from this lambda,
                // TransactionTemplate handles rollback, and assertThrows catches it.
                return null; // For TransactionCallback signature
            });
        });

        logger.info("Caught expected exception for invalid email: {}", thrownException.getMessage());
        // Verify that the user was NOT actually saved.
        assertFalse(userRepository.findByUsername(uniqueUsername).isPresent(),
                "User with invalid email should not be saved.");
    }

    /**
     * Test Case ID: UT_AM_03
     * Purpose: Test user creation with a password that is too short
     *
     * Prerequisites:
     * - Database is accessible
     * - ROLE_STUDENT exists in database
     * - Password validation rules (e.g., minimum length of 8 characters) are in place
     *
     * Test Steps:
     * 1. Attempt to create a new user with a password that is too short (e.g., "12345")
     * 2. Verify that user creation fails
     *
     * Input:
     * - Username: "testUser_shortPass_03"
     * - Email: "testUser_shortPass_03@example.com"
     * - Password: "1234567"
     * - Profile: "Test User"
     * - Role: "ROLE_STUDENT"
     *
     * Expected Results:
     * - User creation throws an IllegalArgumentException or a suitable validation exception
     */
    @Test
    @DisplayName("Test create user with password too short")
    void testCreateUserWithPasswordTooShort() {
        // Prepare data
        Profile profile = new Profile();
        profile.setFirstName("Test");
        profile.setLastName("ShortPass");

        String uniqueUsername = "testUser_shortPass_" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail(uniqueUsername + "@example.com");
        user.setPassword("1234567"); // Password is too short
        user.setProfile(profile);

        // Assert that the transactional operation throws an exception
        Exception thrownException = assertThrows(Exception.class, () -> {
            transactionTemplate.execute(status -> {
                // Ensure ROLE_STUDENT exists
                Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                        .orElseGet(() -> {
                            Role newRole = new Role(null, ERole.ROLE_STUDENT);
                            return roleRepository.save(newRole);
                        });

                Set<Role> roles = new HashSet<>();
                roles.add(studentRole);
                user.setRoles(roles);

                userService.createUser(user); // Expected to throw
                return null; // For TransactionCallback signature
            });
        });

        logger.info("Caught expected exception for short password: {}", thrownException.getMessage());
        // Verify that the user was NOT actually saved.
        assertFalse(userRepository.findByUsername(uniqueUsername).isPresent(),
                "User with too short password should not be saved.");
    }

    /**
     * Test Case ID: UT_AM_04
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
     * Input:
     * - User with username: "testUser_04"
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
     * Test Case ID: UT_AM_05
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
     * Input:
     * - User with username: "testUser_05"
     * Expected Results:
     * - Return true
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
     * Test Case ID: UT_AM_06
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
     * Input:
     * - User with email: "testUser_06@example.com"
     * Expected Results:
     * - Return true
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
     * Test Case ID: UT_AM_07
     * Purpose: Test user pagination
     * 
     * Method function: findUsersByPage
     * 
     * Prerequisites:
     * - Database is accessible
     * - Multiple test users exist in database
     * 
     * Test Steps:
     * 1. Create multiple test users
     * 2. Retrieve users with pagination
     * 
     * Input:
     * - Page number: 0
     * - Page size: 2
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
                logger.info("Created test user with username: {}", uniqueUsername);
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
     * Test Case ID: UT_AM_08
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
     * Input:
     * - User with username: "testUser_08"
     * - New first name: "Updated"
     * - New last name: "Name"
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
     * Test Case ID: UT_AM_09
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
     * Input:
     * - 
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
     * Test Case ID: UT_AM_25
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

    /**
     * Test Case ID: UT_AM_26
     * Purpose: Test finding user by ID
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Find user by ID
     * 
     * Expected Results:
     * - User is found successfully
     * - Retrieved user data matches created user
     */
    @Test
    @DisplayName("Test find user by ID")
    void testFindUserById() {
        transactionTemplate.execute(status -> {
            // Create test user
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUser(uniqueUsername);
            User savedUser = userService.createUser(testUser);

            // Find user by ID
            Optional<User> foundUser = userService.findUserById(savedUser.getId());

            // Verify user was found
            assertTrue(foundUser.isPresent());
            assertEquals(savedUser.getId(), foundUser.get().getId());
            assertEquals(savedUser.getUsername(), foundUser.get().getUsername());
            assertEquals(savedUser.getEmail(), foundUser.get().getEmail());

            // Test with non-existent ID
            Optional<User> notFoundUser = userService.findUserById(99999L);
            assertFalse(notFoundUser.isPresent());

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_27
     * Purpose: Test finding users by intake ID
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Find users by intake ID
     * 
     * Expected Results:
     * - Users are found successfully
     */
    @Test
    @DisplayName("Test find users by intake ID")
    void testFindAllByIntakeId() {
        transactionTemplate.execute(status -> {
            // Create test user
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUser(uniqueUsername);
            userService.createUser(testUser);

            // Find users by intake ID
            List<User> users = userService.findAllByIntakeId(1L);
            assertNotNull(users);

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_28
     * Purpose: Test finding users by username or email contains
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test users exist in database
     * 
     * Test Steps:
     * 1. Create test users
     * 2. Search users by username or email
     * 
     * Expected Results:
     * - Users are found successfully based on search criteria
     */
    @Test
    @DisplayName("Test find users by username or email contains")
    void testFindAllByUsernameContainsOrEmailContains() {
        transactionTemplate.execute(status -> {
            // Create test users
            String searchPrefix = "searchUser_" + UUID.randomUUID().toString().substring(0, 8);
            for (int i = 0; i < 3; i++) {
                String uniqueUsername = searchPrefix + "_" + i;
                User testUser = createTestUser(uniqueUsername);
                testUser.setEmail(uniqueUsername + "@example.com");
                userService.createUser(testUser);
            }

            // Test searching users
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> searchResults = userService.findAllByUsernameContainsOrEmailContains(searchPrefix, searchPrefix, pageable);

            assertNotNull(searchResults);
            assertEquals(3, searchResults.getTotalElements(), "Should find exactly the 3 users created for this test");
            assertTrue(searchResults.getContent().stream()
                    .allMatch(user -> user.getUsername().contains(searchPrefix) || user.getEmail().contains(searchPrefix)));

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_29
     * Purpose: Test finding users for export
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test users exist in database
     * 
     * Test Steps:
     * 1. Create test users
     * 2. Export users by deleted status
     * 
     * Expected Results:
     * - Users are exported successfully
     */
    @Test
    @DisplayName("Test find users for export")
    void testFindAllByDeletedToExport() {
        transactionTemplate.execute(status -> {
            // Create test users
            String exportPrefix = "exportUser_" + UUID.randomUUID().toString().substring(0, 8);
            for (int i = 0; i < 3; i++) {
                String uniqueUsername = exportPrefix + "_" + i;
                User testUser = createTestUser(uniqueUsername);
                testUser.setEmail(uniqueUsername + "@example.com");
                userService.createUser(testUser);
            }

            // Test exporting users
            List<UserExport> exportResults = userService.findAllByDeletedToExport(false);

            assertNotNull(exportResults);
            assertTrue(exportResults.size() >= 3);
            assertTrue(exportResults.stream()
                    .allMatch(user -> user.getUsername().startsWith(exportPrefix)));

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_30
     * Purpose: Test get current username
     * 
     * Prerequisites:
     * - Security context is available
     * 
     * Test Steps:
     * 1. Set up security context
     * 2. Get current username
     * 
     * Expected Results:
     * - Current username is retrieved successfully
     */
    @Test
    @DisplayName("Test get current username")
    @WithMockUser(username = "testuser")
    void testGetUserName() {
        String username = userService.getUserName();
        assertEquals("testuser", username);
    }

    /**
     * Test Case ID: UT_AM_31
     * Purpose: Test password reset request
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Request password reset
     * 
     * Expected Results:
     * - Password reset request is processed successfully
     */
    @Test
    @DisplayName("Test password reset request")
    void testRequestPasswordReset() {
        // Create test user
        String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
        User testUser = createTestUser(uniqueUsername);
        testUser.setEmail(uniqueUsername + "@example.com");
        
        transactionTemplate.execute(status -> {
            userService.createUser(testUser);
            return null;
        });

        // Skip email service test if not configured
        if (System.getProperty("spring.mail.username") == null) {
            logger.warn("Skipping email service test - email configuration not found");
            return;
        }

        try {
            // Test password reset request
            boolean result = userService.requestPasswordReset(testUser.getEmail());
            assertTrue(result);

            // Test with non-existent email
            boolean nonExistentResult = userService.requestPasswordReset("nonexistent@example.com");
            assertFalse(nonExistentResult);
        } catch (MessagingException e) {
            fail("Email service failed: " + e.getMessage());
        }
    }

    /**
     * Test Case ID: UT_AM_32
     * Purpose: Test password reset
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * - Password reset token exists
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Create password reset token
     * 3. Reset password
     * 
     * Expected Results:
     * - Password is reset successfully
     */
    @Test
    @DisplayName("Test password reset")
    void testResetPassword() {
        transactionTemplate.execute(status -> {
            // Create test user
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUser(uniqueUsername);
            testUser.setEmail(uniqueUsername + "@example.com");
            User savedUser = userService.createUser(testUser);

            // Create password reset token using JwtUtils
            JwtUtils jwtUtils = new JwtUtils();
            String token = jwtUtils.generatePasswordResetToken(savedUser.getId());
            
            // Verify token is valid before using it
            assertFalse(jwtUtils.hasTokenExpired(token));

            // Create and save password reset token
            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(token);
            passwordResetToken.setUser(savedUser);
            passwordResetTokenRepository.save(passwordResetToken);

            // Test password reset
            String newPassword = "newPassword123";
            boolean result = userService.resetPassword(token, newPassword);
            assertTrue(result);

            // Verify password was changed
            Optional<User> updatedUser = userService.getUserByUsername(uniqueUsername);
            assertTrue(updatedUser.isPresent());
            assertTrue(passwordEncoder.matches(newPassword, updatedUser.get().getPassword()));

            // Test with non-existent token
            boolean invalidResult = userService.resetPassword(jwtUtils.generatePasswordResetToken(99999L), newPassword);
            assertFalse(invalidResult);

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