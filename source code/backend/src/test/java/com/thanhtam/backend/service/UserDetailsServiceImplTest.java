package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserDetailsServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImplTest.class);

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProfileRepository profileRepository;

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
                    // Find all test users
                    List<User> testUsers = userRepository.findAll().stream()
                        .filter(user -> user.getUsername() != null && (
                            user.getUsername().startsWith("testUser_") ||
                            user.getUsername().startsWith("nonexistentUser_")))
                        .collect(Collectors.toList());

                    logger.info("Found {} test users to clean up", testUsers.size());

                    // Delete all test users in a single transaction
                    for (User user : testUsers) {
                        try {
                            // First delete the user
                            userRepository.delete(user);
                            logger.info("Successfully deleted test user: {}", user.getUsername());
                            
                            // Then delete the associated profile if it exists
                            if (user.getProfile() != null) {
                                profileRepository.deleteById(user.getProfile().getId());
                                logger.info("Successfully deleted profile for user: {}", user.getUsername());
                            }
                        } catch (Exception e) {
                            logger.error("Error deleting user {}: {}", user.getUsername(), e.getMessage());
                            // Force rollback if there's an error
                            status.setRollbackOnly();
                        }
                    }

                    // Verify cleanup
                    long remainingTestUsers = userRepository.findAll().stream()
                        .filter(user -> user.getUsername() != null && (
                            user.getUsername().startsWith("testUser_") ||
                            user.getUsername().startsWith("nonexistentUser_")))
                        .count();
                    
                    if (remainingTestUsers > 0) {
                        logger.error("Cleanup incomplete: {} test users still exist", remainingTestUsers);
                        status.setRollbackOnly();
                    } else {
                        logger.info("Cleanup completed successfully");
                    }
                } catch (Exception e) {
                    logger.error("Error during test data cleanup: {}", e.getMessage());
                    status.setRollbackOnly();
                }
                return null;
            });
        } catch (Exception e) {
            logger.error("Transaction error during test data cleanup: {}", e.getMessage());
        }
    }

    /**
     * Test Case ID: UT_AM_06
     * Purpose: Test loading user details with existing username
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Load user details by username
     * 
     * Expected Results:
     * - UserDetails is loaded successfully
     * - UserDetails contains correct user information
     */
    @Test
    @DisplayName("Test load user details with existing username")
    void testLoadUserByUsernameWithExistingUser() {
        transactionTemplate.execute(status -> {
            // Create test user with unique username
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUser(uniqueUsername);
            User savedUser = userRepository.save(testUser);

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(uniqueUsername);

            // Verify user details
            assertNotNull(userDetails);
            assertEquals(savedUser.getUsername(), userDetails.getUsername());
            assertEquals(savedUser.getPassword(), userDetails.getPassword());
            assertFalse(userDetails.getAuthorities().isEmpty());
            assertEquals(1, userDetails.getAuthorities().size());
            assertEquals("ROLE_STUDENT", userDetails.getAuthorities().iterator().next().getAuthority());

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_07
     * Purpose: Test loading user details with non-existent username
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Try to load user details with non-existent username
     * 
     * Expected Results:
     * - UsernameNotFoundException is thrown
     */
    @Test
    @DisplayName("Test load user details with non-existent username")
    void testLoadUserByUsernameWithNonExistentUser() {
        // Try to load user details with non-existent username
        String nonExistentUsername = "nonexistentUser_" + UUID.randomUUID().toString();
        
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistentUsername);
        });

        // Verify exception message
        assertTrue(exception.getMessage().contains("User Not Found with username: " + nonExistentUsername));
    }

    /**
     * Test Case ID: UT_AM_08
     * Purpose: Test loading user details with null username
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Try to load user details with null username
     * 
     * Expected Results:
     * - UsernameNotFoundException is thrown
     */
    @Test
    @DisplayName("Test load user details with null username")
    void testLoadUserByUsernameWithNullUsername() {
        // Try to load user details with null username
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(null);
        });

        // Verify exception message
        assertTrue(exception.getMessage().contains("User Not Found with username: null"));
    }

    /**
     * Test Case ID: UT_AM_09
     * Purpose: Test loading user details with multiple roles
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database with multiple roles
     * 
     * Test Steps:
     * 1. Create a test user with multiple roles
     * 2. Load user details by username
     * 
     * Expected Results:
     * - UserDetails is loaded successfully
     * - UserDetails contains correct user information and multiple roles
     */
    @Test
    @DisplayName("Test load user details with multiple roles")
    void testLoadUserByUsernameWithMultipleRoles() {
        transactionTemplate.execute(status -> {
            // Create test user with unique username
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUserWithMultipleRoles(uniqueUsername);
            User savedUser = userRepository.save(testUser);

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(uniqueUsername);

            // Verify user details
            assertNotNull(userDetails);
            assertEquals(savedUser.getUsername(), userDetails.getUsername());
            assertEquals(savedUser.getPassword(), userDetails.getPassword());
            assertFalse(userDetails.getAuthorities().isEmpty());
            assertEquals(2, userDetails.getAuthorities().size());
            assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")));
            assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_LECTURER")));

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_10
     * Purpose: Test loading user details with empty roles
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database with no roles
     * 
     * Test Steps:
     * 1. Create a test user with no roles
     * 2. Load user details by username
     * 
     * Expected Results:
     * - UserDetails is loaded successfully
     * - UserDetails contains correct user information but no roles
     */
    @Test
    @DisplayName("Test load user details with empty roles")
    void testLoadUserByUsernameWithEmptyRoles() {
        transactionTemplate.execute(status -> {
            // Create test user with unique username
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUserWithNoRoles(uniqueUsername);
            User savedUser = userRepository.save(testUser);

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(uniqueUsername);

            // Verify user details
            assertNotNull(userDetails);
            assertEquals(savedUser.getUsername(), userDetails.getUsername());
            assertEquals(savedUser.getPassword(), userDetails.getPassword());
            assertTrue(userDetails.getAuthorities().isEmpty());

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_11
     * Purpose: Test loading user details with empty username
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Try to load user details with empty username
     * 
     * Expected Results:
     * - UsernameNotFoundException is thrown
     */
    @Test
    @DisplayName("Test load user details with empty username")
    void testLoadUserByUsernameWithEmptyUsername() {
        // Try to load user details with empty username
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("");
        });

        // Verify exception message
        assertTrue(exception.getMessage().contains("User Not Found with username: "));
    }

    /**
     * Test Case ID: UT_AM_12
     * Purpose: Test loading user details with all available roles
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database with all roles
     * 
     * Test Steps:
     * 1. Create a test user with all available roles
     * 2. Load user details by username
     * 
     * Expected Results:
     * - UserDetails is loaded successfully
     * - UserDetails contains correct user information and all roles
     */
    @Test
    @DisplayName("Test load user details with all available roles")
    void testLoadUserByUsernameWithAllRoles() {
        transactionTemplate.execute(status -> {
            // Create test user with unique username
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUserWithAllRoles(uniqueUsername);
            User savedUser = userRepository.save(testUser);

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(uniqueUsername);

            // Verify user details
            assertNotNull(userDetails);
            assertEquals(savedUser.getUsername(), userDetails.getUsername());
            assertEquals(savedUser.getPassword(), userDetails.getPassword());
            assertFalse(userDetails.getAuthorities().isEmpty());
            assertEquals(3, userDetails.getAuthorities().size());
            assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")));
            assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_LECTURER")));
            assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_13
     * Purpose: Test UserDetailsImpl methods
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Load user details
     * 3. Test all UserDetailsImpl methods
     * 
     * Expected Results:
     * - All UserDetailsImpl methods return correct values
     */
    @Test
    @DisplayName("Test UserDetailsImpl methods")
    void testUserDetailsImplMethods() {
        transactionTemplate.execute(status -> {
            // Create test user with unique username
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUser(uniqueUsername);
            User savedUser = userRepository.save(testUser);

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(uniqueUsername);

            // Verify UserDetailsImpl methods
            assertNotNull(userDetails);
            assertEquals(savedUser.getUsername(), userDetails.getUsername());
            assertEquals(savedUser.getPassword(), userDetails.getPassword());
            assertFalse(userDetails.getAuthorities().isEmpty());
            
            // Test account status methods
            assertTrue(userDetails.isAccountNonExpired());
            assertTrue(userDetails.isAccountNonLocked());
            assertTrue(userDetails.isCredentialsNonExpired());
            assertTrue(userDetails.isEnabled());

            // Test UserDetailsImpl specific methods
            if (userDetails instanceof UserDetailsImpl) {
                UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
                assertEquals(savedUser.getId(), userDetailsImpl.getId());
                assertEquals(savedUser.getEmail(), userDetailsImpl.getEmail());
            }

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_14
     * Purpose: Test load user details with special characters in username
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Try to load user details with username containing special characters
     * 
     * Expected Results:
     * - UsernameNotFoundException is thrown
     */
    @Test
    @DisplayName("Test load user details with special characters in username")
    void testLoadUserByUsernameWithSpecialCharacters() {
        // Try to load user details with username containing special characters
        String specialUsername = "testUser_!@#$%^&*()_" + UUID.randomUUID().toString().substring(0, 8);
        
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(specialUsername);
        });

        // Verify exception message
        assertTrue(exception.getMessage().contains("User Not Found with username: " + specialUsername));
    }

    /**
     * Test Case ID: UT_AM_15
     * Purpose: Test UserDetailsImpl equals method
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test users exist in database
     * 
     * Test Steps:
     * 1. Create test users
     * 2. Load user details
     * 3. Test equals method with different scenarios
     * 
     * Expected Results:
     * - Equals method returns correct results for all scenarios
     */
    @Test
    @DisplayName("Test UserDetailsImpl equals method")
    void testUserDetailsImplEquals() {
        transactionTemplate.execute(status -> {
            // Create test users with unique usernames
            String username1 = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            String username2 = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            
            User testUser1 = createTestUser(username1);
            User testUser2 = createTestUser(username2);
            
            User savedUser1 = userRepository.save(testUser1);
            User savedUser2 = userRepository.save(testUser2);

            // Load user details
            UserDetails userDetails1 = userDetailsService.loadUserByUsername(username1);
            UserDetails userDetails2 = userDetailsService.loadUserByUsername(username2);

            // Test equals method
            assertTrue(userDetails1 instanceof UserDetailsImpl);
            assertTrue(userDetails2 instanceof UserDetailsImpl);
            
            UserDetailsImpl impl1 = (UserDetailsImpl) userDetails1;
            UserDetailsImpl impl2 = (UserDetailsImpl) userDetails2;

            // Test same object
            assertTrue(impl1.equals(impl1));
            
            // Test different objects with same ID
            UserDetailsImpl impl3 = UserDetailsImpl.build(savedUser1);
            assertTrue(impl1.equals(impl3));
            
            // Test different objects with different IDs
            assertFalse(impl1.equals(impl2));
            
            // Test null object
            assertFalse(impl1.equals(null));
            
            // Test different class
            assertFalse(impl1.equals(new Object()));

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_16
     * Purpose: Test UserDetailsImpl build method
     * 
     * Prerequisites:
     * - Database is accessible
     * - Test user exists in database
     * 
     * Test Steps:
     * 1. Create a test user
     * 2. Build UserDetailsImpl using static build method
     * 3. Verify all fields are correctly set
     * 
     * Expected Results:
     * - UserDetailsImpl is built correctly with all fields
     */
    @Test
    @DisplayName("Test UserDetailsImpl build method")
    void testUserDetailsImplBuild() {
        transactionTemplate.execute(status -> {
            // Create test user with unique username
            String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
            User testUser = createTestUser(uniqueUsername);
            User savedUser = userRepository.save(testUser);

            // Build UserDetailsImpl
            UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);

            // Verify all fields
            assertNotNull(userDetails);
            assertEquals(savedUser.getId(), userDetails.getId());
            assertEquals(savedUser.getUsername(), userDetails.getUsername());
            assertEquals(savedUser.getEmail(), userDetails.getEmail());
            assertEquals(savedUser.getPassword(), userDetails.getPassword());
            assertNotNull(userDetails.getAuthorities());
            assertEquals(1, userDetails.getAuthorities().size());
            assertEquals("ROLE_STUDENT", userDetails.getAuthorities().iterator().next().getAuthority());

            // Test account status methods
            assertTrue(userDetails.isAccountNonExpired());
            assertTrue(userDetails.isAccountNonLocked());
            assertTrue(userDetails.isCredentialsNonExpired());
            assertTrue(userDetails.isEnabled());

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_17
     * Purpose: Test UserDetailsImpl build method with null user
     * 
     * Prerequisites:
     * - None
     * 
     * Test Steps:
     * 1. Try to build UserDetailsImpl with null user
     * 
     * Expected Results:
     * - NullPointerException is thrown
     */
    @Test
    @DisplayName("Test UserDetailsImpl build method with null user")
    void testUserDetailsImplBuildWithNullUser() {
        // Test building with null user
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            UserDetailsImpl.build(null);
        });

        // Verify exception
        assertNotNull(exception);
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

    // Helper method to create test user with multiple roles
    private User createTestUserWithMultipleRoles(String username) {
        Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                .orElseGet(() -> {
                    Role newRole = new Role(null, ERole.ROLE_STUDENT);
                    return roleRepository.save(newRole);
                });

        Role lecturerRole = roleRepository.findByName(ERole.ROLE_LECTURER)
                .orElseGet(() -> {
                    Role newRole = new Role(null, ERole.ROLE_LECTURER);
                    return roleRepository.save(newRole);
                });

        Profile profile = new Profile();
        profile.setFirstName("Test");
        profile.setLastName("User");

        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        roles.add(lecturerRole);

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password123");
        user.setProfile(profile);
        user.setRoles(roles);

        return user;
    }

    // Helper method to create test user with no roles
    private User createTestUserWithNoRoles(String username) {
        Profile profile = new Profile();
        profile.setFirstName("Test");
        profile.setLastName("User");

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password123");
        user.setProfile(profile);
        user.setRoles(new HashSet<>());

        return user;
    }

    // Helper method to create test user with all roles
    private User createTestUserWithAllRoles(String username) {
        Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                .orElseGet(() -> {
                    Role newRole = new Role(null, ERole.ROLE_STUDENT);
                    return roleRepository.save(newRole);
                });

        Role lecturerRole = roleRepository.findByName(ERole.ROLE_LECTURER)
                .orElseGet(() -> {
                    Role newRole = new Role(null, ERole.ROLE_LECTURER);
                    return roleRepository.save(newRole);
                });

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role(null, ERole.ROLE_ADMIN);
                    return roleRepository.save(newRole);
                });

        Profile profile = new Profile();
        profile.setFirstName("Test");
        profile.setLastName("User");

        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        roles.add(lecturerRole);
        roles.add(adminRole);

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password123");
        user.setProfile(profile);
        user.setRoles(roles);

        return user;
    }
} 