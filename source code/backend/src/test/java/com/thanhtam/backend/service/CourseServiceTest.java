package com.thanhtam.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Intake; // Import Intake if needed for setup
import com.thanhtam.backend.repository.CourseRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class) // Enable Mockito for JUnit 5
class CourseServiceTest {

    @Mock // Create a mock for the repository dependency
    private CourseRepository courseRepository;

    @InjectMocks // Create an instance of the service and inject the mocks
    private CourseServiceImpl courseService;

    private Course course1;
    private Course course2;
    private Intake intake1;

    @BeforeEach
    void setUp() {
        // Initialize common test data before each test
        intake1 = new Intake(1L, "Spring 2024", "SP24");

        course1 = new Course();
        course1.setId(1L);
        course1.setName("Software Testing");
        course1.setCourseCode("SWT301");
        course1.setImgUrl("swt.jpg");
        course1.setIntakes(Arrays.asList(intake1)); // Link intake to course

        course2 = new Course();
        course2.setId(2L);
        course2.setName("Software Engineering");
        course2.setCourseCode("SWE301");
        course2.setImgUrl("swe.jpg");
        course2.setIntakes(new ArrayList<>()); // No intakes for this one initially
    }

    @Test
    @DisplayName("Test getCourseById - Found")
    void getCourseById_WhenFound_ShouldReturnCourse() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.findById(courseId)).thenReturn(
            Optional.of(course1)
        );

        // Act
        Optional<Course> foundCourse = courseService.getCourseById(courseId);

        // Assert
        assertTrue(foundCourse.isPresent(), "Course should be present");
        assertEquals(course1.getId(), foundCourse.get().getId());
        assertEquals(course1.getName(), foundCourse.get().getName());

        // Verify
        verify(courseRepository, times(1)).findById(courseId);
    }

    @Test
    @DisplayName("Test getCourseById - Not Found")
    void getCourseById_WhenNotFound_ShouldReturnEmptyOptional() {
        // Arrange
        Long courseId = 99L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act
        Optional<Course> foundCourse = courseService.getCourseById(courseId);

        // Assert
        assertFalse(foundCourse.isPresent(), "Course should not be present");

        // Verify
        verify(courseRepository, times(1)).findById(courseId);
    }

    @Test
    @DisplayName("Test getCourseList - Should return list of courses")
    void getCourseList_ShouldReturnListOfCourses() {
        // Arrange
        List<Course> expectedCourses = Arrays.asList(course1, course2);
        when(courseRepository.findAll()).thenReturn(expectedCourses);

        // Act
        List<Course> actualCourses = courseService.getCourseList();

        // Assert
        assertNotNull(actualCourses);
        assertEquals(2, actualCourses.size());
        assertEquals(expectedCourses, actualCourses);

        // Verify
        verify(courseRepository, times(1)).findAll(); // Verify findAll() without pageable is called
    }

    @Test
    @DisplayName(
        "Test getCourseList - Should return empty list when no courses"
    )
    void getCourseList_ShouldReturnEmptyListWhenNoCourses() {
        // Arrange
        List<Course> expectedCourses = new ArrayList<>();
        when(courseRepository.findAll()).thenReturn(expectedCourses);

        // Act
        List<Course> actualCourses = courseService.getCourseList();

        // Assert
        assertNotNull(actualCourses);
        assertTrue(actualCourses.isEmpty());

        // Verify
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test getCourseListByPage - Should return page of courses")
    void getCourseListByPage_ShouldReturnPageOfCourses() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10); // Example page request
        List<Course> coursesInPage = Arrays.asList(course1, course2);
        Page<Course> expectedPage = new PageImpl<>(
            coursesInPage,
            pageable,
            coursesInPage.size()
        ); // Create a mock Page

        when(courseRepository.findAll(pageable)).thenReturn(expectedPage); // Mock the findAll(Pageable) method

        // Act
        Page<Course> actualPage = courseService.getCourseListByPage(pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getNumberOfElements());
        assertEquals(expectedPage.getContent(), actualPage.getContent());
        assertEquals(
            expectedPage.getTotalElements(),
            actualPage.getTotalElements()
        );

        // Verify
        verify(courseRepository, times(1)).findAll(pageable); // Verify findAll(Pageable) is called
    }

    @Test
    @DisplayName("Test saveCourse - Should call repository save")
    void saveCourse_ShouldCallRepositorySave() {
        // Arrange
        Course newCourse = new Course(
            null,
            "PRJ301",
            "Project Management",
            "prj.png",
            new ArrayList<>()
        );
        // Mock the save method to return the object itself (common practice)
        when(courseRepository.save(any(Course.class))).thenReturn(newCourse);

        // Act
        courseService.saveCourse(newCourse);

        // Verify
        // Check that repository.save was called exactly once with the newCourse object
        verify(courseRepository, times(1)).save(newCourse);
        // You could also use ArgumentCaptor here if you needed to inspect the saved object more deeply
    }

    @Test
    @DisplayName("Test delete - Should call repository deleteById")
    void delete_ShouldCallRepositoryDeleteById() {
        // Arrange
        Long courseIdToDelete = 1L;
        // For void methods in mocks, often no 'when' is needed unless you expect an exception
        // doNothing().when(courseRepository).deleteById(courseIdToDelete); // Usually not required

        // Act
        courseService.delete(courseIdToDelete);

        // Verify
        // Check that repository.deleteById was called exactly once with the correct ID
        verify(courseRepository, times(1)).deleteById(courseIdToDelete);
    }

    @Test
    @DisplayName("Test existsByCode - Should return true when code exists")
    void existsByCode_WhenCodeExists_ShouldReturnTrue() {
        // Arrange
        String existingCode = "SWT301";
        when(courseRepository.existsByCourseCode(existingCode)).thenReturn(
            true
        );

        // Act
        boolean exists = courseService.existsByCode(existingCode);

        // Assert
        assertTrue(exists, "Should return true for existing code");

        // Verify
        verify(courseRepository, times(1)).existsByCourseCode(existingCode);
    }

    @Test
    @DisplayName(
        "Test existsByCode - Should return false when code does not exist"
    )
    void existsByCode_WhenCodeDoesNotExist_ShouldReturnFalse() {
        // Arrange
        String nonExistingCode = "XYZ789";
        when(courseRepository.existsByCourseCode(nonExistingCode)).thenReturn(
            false
        );

        // Act
        boolean exists = courseService.existsByCode(nonExistingCode);

        // Assert
        assertFalse(exists, "Should return false for non-existing code");

        // Verify
        verify(courseRepository, times(1)).existsByCourseCode(nonExistingCode);
    }

    @Test
    @DisplayName("Test existsById - Should return true when ID exists")
    void existsById_WhenIdExists_ShouldReturnTrue() {
        // Arrange
        Long existingId = 1L;
        when(courseRepository.existsById(existingId)).thenReturn(true);

        // Act
        boolean exists = courseService.existsById(existingId);

        // Assert
        assertTrue(exists, "Should return true for existing ID");

        // Verify
        verify(courseRepository, times(1)).existsById(existingId);
    }

    @Test
    @DisplayName("Test existsById - Should return false when ID does not exist")
    void existsById_WhenIdDoesNotExist_ShouldReturnFalse() {
        // Arrange
        Long nonExistingId = 999L;
        when(courseRepository.existsById(nonExistingId)).thenReturn(false);

        // Act
        boolean exists = courseService.existsById(nonExistingId);

        // Assert
        assertFalse(exists, "Should return false for non-existing ID");

        // Verify
        verify(courseRepository, times(1)).existsById(nonExistingId);
    }

    @Test
    @DisplayName(
        "Test findAllByIntakeId - Should return list of courses for intake"
    )
    void findAllByIntakeId_ShouldReturnListOfCourses() {
        // Arrange
        Long intakeId = 1L;
        List<Course> expectedCourses = Arrays.asList(course1); // course1 is linked to intake1 in setup
        when(courseRepository.findAllByIntakeId(intakeId)).thenReturn(
            expectedCourses
        );

        // Act
        List<Course> actualCourses = courseService.findAllByIntakeId(intakeId);

        // Assert
        assertNotNull(actualCourses);
        assertEquals(1, actualCourses.size());
        assertEquals(expectedCourses, actualCourses);

        // Verify
        verify(courseRepository, times(1)).findAllByIntakeId(intakeId);
    }

    @Test
    @DisplayName(
        "Test findAllByIntakeId - Should return empty list for intake with no courses"
    )
    void findAllByIntakeId_ShouldReturnEmptyListForIntakeWithNoCourses() {
        // Arrange
        Long intakeIdWithoutCourses = 2L;
        List<Course> expectedCourses = new ArrayList<>();
        when(
            courseRepository.findAllByIntakeId(intakeIdWithoutCourses)
        ).thenReturn(expectedCourses);

        // Act
        List<Course> actualCourses = courseService.findAllByIntakeId(
            intakeIdWithoutCourses
        );

        // Assert
        assertNotNull(actualCourses);
        assertTrue(actualCourses.isEmpty());

        // Verify
        verify(courseRepository, times(1)).findAllByIntakeId(
            intakeIdWithoutCourses
        );
    }

    @Test
    @DisplayName("Test findCourseByPartId - Should return course")
    void findCourseByPartId_ShouldReturnCourse() {
        // Arrange
        Long partIdBelongingToCourse1 = 5L; // Assume a part ID that belongs to course1
        when(
            courseRepository.findCourseByPartId(partIdBelongingToCourse1)
        ).thenReturn(course1);

        // Act
        Course actualCourse = courseService.findCourseByPartId(
            partIdBelongingToCourse1
        );

        // Assert
        assertNotNull(actualCourse);
        assertEquals(course1.getId(), actualCourse.getId());
        assertEquals(course1.getName(), actualCourse.getName());

        // Verify
        verify(courseRepository, times(1)).findCourseByPartId(
            partIdBelongingToCourse1
        );
    }

    @Test
    @DisplayName(
        "Test findCourseByPartId - Should return null when no course found"
    )
    void findCourseByPartId_WhenNoCourseFound_ShouldReturnNull() {
        // Arrange
        Long partIdWithNoCourse = 99L; // Assume a part ID not linked to any course
        when(
            courseRepository.findCourseByPartId(partIdWithNoCourse)
        ).thenReturn(null); // Simulate repo returning null

        // Act
        Course actualCourse = courseService.findCourseByPartId(
            partIdWithNoCourse
        );

        // Assert
        assertNull(
            actualCourse,
            "Should return null when repository finds no course for the part ID"
        );

        // Verify
        verify(courseRepository, times(1)).findCourseByPartId(
            partIdWithNoCourse
        );
    }
}
