package com.thanhtam.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.thanhtam.backend.repository.ChoiceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // Enable Mockito for JUnit 5
class ChoiceServiceTest {

    @Mock // Create a mock for the repository dependency
    private ChoiceRepository choiceRepository;

    @InjectMocks // Create an instance of the service and inject the mocks
    private ChoiceServiceImpl choiceService;

    @Test
    @DisplayName(
        "Test findIsCorrectedById - Should return correct integer value"
    )
    void findIsCorrectedById_ShouldReturnCorrectIntegerValue() {
        // Arrange
        Long choiceId = 1L;
        Integer expectedIsCorrected = 1; // Example value for a correct choice

        // Define the behavior of the mock repository
        when(choiceRepository.findIsCorrectedById(choiceId)).thenReturn(
            expectedIsCorrected
        );

        // Act
        Integer actualIsCorrected = choiceService.findIsCorrectedById(choiceId);

        // Assert
        assertNotNull(
            actualIsCorrected,
            "The returned value should not be null"
        );
        assertEquals(
            expectedIsCorrected,
            actualIsCorrected,
            "The returned isCorrected value should match the expected value"
        );

        // Verify that the repository method was called exactly once with the correct ID
        verify(choiceRepository, times(1)).findIsCorrectedById(choiceId);
    }

    @Test
    @DisplayName(
        "Test findIsCorrectedById - Should return 0 for incorrect choice"
    )
    void findIsCorrectedById_ShouldReturnZeroForIncorrectChoice() {
        // Arrange
        Long choiceId = 2L;
        Integer expectedIsCorrected = 0; // Example value for an incorrect choice

        when(choiceRepository.findIsCorrectedById(choiceId)).thenReturn(
            expectedIsCorrected
        );

        // Act
        Integer actualIsCorrected = choiceService.findIsCorrectedById(choiceId);

        // Assert
        assertNotNull(actualIsCorrected);
        assertEquals(expectedIsCorrected, actualIsCorrected);

        // Verify
        verify(choiceRepository, times(1)).findIsCorrectedById(choiceId);
    }

    @Test
    @DisplayName(
        "Test findIsCorrectedById - Repository returns null (e.g., not found)"
    )
    void findIsCorrectedById_WhenRepositoryReturnsNull_ShouldReturnNull() {
        // Arrange
        Long nonExistentChoiceId = 999L;

        // Define the behavior of the mock repository to return null
        when(
            choiceRepository.findIsCorrectedById(nonExistentChoiceId)
        ).thenReturn(null);

        // Act
        Integer actualIsCorrected = choiceService.findIsCorrectedById(
            nonExistentChoiceId
        );

        // Assert
        assertNull(
            actualIsCorrected,
            "The service should return null if the repository returns null"
        );

        // Verify
        verify(choiceRepository, times(1)).findIsCorrectedById(
            nonExistentChoiceId
        );
    }

    @Test
    @DisplayName("Test findChoiceTextById - Should return correct text")
    void findChoiceTextById_ShouldReturnCorrectText() {
        // Arrange
        Long choiceId = 3L;
        String expectedText = "This is the choice text";

        when(choiceRepository.findChoiceTextById(choiceId)).thenReturn(
            expectedText
        );

        // Act
        String actualText = choiceService.findChoiceTextById(choiceId);

        // Assert
        assertNotNull(actualText, "The returned text should not be null");
        assertEquals(
            expectedText,
            actualText,
            "The returned choice text should match the expected text"
        );

        // Verify
        verify(choiceRepository, times(1)).findChoiceTextById(choiceId);
    }

    @Test
    @DisplayName(
        "Test findChoiceTextById - Repository returns null (e.g., not found)"
    )
    void findChoiceTextById_WhenRepositoryReturnsNull_ShouldReturnNull() {
        // Arrange
        Long nonExistentChoiceId = 888L;

        when(
            choiceRepository.findChoiceTextById(nonExistentChoiceId)
        ).thenReturn(null);

        // Act
        String actualText = choiceService.findChoiceTextById(
            nonExistentChoiceId
        );

        // Assert
        assertNull(
            actualText,
            "The service should return null if the repository returns null"
        );

        // Verify
        verify(choiceRepository, times(1)).findChoiceTextById(
            nonExistentChoiceId
        );
    }
}
