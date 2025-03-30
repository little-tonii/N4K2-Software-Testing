package com.thanhtam.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.repository.QuestionTypeRepository;
import com.thanhtam.backend.ultilities.EQTypeCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // Enable Mockito for JUnit 5
class QuestionTypeServiceTest {

    @Mock // Create a mock for the repository dependency
    private QuestionTypeRepository questionTypeRepository;

    @InjectMocks // Create an instance of the service and inject the mocks
    private QuestionTypeServiceImpl questionTypeService;

    private QuestionType mcType;
    private QuestionType tfType;

    @BeforeEach
    void setUp() {
        // Initialize common test data before each test
        mcType = new QuestionType(1L, EQTypeCode.MC, "Multiple Choice");
        tfType = new QuestionType(2L, EQTypeCode.TF, "True/False");
    }

    @Test
    @DisplayName("Test getQuestionTypeById - Found")
    void getQuestionTypeById_WhenFound_ShouldReturnQuestionType() {
        // Arrange
        Long id = 1L;
        when(questionTypeRepository.findById(id)).thenReturn(
            Optional.of(mcType)
        );

        // Act
        Optional<QuestionType> foundType =
            questionTypeService.getQuestionTypeById(id);

        // Assert
        assertTrue(foundType.isPresent(), "QuestionType should be present");
        assertEquals(mcType.getId(), foundType.get().getId());
        assertEquals(mcType.getTypeCode(), foundType.get().getTypeCode());

        // Verify
        verify(questionTypeRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Test getQuestionTypeById - Not Found")
    void getQuestionTypeById_WhenNotFound_ShouldReturnEmptyOptional() {
        // Arrange
        Long id = 99L;
        when(questionTypeRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<QuestionType> foundType =
            questionTypeService.getQuestionTypeById(id);

        // Assert
        assertFalse(
            foundType.isPresent(),
            "QuestionType should not be present"
        );

        // Verify
        verify(questionTypeRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Test getQuestionTypeByCode - Found")
    void getQuestionTypeByCode_WhenFound_ShouldReturnQuestionType() {
        // Arrange
        EQTypeCode code = EQTypeCode.MC;
        when(questionTypeRepository.findAllByTypeCode(code)).thenReturn(
            Optional.of(mcType)
        );

        // Act
        Optional<QuestionType> foundType =
            questionTypeService.getQuestionTypeByCode(code);

        // Assert
        assertTrue(
            foundType.isPresent(),
            "QuestionType should be present for the code"
        );
        assertEquals(mcType.getId(), foundType.get().getId());
        assertEquals(code, foundType.get().getTypeCode());

        // Verify
        verify(questionTypeRepository, times(1)).findAllByTypeCode(code);
    }

    @Test
    @DisplayName("Test getQuestionTypeByCode - Not Found")
    void getQuestionTypeByCode_WhenNotFound_ShouldReturnEmptyOptional() {
        // Arrange
        EQTypeCode code = EQTypeCode.MS; // Assume MS doesn't exist or repo returns empty
        when(questionTypeRepository.findAllByTypeCode(code)).thenReturn(
            Optional.empty()
        );

        // Act
        Optional<QuestionType> foundType =
            questionTypeService.getQuestionTypeByCode(code);

        // Assert
        assertFalse(
            foundType.isPresent(),
            "QuestionType should not be present for the code"
        );

        // Verify
        verify(questionTypeRepository, times(1)).findAllByTypeCode(code);
    }

    @Test
    @DisplayName("Test getQuestionTypeList - Should return list of types")
    void getQuestionTypeList_ShouldReturnListOfTypes() {
        // Arrange
        List<QuestionType> expectedList = Arrays.asList(mcType, tfType);
        when(questionTypeRepository.findAll()).thenReturn(expectedList);

        // Act
        List<QuestionType> actualList =
            questionTypeService.getQuestionTypeList();

        // Assert
        assertNotNull(actualList);
        assertEquals(2, actualList.size(), "List should contain 2 types");
        assertEquals(expectedList, actualList, "List content should match");

        // Verify
        verify(questionTypeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test getQuestionTypeList - Should return empty list")
    void getQuestionTypeList_WhenNoTypes_ShouldReturnEmptyList() {
        // Arrange
        List<QuestionType> expectedEmptyList = new ArrayList<>();
        when(questionTypeRepository.findAll()).thenReturn(expectedEmptyList);

        // Act
        List<QuestionType> actualList =
            questionTypeService.getQuestionTypeList();

        // Assert
        assertNotNull(actualList);
        assertTrue(actualList.isEmpty(), "List should be empty");

        // Verify
        verify(questionTypeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test saveQuestionType - Should call repository save")
    void saveQuestionType_ShouldCallRepositorySave() {
        // Arrange
        QuestionType newType = new QuestionType(
            null,
            EQTypeCode.MS,
            "Multiple Select"
        );
        ArgumentCaptor<QuestionType> typeCaptor = ArgumentCaptor.forClass(
            QuestionType.class
        );
        // when(questionTypeRepository.save(any(QuestionType.class))).thenReturn(newType); // Optional if needed

        // Act
        questionTypeService.saveQuestionType(newType);

        // Verify
        verify(questionTypeRepository, times(1)).save(typeCaptor.capture());
        assertEquals(EQTypeCode.MS, typeCaptor.getValue().getTypeCode());
        assertEquals("Multiple Select", typeCaptor.getValue().getDescription());
    }

    @Test
    @DisplayName("Test delete - Should call repository deleteById")
    void delete_ShouldCallRepositoryDeleteById() {
        // Arrange
        Long idToDelete = 1L;
        // doNothing().when(questionTypeRepository).deleteById(idToDelete); // Usually not needed

        // Act
        questionTypeService.delete(idToDelete);

        // Verify
        verify(questionTypeRepository, times(1)).deleteById(idToDelete);
    }

    @Test
    @DisplayName("Test existsById - Should return true when ID exists")
    void existsById_WhenIdExists_ShouldReturnTrue() {
        // Arrange
        Long existingId = 1L;
        when(questionTypeRepository.existsById(existingId)).thenReturn(true);

        // Act
        boolean exists = questionTypeService.existsById(existingId);

        // Assert
        assertTrue(exists, "Should return true for existing ID");

        // Verify
        verify(questionTypeRepository, times(1)).existsById(existingId);
    }

    @Test
    @DisplayName("Test existsById - Should return false when ID does not exist")
    void existsById_WhenIdDoesNotExist_ShouldReturnFalse() {
        // Arrange
        Long nonExistingId = 999L;
        when(questionTypeRepository.existsById(nonExistingId)).thenReturn(
            false
        );

        // Act
        boolean exists = questionTypeService.existsById(nonExistingId);

        // Assert
        assertFalse(exists, "Should return false for non-existing ID");

        // Verify
        verify(questionTypeRepository, times(1)).existsById(nonExistingId);
    }
}
