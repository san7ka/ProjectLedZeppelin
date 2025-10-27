package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.dao.UserDAO;
import com.javarush.lepeshinskij.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Тесты для UserService
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.UserRole.USER);
    }

    @Test
    void testCreateUser() {
        // Given
        when(userDAO.existsByUsername("testuser")).thenReturn(false);
        when(userDAO.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userDAO.save(any(User.class))).thenReturn(testUser);

        // When
        User savedUser = userService.createUser("testuser", "test@example.com", "password");

        // Then
        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        verify(userDAO).save(any(User.class));
    }

    @Test
    void testCreateUserWithExistingUsername() {
        // Given
        when(userDAO.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser("testuser", "test@example.com", "password"));
    }

    @Test
    void testCreateUserWithExistingEmail() {
        // Given
        when(userDAO.existsByUsername("testuser")).thenReturn(false);
        when(userDAO.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser("testuser", "test@example.com", "password"));
    }

    @Test
    void testCreateAdmin() {
        // Given
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userDAO.save(any(User.class))).thenReturn(testUser);

        // When
        User savedUser = userService.createAdmin("admin", "admin@example.com", "password");

        // Then
        assertNotNull(savedUser);
        verify(userDAO).save(any(User.class));
    }

    @Test
    void testFindByUsername() {
        // Given
        when(userDAO.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> foundUser = userService.findByUsername("testuser");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        verify(userDAO).findByUsername("testuser");
    }

    @Test
    void testFindByUsernameNotFound() {
        // Given
        when(userDAO.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> foundUser = userService.findByUsername("nonexistent");

        // Then
        assertFalse(foundUser.isPresent());
        verify(userDAO).findByUsername("nonexistent");
    }

    @Test
    void testFindByEmail() {
        // Given
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> foundUser = userService.findByEmail("test@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        verify(userDAO).findByEmail("test@example.com");
    }

    @Test
    void testFindByUsernameOrEmail() {
        // Given
        when(userDAO.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> foundUser = userService.findByUsernameOrEmail("testuser");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        verify(userDAO).findByUsernameOrEmail("testuser");
    }

    @Test
    void testAuthenticate() {
        // Given
        when(userDAO.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        // When
        Optional<User> authenticatedUser = userService.authenticate("testuser", "password");

        // Then
        assertTrue(authenticatedUser.isPresent());
        assertEquals("testuser", authenticatedUser.get().getUsername());
        verify(passwordEncoder).matches("password", "encodedPassword");
    }

    @Test
    void testAuthenticateWithWrongPassword() {
        // Given
        when(userDAO.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When
        Optional<User> authenticatedUser = userService.authenticate("testuser", "wrongpassword");

        // Then
        assertFalse(authenticatedUser.isPresent());
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
    }

    @Test
    void testAuthenticateWithNonExistentUser() {
        // Given
        when(userDAO.findByUsernameOrEmail("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> authenticatedUser = userService.authenticate("nonexistent", "password");

        // Then
        assertFalse(authenticatedUser.isPresent());
        verify(userDAO).findByUsernameOrEmail("nonexistent");
    }

    @Test
    void testUpdateUser() {
        // Given
        when(userDAO.update(any(User.class))).thenReturn(testUser);

        // When
        User updatedUser = userService.updateUser(testUser);

        // Then
        assertNotNull(updatedUser);
        verify(userDAO).update(testUser);
    }

    @Test
    void testChangePassword() {
        // Given
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userDAO.update(any(User.class))).thenReturn(testUser);

        // When
        userService.changePassword(testUser, "newPassword");

        // Then
        assertEquals("newEncodedPassword", testUser.getPassword());
        verify(passwordEncoder).encode("newPassword");
        verify(userDAO).update(testUser);
    }

    @Test
    void testCheckPassword() {
        // Given
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        // When
        boolean isValid = userService.checkPassword(testUser, "password");

        // Then
        assertTrue(isValid);
        verify(passwordEncoder).matches("password", "encodedPassword");
    }

    @Test
    void testDeleteUser() {
        // Given
        doNothing().when(userDAO).delete(testUser);

        // When
        userService.deleteUser(testUser);

        // Then
        verify(userDAO).delete(testUser);
    }

    @Test
    void testDeleteUserById() {
        // Given
        when(userDAO.deleteById(1L)).thenReturn(true);

        // When
        boolean deleted = userService.deleteUserById(1L);

        // Then
        assertTrue(deleted);
        verify(userDAO).deleteById(1L);
    }

    @Test
    void testIsAdmin() {
        // Given
        testUser.setRole(User.UserRole.ADMIN);

        // When
        boolean isAdmin = userService.isAdmin(testUser);

        // Then
        assertTrue(isAdmin);
    }

    @Test
    void testIsNotAdmin() {
        // Given
        testUser.setRole(User.UserRole.USER);

        // When
        boolean isAdmin = userService.isAdmin(testUser);

        // Then
        assertFalse(isAdmin);
    }

    @Test
    void testChangeUserRole() {
        // Given
        when(userDAO.update(any(User.class))).thenReturn(testUser);

        // When
        userService.changeUserRole(testUser, User.UserRole.ADMIN);

        // Then
        assertEquals(User.UserRole.ADMIN, testUser.getRole());
        verify(userDAO).update(testUser);
    }

    @Test
    void testExistsByUsername() {
        // Given
        when(userDAO.existsByUsername("testuser")).thenReturn(true);
        when(userDAO.existsByUsername("nonexistent")).thenReturn(false);

        // When
        boolean exists = userService.existsByUsername("testuser");
        boolean notExists = userService.existsByUsername("nonexistent");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
        verify(userDAO).existsByUsername("testuser");
        verify(userDAO).existsByUsername("nonexistent");
    }

    @Test
    void testExistsByEmail() {
        // Given
        when(userDAO.existsByEmail("test@example.com")).thenReturn(true);
        when(userDAO.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When
        boolean exists = userService.existsByEmail("test@example.com");
        boolean notExists = userService.existsByEmail("nonexistent@example.com");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
        verify(userDAO).existsByEmail("test@example.com");
        verify(userDAO).existsByEmail("nonexistent@example.com");
    }
}