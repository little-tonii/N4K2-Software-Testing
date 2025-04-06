package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.repository.IntakeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IntakeServiceTest {

    @Mock
    private IntakeRepository intakeRepository;

    @InjectMocks
    private IntakeServiceImpl intakeService;

    private Intake testIntake;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testIntake = new Intake();
        testIntake.setId(1L);
        testIntake.setName("Test Intake");
        testIntake.setIntakeCode("INT001");
    }

    /**
     * Test Case: Find intake by code
     * Expected Behavior:
     * - Should return the intake if found
     * - Should call intakeRepository.findByIntakeCode() with correct code
     */
    @Test
    @DisplayName("Test findByCode")
    void testFindByCode() {
        // Arrange
        String intakeCode = "INT001";
        when(intakeRepository.findByIntakeCode(intakeCode)).thenReturn(Optional.of(testIntake));

        // Act
        Intake result = intakeService.findByCode(intakeCode);

        // Assert
        assertNotNull(result);
        assertEquals(testIntake.getIntakeCode(), result.getIntakeCode());
        verify(intakeRepository).findByIntakeCode(intakeCode);
    }

    /**
     * Test Case: Find intake by ID
     * Expected Behavior:
     * - Should return the intake if found
     * - Should call intakeRepository.findById() with correct ID
     */
    @Test
    @DisplayName("Test findById")
    void testFindById() {
        // Arrange
        Long intakeId = 1L;
        when(intakeRepository.findById(intakeId)).thenReturn(Optional.of(testIntake));

        // Act
        Optional<Intake> result = intakeService.findById(intakeId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testIntake.getId(), result.get().getId());
        verify(intakeRepository).findById(intakeId);
    }

    /**
     * Test Case: Find all intakes
     * Expected Behavior:
     * - Should return all intakes
     * - Should call intakeRepository.findAll() exactly once
     */
    @Test
    @DisplayName("Test findAll")
    void testFindAll() {
        // Arrange
        when(intakeRepository.findAll()).thenReturn(Collections.singletonList(testIntake));

        // Act
        List<Intake> result = intakeService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(intakeRepository).findAll();
    }

    @Test
    @DisplayName("Test findByCode when intake is not found")
    void testFindByCode_NotFound() {
        // Arrange
        String intakeCode = "NONEXISTENT";
        when(intakeRepository.findByIntakeCode(intakeCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            intakeService.findByCode(intakeCode);
        });
    }

    @Test
    @DisplayName("Test findByCode with null code")
    void testFindByCode_NullCode() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            intakeService.findByCode(null);
        });
    }

    @Test
    @DisplayName("Test findById with null id")
    void testFindById_NullId() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            intakeService.findById(null);
        });
    }
}