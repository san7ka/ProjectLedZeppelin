package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.User;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для UserDAO
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional
public class UserDAOTest {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(User.UserRole.USER);
    }

    @Test
    void testSaveUser() {
        // Given
        User user = testUser;

        // When
        User savedUser = userDAO.save(user);

        // Then
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals(User.UserRole.USER, savedUser.getRole());
    }

    @Test
    void testFindById() {
        // Given
        User savedUser = userDAO.save(testUser);

        // When
        Optional<User> foundUser = userDAO.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void testFindByUsername() {
        // Given
        userDAO.save(testUser);

        // When
        Optional<User> foundUser = userDAO.findByUsername("testuser");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void testFindByEmail() {
        // Given
        userDAO.save(testUser);

        // When
        Optional<User> foundUser = userDAO.findByEmail("test@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void testFindByUsernameOrEmail() {
        // Given
        userDAO.save(testUser);

        // When
        Optional<User> foundByUsername = userDAO.findByUsernameOrEmail("testuser");
        Optional<User> foundByEmail = userDAO.findByUsernameOrEmail("test@example.com");

        // Then
        assertTrue(foundByUsername.isPresent());
        assertTrue(foundByEmail.isPresent());
        assertEquals(foundByUsername.get().getId(), foundByEmail.get().getId());
    }

    @Test
    void testFindByUsernameNotFound() {
        // When
        Optional<User> foundUser = userDAO.findByUsername("nonexistent");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByEmailNotFound() {
        // When
        Optional<User> foundUser = userDAO.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testUpdateUser() {
        // Given
        User savedUser = userDAO.save(testUser);
        savedUser.setEmail("updated@example.com");

        // When
        User updatedUser = userDAO.save(savedUser);

        // Then
        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("testuser", updatedUser.getUsername());
    }

    @Test
    void testDeleteUser() {
        // Given
        User savedUser = userDAO.save(testUser);
        Long userId = savedUser.getId();

        // When
        userDAO.delete(savedUser);

        // Then
        Optional<User> deletedUser = userDAO.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testFindAllUsers() {
        // Given
        User user1 = new User("user1", "user1@example.com", "password1");
        User user2 = new User("user2", "user2@example.com", "password2");
        userDAO.save(user1);
        userDAO.save(user2);

        // When
        var allUsers = userDAO.findAll();

        // Then
        assertTrue(allUsers.size() >= 2);
    }

    @Test
    void testExistsByUsername() {
        // Given
        userDAO.save(testUser);

        // When
        boolean exists = userDAO.existsByUsername("testuser");
        boolean notExists = userDAO.existsByUsername("nonexistent");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void testExistsByEmail() {
        // Given
        userDAO.save(testUser);

        // When
        boolean exists = userDAO.existsByEmail("test@example.com");
        boolean notExists = userDAO.existsByEmail("nonexistent@example.com");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }
}
