package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.repository.ProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ProfileServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceTest.class);

    @Autowired
    private ProfileService profileService;

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
                    // Find all test profiles
                    List<Profile> testProfiles = profileRepository.findAll().stream()
                        .filter(profile -> profile.getFirstName() != null && (
                            profile.getFirstName().startsWith("Test_")))
                        .collect(Collectors.toList());

                    logger.info("Found {} test profiles to clean up", testProfiles.size());

                    // Delete all test profiles
                    for (Profile profile : testProfiles) {
                        try {
                            profileRepository.delete(profile);
                            logger.info("Successfully deleted test profile: {}", profile.getFirstName());
                        } catch (Exception e) {
                            logger.error("Error deleting profile {}: {}", profile.getFirstName(), e.getMessage());
                            status.setRollbackOnly();
                        }
                    }

                    // Verify cleanup
                    long remainingTestProfiles = profileRepository.findAll().stream()
                        .filter(profile -> profile.getFirstName() != null && (
                            profile.getFirstName().startsWith("Test_")))
                        .count();
                    
                    if (remainingTestProfiles > 0) {
                        logger.error("Cleanup incomplete: {} test profiles still exist", remainingTestProfiles);
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
     * Test Case ID: UT_AM_01
     * Purpose: Test profile creation with valid data
     * 
     * Prerequisites:
     * - Database is accessible
     * 
     * Test Steps:
     * 1. Create a new profile with valid data
     * 2. Save the profile using profileService
     * 3. Verify the profile was saved correctly
     * 
     * Expected Results:
     * - Profile is created successfully
     * - All profile data is saved correctly
     * - Profile can be retrieved from database
     */
    @Test
    @DisplayName("Test create profile with valid data")
    void testCreateProfileWithValidData() {
        transactionTemplate.execute(status -> {
            // Create a test profile
            String uniqueName = "Test_" + UUID.randomUUID().toString().substring(0, 8);
            Profile profile = createTestProfile(uniqueName);

            // Save the profile
            Profile savedProfile = profileService.createProfile(profile);

            // Verify the profile was saved correctly
            assertNotNull(savedProfile);
            assertNotNull(savedProfile.getId());
            assertEquals(uniqueName, savedProfile.getFirstName());
            assertEquals("Last", savedProfile.getLastName());
            assertEquals("test.jpg", savedProfile.getImage());

            // Verify we can retrieve the profile from the database
            Profile retrievedProfile = profileRepository.findById(savedProfile.getId()).orElse(null);
            assertNotNull(retrievedProfile);
            assertEquals(savedProfile.getId(), retrievedProfile.getId());
            assertEquals(savedProfile.getFirstName(), retrievedProfile.getFirstName());
            assertEquals(savedProfile.getLastName(), retrievedProfile.getLastName());
            assertEquals(savedProfile.getImage(), retrievedProfile.getImage());

            return null;
        });
    }

    /**
     * Test Case ID: UT_AM_02
     * Purpose: Test retrieving all profiles
     * 
     * Prerequisites:
     * - Database is accessible
     * - Multiple test profiles exist in database
     * 
     * Test Steps:
     * 1. Create multiple test profiles
     * 2. Retrieve all profiles
     * 
     * Expected Results:
     * - All profiles are retrieved successfully
     * - Retrieved profiles match created profiles
     */
    @Test
    @DisplayName("Test get all profiles")
    void testGetAllProfiles() {
        transactionTemplate.execute(status -> {
            // Create multiple test profiles
            for (int i = 0; i < 3; i++) {
                String uniqueName = "Test_" + UUID.randomUUID().toString().substring(0, 8);
                Profile profile = createTestProfile(uniqueName);
                profileService.createProfile(profile);
                logger.info("Created test profile: {}", uniqueName);
            }

            // Get all profiles
            List<Profile> profiles = profileService.getAllProfiles();

            // Verify profiles were retrieved correctly
            assertNotNull(profiles);
            assertTrue(profiles.size() >= 3);
            assertTrue(profiles.stream()
                .anyMatch(profile -> profile.getFirstName().startsWith("Test_")));

            return null;
        });
    }

    // Helper method to create test profile
    private Profile createTestProfile(String firstName) {
        Profile profile = new Profile();
        profile.setFirstName(firstName);
        profile.setLastName("Last");
        profile.setImage("test.jpg");
        return profile;
    }
} 