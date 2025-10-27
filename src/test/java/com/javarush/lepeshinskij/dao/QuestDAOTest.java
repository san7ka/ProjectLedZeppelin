package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для QuestDAO
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional
public class QuestDAOTest {

    @Autowired
    private QuestDAO questDAO;

    @Autowired
    private UserDAO userDAO;

    private User testAuthor;
    private Quest testQuest;

    @BeforeEach
    void setUp() {
        // Создаем тестового автора
        testAuthor = new User();
        testAuthor.setUsername("testauthor");
        testAuthor.setEmail("author@example.com");
        testAuthor.setPassword("password123");
        testAuthor.setRole(User.UserRole.USER);
        testAuthor = userDAO.save(testAuthor);

        // Создаем тестовый квест
        testQuest = new Quest();
        testQuest.setTitle("Test Quest");
        testQuest.setDescription("A test quest for unit testing");
        testQuest.setAuthor(testAuthor);
    }

    @Test
    void testSaveQuest() {
        // Given
        Quest quest = testQuest;

        // When
        Quest savedQuest = questDAO.save(quest);

        // Then
        assertNotNull(savedQuest);
        assertNotNull(savedQuest.getId());
        assertEquals("Test Quest", savedQuest.getTitle());
        assertEquals("A test quest for unit testing", savedQuest.getDescription());
        assertEquals(testAuthor.getId(), savedQuest.getAuthor().getId());
    }

    @Test
    void testFindById() {
        // Given
        Quest savedQuest = questDAO.save(testQuest);

        // When
        Optional<Quest> foundQuest = questDAO.findById(savedQuest.getId());

        // Then
        assertTrue(foundQuest.isPresent());
        assertEquals(savedQuest.getId(), foundQuest.get().getId());
        assertEquals("Test Quest", foundQuest.get().getTitle());
    }

    @Test
    void testFindByAuthor() {
        // Given
        questDAO.save(testQuest);

        // When
        List<Quest> authorQuests = questDAO.findByAuthor(testAuthor);

        // Then
        assertFalse(authorQuests.isEmpty());
        assertEquals("Test Quest", authorQuests.get(0).getTitle());
        assertEquals(testAuthor.getId(), authorQuests.get(0).getAuthor().getId());
    }

    @Test
    void testFindByAuthorId() {
        // Given
        questDAO.save(testQuest);

        // When
        List<Quest> authorQuests = questDAO.findByAuthorId(testAuthor.getId());

        // Then
        assertFalse(authorQuests.isEmpty());
        assertEquals("Test Quest", authorQuests.get(0).getTitle());
    }

    @Test
    void testFindByAuthorUsername() {
        // Given
        questDAO.save(testQuest);

        // When
        List<Quest> authorQuests = questDAO.findByAuthorUsername("testauthor");

        // Then
        assertFalse(authorQuests.isEmpty());
        assertEquals("Test Quest", authorQuests.get(0).getTitle());
    }

    @Test
    void testFindByTitleContaining() {
        // Given
        questDAO.save(testQuest);

        // When
        List<Quest> quests = questDAO.findByTitleContaining("Test");

        // Then
        assertFalse(quests.isEmpty());
        assertTrue(quests.get(0).getTitle().contains("Test"));
    }

    @Test
    void testFindByDescriptionContaining() {
        // Given
        questDAO.save(testQuest);

        // When
        List<Quest> quests = questDAO.findByDescriptionContaining("test quest");

        // Then
        assertFalse(quests.isEmpty());
        assertTrue(quests.get(0).getDescription().toLowerCase().contains("test quest"));
    }

    @Test
    void testFindAllQuests() {
        // Given
        Quest quest1 = new Quest("Quest 1", "Description 1", testAuthor);
        Quest quest2 = new Quest("Quest 2", "Description 2", testAuthor);
        questDAO.save(quest1);
        questDAO.save(quest2);

        // When
        List<Quest> allQuests = questDAO.findAll();

        // Then
        assertTrue(allQuests.size() >= 2);
    }

    @Test
    void testUpdateQuest() {
        // Given
        Quest savedQuest = questDAO.save(testQuest);
        savedQuest.setTitle("Updated Quest Title");

        // When
        Quest updatedQuest = questDAO.save(savedQuest);

        // Then
        assertEquals(savedQuest.getId(), updatedQuest.getId());
        assertEquals("Updated Quest Title", updatedQuest.getTitle());
        assertEquals("A test quest for unit testing", updatedQuest.getDescription());
    }

    @Test
    void testDeleteQuest() {
        // Given
        Quest savedQuest = questDAO.save(testQuest);
        Long questId = savedQuest.getId();

        // When
        questDAO.delete(savedQuest);

        // Then
        Optional<Quest> deletedQuest = questDAO.findById(questId);
        assertFalse(deletedQuest.isPresent());
    }

    @Test
    void testCountQuests() {
        // Given
        Quest quest1 = new Quest("Quest 1", "Description 1", testAuthor);
        Quest quest2 = new Quest("Quest 2", "Description 2", testAuthor);
        questDAO.save(quest1);
        questDAO.save(quest2);

        // When
        long count = questDAO.count();

        // Then
        assertTrue(count >= 2);
    }

    @Test
    void testFindByAuthorWithNoQuests() {
        // Given
        User newAuthor = new User("newauthor", "new@example.com", "password");
        newAuthor = userDAO.save(newAuthor);

        // When
        List<Quest> authorQuests = questDAO.findByAuthor(newAuthor);

        // Then
        assertTrue(authorQuests.isEmpty());
    }

    @Test
    void testFindByTitleContainingCaseInsensitive() {
        // Given
        questDAO.save(testQuest);

        // When
        List<Quest> quests = questDAO.findByTitleContaining("test");

        // Then
        assertFalse(quests.isEmpty());
        assertTrue(quests.get(0).getTitle().toLowerCase().contains("test"));
    }
}
