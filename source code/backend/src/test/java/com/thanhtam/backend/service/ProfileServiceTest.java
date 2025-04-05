package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.repository.ProfileRepository;
import com.thanhtam.backend.service.ProfileServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Transactional
    void testCreateProfile() {
        Profile profile = new Profile();
        profile.setFirstName("John");
        profile.setLastName("Doe");

        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        Profile result = profileService.createProfile(profile);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    void testGetAllProfiles() {
        Profile profile1 = new Profile();
        profile1.setFirstName("John");
        Profile profile2 = new Profile();
        profile2.setFirstName("Jane");

        when(profileRepository.findAll()).thenReturn(Arrays.asList(profile1, profile2));

        List<Profile> result = profileService.getAllProfiles();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(profileRepository, times(1)).findAll();
    }
}