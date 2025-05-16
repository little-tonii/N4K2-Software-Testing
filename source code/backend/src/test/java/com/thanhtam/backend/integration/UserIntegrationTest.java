package com.thanhtam.backend.integration;

import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.service.UserService;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void testCreateUserWithDatabase() {
        System.out.println("Starting test: testCreateUserWithDatabase");
        
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        
        transactionTemplate.execute(status -> {
            System.out.println("Transaction started");
            
            // Debug: print all bean names
            System.out.println("--- ALL BEANS IN CONTEXT ---");
            for (String beanName : applicationContext.getBeanDefinitionNames()) {
                System.out.println(beanName);
            }
            System.out.println("--- END BEANS ---");

            // Ensure ROLE_STUDENT exists in DB
            if (roleRepository == null) {
                System.out.println("RoleRepository is null");
                return null;
            }
            Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                    .orElseGet(() -> {
                        System.out.println("Creating new ROLE_STUDENT");
                        Role newRole = new Role(null, ERole.ROLE_STUDENT);
                        return roleRepository.save(newRole);
                    });
            System.out.println("Student Role: " + studentRole);

            // Create a profile
            Profile profile = new Profile();
            profile.setFirstName("Integration");
            profile.setLastName("Test");
            System.out.println("Created profile: " + profile);

            // Create roles
            Set<Role> roles = new HashSet<>();
            roles.add(studentRole);
            System.out.println("Created roles set: " + roles);

            // Create a new user
            User user = new User();
            user.setUsername("integrationTestUser");
            user.setEmail("integration.test@example.com");
            user.setPassword("password123");
            user.setProfile(profile);
            user.setRoles(roles);
            System.out.println("Created user object: " + user);

            // Save the user
            User savedUser = userService.createUser(user);
            System.out.println("Saved user: " + savedUser);

            // Verify the user was saved correctly
            assertNotNull(savedUser);
            assertNotNull(savedUser.getId());
            assertEquals("integrationTestUser", savedUser.getUsername());
            assertEquals("integration.test@example.com", savedUser.getEmail());
            assertNotNull(savedUser.getProfile());
            assertEquals("Integration", savedUser.getProfile().getFirstName());
            assertEquals("Test", savedUser.getProfile().getLastName());
            assertNotNull(savedUser.getRoles());
            assertFalse(savedUser.getRoles().isEmpty());
            assertEquals(ERole.ROLE_STUDENT, savedUser.getRoles().iterator().next().getName());

            // Verify we can retrieve the user from the database
            User retrievedUser = userRepository.findByUsername("integrationTestUser")
                    .orElse(null);
            System.out.println("Retrieved user from DB: " + retrievedUser);
            
            assertNotNull(retrievedUser);
            assertEquals(savedUser.getId(), retrievedUser.getId());
            assertEquals(savedUser.getUsername(), retrievedUser.getUsername());
            assertEquals(savedUser.getEmail(), retrievedUser.getEmail());
            assertNotNull(retrievedUser.getProfile());
            assertEquals(savedUser.getProfile().getFirstName(), retrievedUser.getProfile().getFirstName());
            assertEquals(savedUser.getProfile().getLastName(), retrievedUser.getProfile().getLastName());
            assertNotNull(retrievedUser.getRoles());
            assertFalse(retrievedUser.getRoles().isEmpty());
            assertEquals(ERole.ROLE_STUDENT, retrievedUser.getRoles().iterator().next().getName());

            System.out.println("Transaction completed successfully");
            return null;
        });
        
        // Verify user still exists after transaction
        User finalCheckUser = userRepository.findByUsername("integrationTestUser")
                .orElse(null);
        System.out.println("Final check - User in DB: " + finalCheckUser);
    }
} 