package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.repository.QuestionTypeRepository;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionTypeServiceImplTest {

    @Mock
    private QuestionTypeRepository questionTypeRepository;

    @InjectMocks
    private QuestionTypeServiceImpl questionTypeService;

    private QuestionType multipleChoiceType;
    private QuestionType trueFalseType;
    private QuestionType multipleSelectType;

    @BeforeEach
    void setUp() {
        // Setup test data
        multipleChoiceType = new QuestionType();
        multipleChoiceType.setId(1L);
        multipleChoiceType.setTypeCode(EQTypeCode.MC);
        multipleChoiceType.setDescription("Multiple Choice");

        trueFalseType = new QuestionType();
        trueFalseType.setId(2L);
        trueFalseType.setTypeCode(EQTypeCode.TF);
        trueFalseType.setDescription("True/False");

        multipleSelectType = new QuestionType();
        multipleSelectType.setId(3L);
        multipleSelectType.setTypeCode(EQTypeCode.MS);
        multipleSelectType.setDescription("Multiple Select");
    }

    /**
     * Test Case ID: UT_QBM_17
     * Purpose: Test retrieving a question type by its ID
     * 
     * Prerequisites:
     * - Question type exists in repository
     * 
     * Test Steps:
     * 1. Mock repository to return a question type
     * 2. Call getQuestionTypeById
     * 
     * Expected Results:
     * - Returns the correct question type
     */
    @Test
    @DisplayName("Should return question type when valid ID is provided")
    void getQuestionTypeById_ShouldReturnQuestionType_WhenValidIdProvided() {
        when(questionTypeRepository.findById(1L)).thenReturn(Optional.of(multipleChoiceType));

        Optional<QuestionType> result = questionTypeService.getQuestionTypeById(1L);

        assertTrue(result.isPresent());
        assertEquals(multipleChoiceType, result.get());
        verify(questionTypeRepository).findById(1L);
    }

    /**
     * Test Case ID: UT_QBM_18
     * Purpose: Test retrieving a question type by its code
     */
    @Test
    @DisplayName("Should return question type when valid code is provided")
    void getQuestionTypeByCode_ShouldReturnQuestionType_WhenValidCodeProvided() {
        when(questionTypeRepository.findAllByTypeCode(EQTypeCode.MC))
            .thenReturn(Optional.of(multipleChoiceType));

        Optional<QuestionType> result = questionTypeService.getQuestionTypeByCode(EQTypeCode.MC);

        assertTrue(result.isPresent());
        assertEquals(multipleChoiceType, result.get());
        verify(questionTypeRepository).findAllByTypeCode(EQTypeCode.MC);
    }

    /**
     * Test Case ID: UT_QBM_19
     * Purpose: Test retrieving all question types
     */
    @Test
    @DisplayName("Should return all question types")
    void getQuestionTypeList_ShouldReturnAllQuestionTypes() {
        List<QuestionType> expectedTypes = Arrays.asList(multipleChoiceType, trueFalseType, multipleSelectType);
        when(questionTypeRepository.findAll()).thenReturn(expectedTypes);

        List<QuestionType> result = questionTypeService.getQuestionTypeList();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedTypes, result);
        verify(questionTypeRepository).findAll();
    }

    /**
     * Test Case ID: UT_QBM_20
     * Purpose: Test saving a question type
     */
    @Test
    @DisplayName("Should save question type successfully")
    void saveQuestionType_ShouldSaveSuccessfully() {
        when(questionTypeRepository.save(any(QuestionType.class))).thenReturn(multipleChoiceType);

        questionTypeService.saveQuestionType(multipleChoiceType);

        verify(questionTypeRepository).save(multipleChoiceType);
    }

    /**
     * Test Case ID: UT_QBM_21
     * Purpose: Test deleting a question type
     */
    @Test
    @DisplayName("Should delete question type when valid ID is provided")
    void delete_ShouldDeleteQuestionType_WhenValidIdProvided() {
        doNothing().when(questionTypeRepository).deleteById(1L);

        questionTypeService.delete(1L);

        verify(questionTypeRepository).deleteById(1L);
    }

    /**
     * Test Case ID: UT_QBM_22
     * Purpose: Test checking if question type exists
     */
    @Test
    @DisplayName("Should return true when question type exists")
    void existsById_ShouldReturnTrue_WhenQuestionTypeExists() {
        when(questionTypeRepository.existsById(1L)).thenReturn(true);

        boolean result = questionTypeService.existsById(1L);

        assertTrue(result);
        verify(questionTypeRepository).existsById(1L);
    }

    /**
     * Test Case ID: UT_QBM_23
     * Purpose: Test checking if question type does not exist
     */
    @Test
    @DisplayName("Should return false when question type does not exist")
    void existsById_ShouldReturnFalse_WhenQuestionTypeDoesNotExist() {
        when(questionTypeRepository.existsById(999L)).thenReturn(false);

        boolean result = questionTypeService.existsById(999L);

        assertFalse(result);
        verify(questionTypeRepository).existsById(999L);
    }

    /**
     * Test Case ID: UT_QBM_24
     * Purpose: Test retrieving non-existent question type by ID
     */
    @Test
    @DisplayName("Should return empty when question type ID does not exist")
    void getQuestionTypeById_ShouldReturnEmpty_WhenInvalidIdProvided() {
        when(questionTypeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<QuestionType> result = questionTypeService.getQuestionTypeById(999L);

        assertFalse(result.isPresent());
        verify(questionTypeRepository).findById(999L);
    }

    /**
     * Test Case ID: UT_QBM_25
     * Purpose: Test retrieving non-existent question type by code
     */
    @Test
    @DisplayName("Should return empty when question type code does not exist")
    void getQuestionTypeByCode_ShouldReturnEmpty_WhenInvalidCodeProvided() {
        when(questionTypeRepository.findAllByTypeCode(EQTypeCode.TF)).thenReturn(Optional.empty());

        Optional<QuestionType> result = questionTypeService.getQuestionTypeByCode(EQTypeCode.TF);

        assertFalse(result.isPresent());
        verify(questionTypeRepository).findAllByTypeCode(EQTypeCode.TF);
    }
} 