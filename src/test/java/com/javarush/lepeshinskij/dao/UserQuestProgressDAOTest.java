package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для UserQuestProgressDAO
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional
public class UserQuestProgressDAOTest {

    @Autowired
    private UserQuestProgressDAO userQuestProgressDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private QuestDAO questDAO;

    @Autowired
    private QuestStageDAO questStageDAO;

    private User testUser;
    private Quest testQuest;
    private QuestStage testStage;
    private UserQuestProgress testProgress;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("user@example.com");
        testUser.setPassword("password123");
        testUser.setRole(User.UserRole.USER);
        testUser = userDAO.save(testUser);

        // Создаем тестовый квест
        testQuest = new Quest();
        testQuest.setTitle("Test Quest");
        testQuest.setDescription("A test quest for unit testing");
        testQuest.setAuthor(testUser);
        testQuest = questDAO.save(testQuest);

        // Создаем тестовый этап
        testStage = new QuestStage();
        testStage.setStageId("test_stage");
        testStage.setTitle("Test Stage");
        testStage.setDescription("A test stage for unit testing");
        testStage.setQuest(testQuest);
        testStage.setStageIndex(1);
        testStage.setResultType(QuestStage.ResultType.NONE);
        testStage = questStageDAO.save(testStage);

        // Создаем тестовый прогресс
        testProgress = new UserQuestProgress();
        testProgress.setUser(testUser);
        testProgress.setQuest(testQuest);
        testProgress.setCurrentStageId("test_stage");
        testProgress.setIsCompleted(false);
        testProgress.setStartedAt(LocalDateTime.now());
    }

    @Test
    void testSaveUserQuestProgress() {
        // Given
        UserQuestProgress progress = testProgress;

        // When
        UserQuestProgress savedProgress = userQuestProgressDAO.save(progress);

        // Then
        assertNotNull(savedProgress);
        assertNotNull(savedProgress.getId());
        assertEquals(testUser.getId(), savedProgress.getUser().getId());
        assertEquals(testQuest.getId(), savedProgress.getQuest().getId());
        assertEquals("test_stage", savedProgress.getCurrentStageId());
        assertFalse(savedProgress.getIsCompleted());
        assertNotNull(savedProgress.getStartedAt());
    }

    @Test
    void testFindById() {
        // Given
        UserQuestProgress savedProgress = userQuestProgressDAO.save(testProgress);

        // When
        Optional<UserQuestProgress> foundProgress = userQuestProgressDAO.findById(savedProgress.getId());

        // Then
        assertTrue(foundProgress.isPresent());
        assertEquals(savedProgress.getId(), foundProgress.get().getId());
        assertEquals(testUser.getId(), foundProgress.get().getUser().getId());
        assertEquals(testQuest.getId(), foundProgress.get().getQuest().getId());
    }

    @Test
    void testFindByUser() {
        // Given
        userQuestProgressDAO.save(testProgress);

        // When
        List<UserQuestProgress> userProgress = userQuestProgressDAO.findByUser(testUser);

        // Then
        assertFalse(userProgress.isEmpty());
        assertEquals(testUser.getId(), userProgress.get(0).getUser().getId());
        assertEquals(testQuest.getId(), userProgress.get(0).getQuest().getId());
    }

    @Test
    void testFindByQuest() {
        // Given
        userQuestProgressDAO.save(testProgress);

        // When
        List<UserQuestProgress> questProgress = userQuestProgressDAO.findByQuest(testQuest);

        // Then
        assertFalse(questProgress.isEmpty());
        assertEquals(testQuest.getId(), questProgress.get(0).getQuest().getId());
        assertEquals(testUser.getId(), questProgress.get(0).getUser().getId());
    }

    @Test
    void testFindByUserAndQuest() {
        // Given
        userQuestProgressDAO.save(testProgress);

        // When
        Optional<UserQuestProgress> foundProgress = userQuestProgressDAO.findByUserAndQuest(testUser, testQuest);

        // Then
        assertTrue(foundProgress.isPresent());
        assertEquals(testUser.getId(), foundProgress.get().getUser().getId());
        assertEquals(testQuest.getId(), foundProgress.get().getQuest().getId());
    }

    @Test
    void testFindByUserId() {
        // Given
        userQuestProgressDAO.save(testProgress);

        // When
        List<UserQuestProgress> userProgress = userQuestProgressDAO.findByUserId(testUser.getId());

        // Then
        assertFalse(userProgress.isEmpty());
        assertEquals(testUser.getId(), userProgress.get(0).getUser().getId());
    }

    @Test
    void testFindByQuestId() {
        // Given
        userQuestProgressDAO.save(testProgress);

        // When
        List<UserQuestProgress> questProgress = userQuestProgressDAO.findByQuestId(testQuest.getId());

        // Then
        assertFalse(questProgress.isEmpty());
        assertEquals(testQuest.getId(), questProgress.get(0).getQuest().getId());
    }

    @Test
    void testFindByIsCompleted() {
        // Given
        UserQuestProgress completedProgress = new UserQuestProgress();
        completedProgress.setUser(testUser);
        completedProgress.setQuest(testQuest);
        completedProgress.setCurrentStageId("test_stage");
        completedProgress.setIsCompleted(true);
        completedProgress.setStartedAt(LocalDateTime.now());
        completedProgress.setCompletedAt(LocalDateTime.now());

        userQuestProgressDAO.save(testProgress);
        userQuestProgressDAO.save(completedProgress);

        // When
        List<UserQuestProgress> completedProgressList = userQuestProgressDAO.findCompletedByUser(testUser);
        List<UserQuestProgress> inProgressList = userQuestProgressDAO.findActiveByUser(testUser);

        // Then
        assertFalse(completedProgressList.isEmpty());
        assertFalse(inProgressList.isEmpty());
        assertTrue(completedProgressList.stream().allMatch(p -> p.getIsCompleted()));
        assertTrue(inProgressList.stream().allMatch(p -> !p.getIsCompleted()));
    }

    @Test
    void testIsUserPlayingQuest() {
        // Given
        userQuestProgressDAO.save(testProgress);

        // When
        boolean isPlaying = userQuestProgressDAO.isUserPlayingQuest(testUser, testQuest);
        boolean isNotPlaying = userQuestProgressDAO.isUserPlayingQuest(testUser, new Quest());

        // Then
        assertTrue(isPlaying);
        assertFalse(isNotPlaying);
    }

    @Test
    void testCountByUser() {
        // Given
        Quest quest2 = new Quest("Quest 2", "Description 2", testUser);
        quest2 = questDAO.save(quest2);

        UserQuestProgress progress2 = new UserQuestProgress();
        progress2.setUser(testUser);
        progress2.setQuest(quest2);
        progress2.setCurrentStageId("stage1");
        progress2.setIsCompleted(false);
        progress2.setStartedAt(LocalDateTime.now());

        userQuestProgressDAO.save(testProgress);
        userQuestProgressDAO.save(progress2);

        // When
        List<UserQuestProgress> userProgress = userQuestProgressDAO.findByUser(testUser);

        // Then
        assertEquals(2, userProgress.size());
    }

    @Test
    void testCountByQuest() {
        // Given
        User user2 = new User("user2", "user2@example.com", "password");
        user2 = userDAO.save(user2);

        UserQuestProgress progress2 = new UserQuestProgress();
        progress2.setUser(user2);
        progress2.setQuest(testQuest);
        progress2.setCurrentStageId("test_stage");
        progress2.setIsCompleted(false);
        progress2.setStartedAt(LocalDateTime.now());

        userQuestProgressDAO.save(testProgress);
        userQuestProgressDAO.save(progress2);

        // When
        List<UserQuestProgress> questProgress = userQuestProgressDAO.findByQuest(testQuest);

        // Then
        assertEquals(2, questProgress.size());
    }

    @Test
    void testUpdateUserQuestProgress() {
        // Given
        UserQuestProgress savedProgress = userQuestProgressDAO.save(testProgress);
        savedProgress.setIsCompleted(true);
        savedProgress.setCompletedAt(LocalDateTime.now());
        savedProgress.setCurrentStageId("final_stage");

        // When
        UserQuestProgress updatedProgress = userQuestProgressDAO.save(savedProgress);

        // Then
        assertEquals(savedProgress.getId(), updatedProgress.getId());
        assertTrue(updatedProgress.getIsCompleted());
        assertNotNull(updatedProgress.getCompletedAt());
        assertEquals("final_stage", updatedProgress.getCurrentStageId());
    }

    @Test
    void testDeleteUserQuestProgress() {
        // Given
        UserQuestProgress savedProgress = userQuestProgressDAO.save(testProgress);
        Long progressId = savedProgress.getId();

        // When
        userQuestProgressDAO.delete(savedProgress);

        // Then
        Optional<UserQuestProgress> deletedProgress = userQuestProgressDAO.findById(progressId);
        assertFalse(deletedProgress.isPresent());
    }

    @Test
    void testFindAllUserQuestProgress() {
        // Given
        Quest quest2 = new Quest("Quest 2", "Description 2", testUser);
        quest2 = questDAO.save(quest2);

        UserQuestProgress progress2 = new UserQuestProgress();
        progress2.setUser(testUser);
        progress2.setQuest(quest2);
        progress2.setCurrentStageId("stage1");
        progress2.setIsCompleted(false);
        progress2.setStartedAt(LocalDateTime.now());

        userQuestProgressDAO.save(testProgress);
        userQuestProgressDAO.save(progress2);

        // When
        List<UserQuestProgress> allProgress = userQuestProgressDAO.findAll();

        // Then
        assertTrue(allProgress.size() >= 2);
    }

    @Test
    void testCountUserQuestProgress() {
        // Given
        Quest quest2 = new Quest("Quest 2", "Description 2", testUser);
        quest2 = questDAO.save(quest2);

        UserQuestProgress progress2 = new UserQuestProgress();
        progress2.setUser(testUser);
        progress2.setQuest(quest2);
        progress2.setCurrentStageId("stage1");
        progress2.setIsCompleted(false);
        progress2.setStartedAt(LocalDateTime.now());

        userQuestProgressDAO.save(testProgress);
        userQuestProgressDAO.save(progress2);

        // When
        long count = userQuestProgressDAO.count();

        // Then
        assertTrue(count >= 2);
    }

    @Test
    void testFindByUserAndQuestNotFound() {
        // Given
        User otherUser = new User("otheruser", "other@example.com", "password");
        otherUser = userDAO.save(otherUser);

        Quest otherQuest = new Quest("Other Quest", "Other Description", otherUser);
        otherQuest = questDAO.save(otherQuest);

        // When
        Optional<UserQuestProgress> foundProgress = userQuestProgressDAO.findByUserAndQuest(otherUser, testQuest);

        // Then
        assertFalse(foundProgress.isPresent());
    }
}
