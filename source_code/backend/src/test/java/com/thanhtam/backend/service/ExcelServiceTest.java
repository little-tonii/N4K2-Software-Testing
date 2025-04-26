package com.thanhtam.backend.service;

import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExcelServiceTest {

    @Mock
    private FilesStorageService filesStorageService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private IntakeService intakeService;

    @InjectMocks
    private ExcelServiceImpl excelService;

    @TempDir
    Path tempDir;

    private User testUser;
    private Role testRole;
    private Intake testIntake;
    private Profile testProfile;
    private File testExcelFile;

    @BeforeEach
    void setUp() throws IOException {
        // Initialize test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName(ERole.ROLE_STUDENT);

        testIntake = new Intake();
        testIntake.setId(1L);
        testIntake.setIntakeCode("INT001");
        testIntake.setName("Intake 2024");

        testProfile = new Profile();
        testProfile.setId(1L);
        testProfile.setFirstName("Test");
        testProfile.setLastName("User");

        testUser.setProfile(testProfile);
        testUser.setIntake(testIntake);
        Set<Role> roles = new HashSet<>();
        roles.add(testRole);
        testUser.setRoles(roles);
    }

    private File createTestExcelFile(String fileName, String username, String email, String firstName, String lastName, String intakeCode, String role) throws IOException {
        File file = tempDir.resolve(fileName).toFile();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Username");
            headerRow.createCell(1).setCellValue("Email");
            headerRow.createCell(2).setCellValue("FirstName");
            headerRow.createCell(3).setCellValue("LastName");
            headerRow.createCell(4).setCellValue("IntakeCode");
            headerRow.createCell(5).setCellValue("Role");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(username);
            dataRow.createCell(1).setCellValue(email);
            dataRow.createCell(2).setCellValue(firstName);
            dataRow.createCell(3).setCellValue(lastName);
            dataRow.createCell(4).setCellValue(intakeCode);
            dataRow.createCell(5).setCellValue(role);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
        return file;
    }

    /**
     * Test Case: Insert a new user into the database
     * Expected Behavior: 
     * - Should check if user exists by email/username
     * - Should save the user if they don't exist
     * - Should call userRepository.save() exactly once
     */
    @Test
    @DisplayName("Test InsertUserToDB with new user")
    void testInsertUserToDBWithNewUser() {
        // Arrange
        List<User> userList = Collections.singletonList(testUser);
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        excelService.InsertUserToDB(userList);

        // Assert
        verify(userRepository).existsByEmailOrUsername(testUser.getEmail(), testUser.getUsername());
        verify(userRepository).save(testUser);
    }

    /**
     * Test Case: Attempt to insert an existing user
     * Expected Behavior:
     * - Should check if user exists
     * - Should not save if user exists
     * - Should never call userRepository.save()
     */
    @Test
    @DisplayName("Test InsertUserToDB with existing user")
    void testInsertUserToDBWithExistingUser() {
        // Arrange
        List<User> userList = Collections.singletonList(testUser);
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(true);

        // Act
        excelService.InsertUserToDB(userList);

        // Assert
        verify(userRepository).existsByEmailOrUsername(testUser.getEmail(), testUser.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test Case: Write user data to Excel file
     * Expected Behavior:
     * - Should not throw any exceptions
     * - Should successfully write data to Excel file
     */
    @Test
    @DisplayName("Test writeUserToExcelFile")
    void testWriteUserToExcelFile() throws IOException {
        // Arrange
        ArrayList<UserExport> userExports = new ArrayList<>();
        UserExport userExport = new UserExport(
            "testuser",
            "Test",
            "User",
            "test@example.com"
        );
        userExports.add(userExport);

        // Act & Assert
        assertDoesNotThrow(() -> excelService.writeUserToExcelFile(userExports));
    }

    /**
     * Test Case: Read from an invalid Excel file path
     * Expected Behavior:
     * - Should throw IOException when file path is invalid
     */
    @Test
    @DisplayName("Test readUserFromExcelFile with invalid file path")
    void testReadUserFromExcelFileInvalidPath() {
        // Arrange
        String invalidPath = "invalid/path/file.xlsx";

        // Act & Assert
        assertThrows(IOException.class, () -> excelService.readUserFromExcelFile(invalidPath));
    }

    @Test
    @DisplayName("Test readUserFromExcelFile with valid XLSX file")
    void testReadUserFromExcelFile_ValidXLSX() throws IOException {
        // Arrange
        File file = createTestExcelFile("test.xlsx", "testuser", "test@example.com", "Test", "User", "INT001", "STUDENT");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(intakeService.findByCode(anyString())).thenReturn(testIntake);
        when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(testRole));
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty());
        when(roleService.findByName(ERole.ROLE_LECTURER)).thenReturn(Optional.empty());

        // Act
        List<User> users = excelService.readUserFromExcelFile(file.getAbsolutePath());

        // Assert
        assertNotNull(users);
        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test", user.getProfile().getFirstName());
        assertEquals("User", user.getProfile().getLastName());
        assertEquals("INT001", user.getIntake().getIntakeCode());
        assertTrue(user.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_STUDENT));
        
        verify(passwordEncoder).encode("testuser");
        verify(intakeService).findByCode("INT001");
        verify(roleService).findByName(ERole.ROLE_STUDENT);
    }

    @Test
    @DisplayName("Test readUserFromExcelFile with ADMIN role")
    void testReadUserFromExcelFile_AdminRole() throws IOException {
        // Arrange
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(ERole.ROLE_ADMIN);

        File file = createTestExcelFile("admin.xlsx", "adminuser", "admin@example.com", "Admin", "User", "INT001", "ADMIN");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(intakeService.findByCode(anyString())).thenReturn(testIntake);
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(testRole));
        when(roleService.findByName(ERole.ROLE_LECTURER)).thenReturn(Optional.empty());

        // Act
        List<User> users = excelService.readUserFromExcelFile(file.getAbsolutePath());

        // Assert
        assertNotNull(users);
        assertEquals(1, users.size());
        User user = users.get(0);
        assertTrue(user.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_ADMIN));
    }

    @Test
    @DisplayName("Test readUserFromExcelFile with LECTURER role")
    void testReadUserFromExcelFile_LecturerRole() throws IOException {
        // Arrange
        Role lecturerRole = new Role();
        lecturerRole.setId(3L);
        lecturerRole.setName(ERole.ROLE_LECTURER);

        File file = createTestExcelFile("lecturer.xlsx", "lectureruser", "lecturer@example.com", "Lecturer", "User", "INT001", "LECTURER");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(intakeService.findByCode(anyString())).thenReturn(testIntake);
        when(roleService.findByName(ERole.ROLE_LECTURER)).thenReturn(Optional.of(lecturerRole));
        when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(testRole));
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty());

        // Act
        List<User> users = excelService.readUserFromExcelFile(file.getAbsolutePath());

        // Assert
        assertNotNull(users);
        assertEquals(1, users.size());
        User user = users.get(0);
        assertTrue(user.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_LECTURER));
    }

    @Test
    @DisplayName("Test readUserFromExcelFile with different cell types")
    void testReadUserFromExcelFile_DifferentCellTypes() throws IOException {
        // Create Excel file with different cell types
        File file = createTestExcelFile("mixed.xlsx", "testuser", "test@example.com", "Test", "User", "INT001", "STUDENT");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(intakeService.findByCode(anyString())).thenReturn(testIntake);
        when(roleService.findByName(any(ERole.class))).thenReturn(Optional.of(testRole));

        // Act
        List<User> users = excelService.readUserFromExcelFile(file.getAbsolutePath());

        // Assert
        assertNotNull(users);
        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test", user.getProfile().getFirstName());
    }

    @Test
    @DisplayName("Test readUserFromExcelFile when intake is not found")
    void testReadUserFromExcelFile_NullIntake() throws IOException {
        // Create test file
        File file = createTestExcelFile("null_intake.xlsx", "testuser", "test@example.com", "Test", "User", "NONEXISTENT", "STUDENT");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(intakeService.findByCode("NONEXISTENT")).thenReturn(null);
        when(roleService.findByName(any(ERole.class))).thenReturn(Optional.of(testRole));

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            excelService.readUserFromExcelFile(file.getAbsolutePath());
        });
    }

    @Test
    @DisplayName("Test readUserFromExcelFile when role is not found")
    void testReadUserFromExcelFile_NullRole() throws IOException {
        // Create test file
        File file = createTestExcelFile("null_role.xlsx", "testuser", "test@example.com", "Test", "User", "INT001", "INVALID_ROLE");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(intakeService.findByCode(anyString())).thenReturn(testIntake);
        when(roleService.findByName(any(ERole.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            excelService.readUserFromExcelFile(file.getAbsolutePath());
        });
    }

    @Test
    @DisplayName("Test InsertUserToDB with null user list")
    void testInsertUserToDB_NullUserList() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            excelService.InsertUserToDB(null);
        });
    }

    @Test
    @DisplayName("Test InsertUserToDB with null user properties")
    void testInsertUserToDB_NullUserProperties() {
        // Setup
        User nullUser = new User();  // User with null email and username
        List<User> userList = Collections.singletonList(nullUser);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            excelService.InsertUserToDB(userList);
        });
        
        verify(userRepository, never()).existsByEmailOrUsername(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Test readUserFromExcelFile with null file path")
    void testReadUserFromExcelFile_NullFilePath() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            excelService.readUserFromExcelFile(null);
        });
    }

    @Test
    @DisplayName("Test writeUserToExcelFile with null user exports")
    void testWriteUserToExcelFile_NullUserExports() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            excelService.writeUserToExcelFile(null);
        });
    }
} 