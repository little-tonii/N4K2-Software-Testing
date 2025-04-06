package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamUserServiceTest {

    @Mock
    private ExamUserRepository examUserRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamService examService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ExamUserServiceImpl examUserService;

    private Exam testExam;
    private User testUser;
    private ExamUser testExamUser;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testExam = new Exam();
        testExam.setId(1L);
        testExam.setTitle("Test Exam");
        testExam.setDurationExam(60);
        testExam.setCanceled(false);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testExamUser = new ExamUser();
        testExamUser.setId(1L);
        testExamUser.setExam(testExam);
        testExamUser.setUser(testUser);
        testExamUser.setTimeStart(new Date());
        testExamUser.setTimeFinish(new Date());
        testExamUser.setTotalPoint(-1.0);
        testExamUser.setRemainingTime(testExam.getDurationExam() * 60);

        userList = Arrays.asList(testUser);
    }

    @Test
    @DisplayName("Test create exam users")
    void testCreate() {
        // Arrange
        ArgumentCaptor<List<ExamUser>> examUserListCaptor = ArgumentCaptor.forClass(List.class);

        // Act
        examUserService.create(testExam, userList);

        // Assert
        verify(examUserRepository).saveAll(examUserListCaptor.capture());
        List<ExamUser> capturedExamUsers = examUserListCaptor.getValue();
        assertEquals(1, capturedExamUsers.size());
        ExamUser capturedExamUser = capturedExamUsers.get(0);
        assertEquals(testUser, capturedExamUser.getUser());
        assertEquals(testExam, capturedExamUser.getExam());
        assertEquals(testExam.getDurationExam() * 60, capturedExamUser.getRemainingTime());
        assertEquals(-1.0, capturedExamUser.getTotalPoint());
    }

    @Test
    @DisplayName("Test get exam list by username")
    void testGetExamListByUsername() {
        // Arrange
        List<ExamUser> examUserList = Arrays.asList(testExamUser);
        when(examUserRepository.findAllByUser_UsernameAndExam_Canceled(testUser.getUsername(), false))
            .thenReturn(examUserList);

        // Act
        List<ExamUser> result = examUserService.getExamListByUsername(testUser.getUsername());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExamUser.getId(), result.get(0).getId());
        verify(examUserRepository).findAllByUser_UsernameAndExam_Canceled(testUser.getUsername(), false);
    }

    @Test
    @DisplayName("Test find by exam and user")
    void testFindByExamAndUser() {
        // Arrange
        when(examUserRepository.findByExam_IdAndUser_Username(testExam.getId(), testUser.getUsername()))
            .thenReturn(testExamUser);

        // Act
        ExamUser result = examUserService.findByExamAndUser(testExam.getId(), testUser.getUsername());

        // Assert
        assertNotNull(result);
        assertEquals(testExamUser.getId(), result.getId());
        verify(examUserRepository).findByExam_IdAndUser_Username(testExam.getId(), testUser.getUsername());
    }

    @Test
    @DisplayName("Test update exam user")
    void testUpdate() {
        // Arrange
        when(examUserRepository.save(any(ExamUser.class))).thenReturn(testExamUser);

        // Act
        examUserService.update(testExamUser);

        // Assert
        verify(examUserRepository).save(testExamUser);
    }

    @Test
    @DisplayName("Test find exam user by id")
    void testFindExamUserById() {
        // Arrange
        when(examUserRepository.findById(testExamUser.getId())).thenReturn(Optional.of(testExamUser));

        // Act
        Optional<ExamUser> result = examUserService.findExamUserById(testExamUser.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testExamUser.getId(), result.get().getId());
        verify(examUserRepository).findById(testExamUser.getId());
    }

    @Test
    @DisplayName("Test get complete exams")
    void testGetCompleteExams() {
        // Arrange
        Long courseId = 1L;
        when(examUserRepository.findAllByExam_Part_Course_IdAndUser_UsernameAndTotalPointIsGreaterThan(
            courseId, testUser.getUsername(), -1.0)).thenReturn(Collections.singletonList(testExamUser));

        // Act
        List<ExamUser> result = examUserService.getCompleteExams(courseId, testUser.getUsername());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExamUser.getId(), result.get(0).getId());
        verify(examUserRepository).findAllByExam_Part_Course_IdAndUser_UsernameAndTotalPointIsGreaterThan(
            courseId, testUser.getUsername(), -1.0);
    }

    @Test
    @DisplayName("Test find all by exam id")
    void testFindAllByExam_Id() {
        // Arrange
        List<ExamUser> examUsers = Arrays.asList(testExamUser);
        when(examUserRepository.findAllByExam_Id(testExam.getId())).thenReturn(examUsers);

        // Act
        List<ExamUser> result = examUserService.findAllByExam_Id(testExam.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExamUser.getId(), result.get(0).getId());
        verify(examUserRepository).findAllByExam_Id(testExam.getId());
    }

    @Test
    @DisplayName("Test find finished exam users by exam id")
    void testFindExamUsersByIsFinishedIsTrueAndExamId() {
        // Arrange
        List<ExamUser> finishedExams = Arrays.asList(testExamUser);
        when(examUserRepository.findExamUsersByIsFinishedIsTrueAndExam_Id(testExam.getId()))
            .thenReturn(finishedExams);

        // Act
        List<ExamUser> result = examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(testExam.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExamUser.getId(), result.get(0).getId());
        verify(examUserRepository).findExamUsersByIsFinishedIsTrueAndExam_Id(testExam.getId());
    }

    @Test
    @DisplayName("Test create with null exam")
    void testCreate_NullExam() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            examUserService.create(null, Collections.singletonList(testUser));
        });
    }

    @Test
    @DisplayName("Test create with null user list")
    void testCreate_NullUserList() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            examUserService.create(testExam, null);
        });
    }

    @Test
    @DisplayName("Test findByExamAndUser with null examId")
    void testFindByExamAndUser_NullExamId() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            examUserService.findByExamAndUser(null, "testuser");
        });
    }

    @Test
    @DisplayName("Test findByExamAndUser with null username")
    void testFindByExamAndUser_NullUsername() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            examUserService.findByExamAndUser(1L, null);
        });
    }

    @Test
    @DisplayName("Test update with null examUser")
    void testUpdate_NullExamUser() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            examUserService.update(null);
        });
    }
} 