package com.thanhtam.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.repository.PartRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class) // Enable Mockito for JUnit 5
class PartServiceTest {

    @Mock // Create a mock for the repository dependency
    private PartRepository partRepository;

    @InjectMocks // Create an instance of the service and inject the mocks
    private PartServiceImpl partService;

    private Course course1;
    private Part part1;
    private Part part2;

    @BeforeEach
    void setUp() {
        // Initialize common test data before each test
        course1 = new Course();
        course1.setId(1L);
        course1.setName("Software Testing");
        course1.setCourseCode("SWT301");

        part1 = new Part();
        part1.setId(10L);
        part1.setName("Chapter 1: Introduction");
        part1.setCourse(course1); // Link part to course

        part2 = new Part();
        part2.setId(11L);
        part2.setName("Chapter 2: Test Levels");
        part2.setCourse(course1); // Link part to course
    }

    @Test
    @DisplayName("Test savePart - Should call repository save")
    void savePart_ShouldCallRepositorySave() {
        // Arrange
        Part newPart = new Part(null, "Chapter 3: Test Techniques", course1);
        // Use ArgumentCaptor to verify the object passed to save
        ArgumentCaptor<Part> partCaptor = ArgumentCaptor.forClass(Part.class);

        // Act
        partService.savePart(newPart);

        // Verify
        // Check that repository.save was called exactly once
        verify(partRepository, times(1)).save(partCaptor.capture());
        // Optionally assert properties of the captured object
        assertEquals(
            "Chapter 3: Test Techniques",
            partCaptor.getValue().getName()
        );
        assertEquals(course1, partCaptor.getValue().getCourse());
    }

    @Test
    @DisplayName(
        "Test getPartLisByCourse (Paged) - Should return page of parts"
    )
    void getPartLisByCourse_ShouldReturnPageOfParts() {
        // Arrange
        Long courseId = course1.getId();
        Pageable pageable = PageRequest.of(0, 5); // Example: page 0, size 5
        List<Part> partsInPage = Arrays.asList(part1, part2);
        Page<Part> expectedPage = new PageImpl<>(
            partsInPage,
            pageable,
            partsInPage.size()
        ); // Create a mock Page

        // Define the behavior of the mock repository
        when(partRepository.findAllByCourseId(courseId, pageable)).thenReturn(
            expectedPage
        );

        // Act
        Page<Part> actualPage = partService.getPartLisByCourse(
            pageable,
            courseId
        );

        // Assert
        assertNotNull(actualPage);
        assertEquals(
            2,
            actualPage.getNumberOfElements(),
            "Page should contain 2 elements"
        );
        assertEquals(
            partsInPage,
            actualPage.getContent(),
            "Page content should match"
        );
        assertEquals(
            expectedPage.getTotalElements(),
            actualPage.getTotalElements()
        );

        // Verify
        verify(partRepository, times(1)).findAllByCourseId(courseId, pageable);
    }

    @Test
    @DisplayName("Test getPartLisByCourse (Paged) - Should return empty page")
    void getPartLisByCourse_WhenNoParts_ShouldReturnEmptyPage() {
        // Arrange
        Long courseIdWithNoParts = 99L;
        Pageable pageable = PageRequest.of(0, 5);
        Page<Part> expectedEmptyPage = new PageImpl<>(
            new ArrayList<>(),
            pageable,
            0
        ); // Empty page

        when(
            partRepository.findAllByCourseId(courseIdWithNoParts, pageable)
        ).thenReturn(expectedEmptyPage);

        // Act
        Page<Part> actualPage = partService.getPartLisByCourse(
            pageable,
            courseIdWithNoParts
        );

        // Assert
        assertNotNull(actualPage);
        assertTrue(
            actualPage.getContent().isEmpty(),
            "Page content should be empty"
        );
        assertEquals(0, actualPage.getTotalElements());

        // Verify
        verify(partRepository, times(1)).findAllByCourseId(
            courseIdWithNoParts,
            pageable
        );
    }

    @Test
    @DisplayName(
        "Test getPartListByCourse (List) - Should return list of parts"
    )
    void getPartListByCourse_ShouldReturnListOfParts() {
        // Arrange
        List<Part> expectedParts = Arrays.asList(part1, part2);
        when(partRepository.findAllByCourse(course1)).thenReturn(expectedParts);

        // Act
        List<Part> actualParts = partService.getPartListByCourse(course1);

        // Assert
        assertNotNull(actualParts);
        assertEquals(2, actualParts.size(), "List should contain 2 parts");
        assertEquals(expectedParts, actualParts, "List content should match");

        // Verify
        verify(partRepository, times(1)).findAllByCourse(course1);
    }

    @Test
    @DisplayName("Test getPartListByCourse (List) - Should return empty list")
    void getPartListByCourse_WhenNoParts_ShouldReturnEmptyList() {
        // Arrange
        Course courseWithNoParts = new Course();
        courseWithNoParts.setId(99L);
        List<Part> expectedEmptyList = new ArrayList<>();
        when(partRepository.findAllByCourse(courseWithNoParts)).thenReturn(
            expectedEmptyList
        );

        // Act
        List<Part> actualParts = partService.getPartListByCourse(
            courseWithNoParts
        );

        // Assert
        assertNotNull(actualParts);
        assertTrue(actualParts.isEmpty(), "List should be empty");

        // Verify
        verify(partRepository, times(1)).findAllByCourse(courseWithNoParts);
    }

    @Test
    @DisplayName("Test findPartById - Found")
    void findPartById_WhenFound_ShouldReturnPart() {
        // Arrange
        Long partId = part1.getId(); // 10L
        when(partRepository.findById(partId)).thenReturn(Optional.of(part1));

        // Act
        Optional<Part> foundPart = partService.findPartById(partId);

        // Assert
        assertTrue(foundPart.isPresent(), "Part should be present");
        assertEquals(part1.getId(), foundPart.get().getId());
        assertEquals(part1.getName(), foundPart.get().getName());

        // Verify
        verify(partRepository, times(1)).findById(partId);
    }

    @Test
    @DisplayName("Test findPartById - Not Found")
    void findPartById_WhenNotFound_ShouldReturnEmptyOptional() {
        // Arrange
        Long nonExistentPartId = 999L;
        when(partRepository.findById(nonExistentPartId)).thenReturn(
            Optional.empty()
        );

        // Act
        Optional<Part> foundPart = partService.findPartById(nonExistentPartId);

        // Assert
        assertFalse(foundPart.isPresent(), "Part should not be present");

        // Verify
        verify(partRepository, times(1)).findById(nonExistentPartId);
    }

    @Test
    @DisplayName("Test existsById - Should return true when ID exists")
    void existsById_WhenIdExists_ShouldReturnTrue() {
        // Arrange
        Long existingId = part1.getId(); // 10L
        when(partRepository.existsById(existingId)).thenReturn(true);

        // Act
        boolean exists = partService.existsById(existingId);

        // Assert
        assertTrue(exists, "Should return true for existing ID");

        // Verify
        verify(partRepository, times(1)).existsById(existingId);
    }

    @Test
    @DisplayName("Test existsById - Should return false when ID does not exist")
    void existsById_WhenIdDoesNotExist_ShouldReturnFalse() {
        // Arrange
        Long nonExistingId = 888L;
        when(partRepository.existsById(nonExistingId)).thenReturn(false);

        // Act
        boolean exists = partService.existsById(nonExistingId);

        // Assert
        assertFalse(exists, "Should return false for non-existing ID");

        // Verify
        verify(partRepository, times(1)).existsById(nonExistingId);
    }
}
