package com.thanhtam.backend.service;

import com.thanhtam.backend.repository.ChoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChoiceServiceImplTest {

    @Mock
    private ChoiceRepository choiceRepository;

    @InjectMocks
    private ChoiceServiceImpl choiceService;

    private static final Long VALID_CHOICE_ID = 1L;
    private static final Long INVALID_CHOICE_ID = 999L;
    private static final String EXPECTED_CHOICE_TEXT = "Test Choice";
    private static final Integer EXPECTED_CORRECTED_VALUE = 1;

    @BeforeEach
    void setUp() {
        // No common setup needed
    }

    /**
     * Test Case ID: UT_QBM_01
     * Purpose: Test finding isCorrected value for a valid choice ID
     * 
     * Prerequisites:
     * - Choice exists in repository
     * 
     * Test Steps:
     * 1. Mock repository to return isCorrected value
     * 2. Call findIsCorrectedById with valid ID
     * 
     * Expected Results:
     * - Returns the correct isCorrected value
     */
    @Test
    @DisplayName("Should return isCorrected value when valid ID is provided")
    void findIsCorrectedById_ShouldReturnValue_WhenValidIdProvided() {
        when(choiceRepository.findIsCorrectedById(VALID_CHOICE_ID)).thenReturn(EXPECTED_CORRECTED_VALUE);

        Integer result = choiceService.findIsCorrectedById(VALID_CHOICE_ID);

        assertNotNull(result);
        assertEquals(EXPECTED_CORRECTED_VALUE, result);
        verify(choiceRepository).findIsCorrectedById(VALID_CHOICE_ID);
    }

    /**
     * Test Case ID: UT_QBM_21
     * Purpose: Test finding isCorrected value for an invalid choice ID
     */
    @Test
    @DisplayName("Should return null when invalid ID is provided for isCorrected")
    void findIsCorrectedById_ShouldReturnNull_WhenInvalidIdProvided() {
        when(choiceRepository.findIsCorrectedById(INVALID_CHOICE_ID)).thenReturn(null);

        Integer result = choiceService.findIsCorrectedById(INVALID_CHOICE_ID);

        assertNull(result);
        verify(choiceRepository).findIsCorrectedById(INVALID_CHOICE_ID);
    }

    /**
     * Test Case ID: UT_QBM_22
     * Purpose: Test finding choice text for a valid choice ID
     */
    @Test
    @DisplayName("Should return choice text when valid ID is provided")
    void findChoiceTextById_ShouldReturnText_WhenValidIdProvided() {
        when(choiceRepository.findChoiceTextById(VALID_CHOICE_ID)).thenReturn(EXPECTED_CHOICE_TEXT);

        String result = choiceService.findChoiceTextById(VALID_CHOICE_ID);

        assertNotNull(result);
        assertEquals(EXPECTED_CHOICE_TEXT, result);
        verify(choiceRepository).findChoiceTextById(VALID_CHOICE_ID);
    }

    /**
     * Test Case ID: UT_QBM_23
     * Purpose: Test finding choice text for an invalid choice ID
     */
    @Test
    @DisplayName("Should return null when invalid ID is provided for choice text")
    void findChoiceTextById_ShouldReturnNull_WhenInvalidIdProvided() {
        when(choiceRepository.findChoiceTextById(INVALID_CHOICE_ID)).thenReturn(null);

        String result = choiceService.findChoiceTextById(INVALID_CHOICE_ID);

        assertNull(result);
        verify(choiceRepository).findChoiceTextById(INVALID_CHOICE_ID);
    }

    /**
     * Test Case ID: UT_QBM_24
     * Purpose: Test finding isCorrected value with zero value
     */
    @Test
    @DisplayName("Should return zero when choice is not corrected")
    void findIsCorrectedById_ShouldReturnZero_WhenChoiceNotCorrected() {
        when(choiceRepository.findIsCorrectedById(VALID_CHOICE_ID)).thenReturn(0);

        Integer result = choiceService.findIsCorrectedById(VALID_CHOICE_ID);

        assertNotNull(result);
        assertEquals(0, result);
        verify(choiceRepository).findIsCorrectedById(VALID_CHOICE_ID);
    }

    /**
     * Test Case ID: UT_QBM_25
     * Purpose: Test finding choice text with empty string
     */
    @Test
    @DisplayName("Should return empty string when choice text is empty")
    void findChoiceTextById_ShouldReturnEmptyString_WhenChoiceTextEmpty() {
        when(choiceRepository.findChoiceTextById(VALID_CHOICE_ID)).thenReturn("");

        String result = choiceService.findChoiceTextById(VALID_CHOICE_ID);

        assertNotNull(result);
        assertEquals("", result);
        verify(choiceRepository).findChoiceTextById(VALID_CHOICE_ID);
    }
} 