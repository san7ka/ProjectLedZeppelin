package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
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
 * Тесты для QuestStageDAO
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional
public class QuestStageDAOTest {

    @Autowired
    private QuestStageDAO questStageDAO;

    @Autowired
    private QuestDAO questDAO;

    @Autowired
    private UserDAO userDAO;

    private User testAuthor;
    private Quest testQuest;
    private QuestStage testStage;

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
        testQuest = questDAO.save(testQuest);

        // Создаем тестовый этап
        testStage = new QuestStage();
        testStage.setStageId("test_stage");
        testStage.setTitle("Test Stage");
        testStage.setDescription("A test stage for unit testing");
        testStage.setQuest(testQuest);
        testStage.setStageIndex(1);
        testStage.setResultType(QuestStage.ResultType.NONE);
    }

    @Test
    void testSaveQuestStage() {
        // Given
        QuestStage stage = testStage;

        // When
        QuestStage savedStage = questStageDAO.save(stage);

        // Then
        assertNotNull(savedStage);
        assertNotNull(savedStage.getId());
        assertEquals("test_stage", savedStage.getStageId());
        assertEquals("Test Stage", savedStage.getTitle());
        assertEquals("A test stage for unit testing", savedStage.getDescription());
        assertEquals(testQuest.getId(), savedStage.getQuest().getId());
        assertEquals(1, savedStage.getStageIndex());
        assertEquals(QuestStage.ResultType.NONE, savedStage.getResultType());
    }

    @Test
    void testFindById() {
        // Given
        QuestStage savedStage = questStageDAO.save(testStage);

        // When
        Optional<QuestStage> foundStage = questStageDAO.findById(savedStage.getId());

        // Then
        assertTrue(foundStage.isPresent());
        assertEquals(savedStage.getId(), foundStage.get().getId());
        assertEquals("test_stage", foundStage.get().getStageId());
    }

    @Test
    void testFindByQuestIdAndStageId() {
        // Given
        questStageDAO.save(testStage);

        // When
        Optional<QuestStage> foundStage = questStageDAO.findByQuestIdAndStageId(testQuest.getId(), "test_stage");

        // Then
        assertTrue(foundStage.isPresent());
        assertEquals("test_stage", foundStage.get().getStageId());
        assertEquals(testQuest.getId(), foundStage.get().getQuest().getId());
    }

    @Test
    void testFindByQuestAndStageId() {
        // Given
        questStageDAO.save(testStage);

        // When
        Optional<QuestStage> foundStage = questStageDAO.findByQuestAndStageId(testQuest, "test_stage");

        // Then
        assertTrue(foundStage.isPresent());
        assertEquals("test_stage", foundStage.get().getStageId());
        assertEquals(testQuest.getId(), foundStage.get().getQuest().getId());
    }

    @Test
    void testFindByQuest() {
        // Given
        QuestStage stage1 = new QuestStage("stage1", "Stage 1", "Description 1");
        stage1.setQuest(testQuest);
        stage1.setStageIndex(1);
        stage1.setResultType(QuestStage.ResultType.NONE);

        QuestStage stage2 = new QuestStage("stage2", "Stage 2", "Description 2");
        stage2.setQuest(testQuest);
        stage2.setStageIndex(2);
        stage2.setResultType(QuestStage.ResultType.WIN);

        questStageDAO.save(stage1);
        questStageDAO.save(stage2);

        // When
        List<QuestStage> questStages = questStageDAO.findByQuest(testQuest);

        // Then
        assertEquals(2, questStages.size());
        assertTrue(questStages.stream().anyMatch(s -> s.getStageId().equals("stage1")));
        assertTrue(questStages.stream().anyMatch(s -> s.getStageId().equals("stage2")));
    }

    @Test
    void testFindByQuestId() {
        // Given
        QuestStage stage1 = new QuestStage("stage1", "Stage 1", "Description 1");
        stage1.setQuest(testQuest);
        stage1.setStageIndex(1);
        stage1.setResultType(QuestStage.ResultType.NONE);

        QuestStage stage2 = new QuestStage("stage2", "Stage 2", "Description 2");
        stage2.setQuest(testQuest);
        stage2.setStageIndex(2);
        stage2.setResultType(QuestStage.ResultType.WIN);

        questStageDAO.save(stage1);
        questStageDAO.save(stage2);

        // When
        List<QuestStage> questStages = questStageDAO.findByQuestId(testQuest.getId());

        // Then
        assertEquals(2, questStages.size());
        assertTrue(questStages.stream().anyMatch(s -> s.getStageId().equals("stage1")));
        assertTrue(questStages.stream().anyMatch(s -> s.getStageId().equals("stage2")));
    }

    @Test
    void testFindByStageId() {
        // Given
        questStageDAO.save(testStage);

        // When
        Optional<QuestStage> stage = questStageDAO.findByQuestAndStageId(testQuest, "test_stage");

        // Then
        assertTrue(stage.isPresent());
        assertEquals("test_stage", stage.get().getStageId());
    }

    @Test
    void testFindByResultType() {
        // Given
        QuestStage winStage = new QuestStage("win_stage", "Win Stage", "You win!");
        winStage.setQuest(testQuest);
        winStage.setStageIndex(2);
        winStage.setResultType(QuestStage.ResultType.WIN);

        questStageDAO.save(testStage);
        questStageDAO.save(winStage);

        // When
        List<QuestStage> winStages = questStageDAO.findByResultType(QuestStage.ResultType.WIN);

        // Then
        assertFalse(winStages.isEmpty());
        assertTrue(winStages.stream().allMatch(s -> s.getResultType() == QuestStage.ResultType.WIN));
    }

    @Test
    void testFindAllQuestStages() {
        // Given
        QuestStage stage1 = new QuestStage("stage1", "Stage 1", "Description 1");
        stage1.setQuest(testQuest);
        stage1.setStageIndex(1);
        stage1.setResultType(QuestStage.ResultType.NONE);

        QuestStage stage2 = new QuestStage("stage2", "Stage 2", "Description 2");
        stage2.setQuest(testQuest);
        stage2.setStageIndex(2);
        stage2.setResultType(QuestStage.ResultType.WIN);

        questStageDAO.save(stage1);
        questStageDAO.save(stage2);

        // When
        List<QuestStage> allStages = questStageDAO.findAll();

        // Then
        assertTrue(allStages.size() >= 2);
    }

    @Test
    void testUpdateQuestStage() {
        // Given
        QuestStage savedStage = questStageDAO.save(testStage);
        savedStage.setTitle("Updated Stage Title");
        savedStage.setResultType(QuestStage.ResultType.WIN);

        // When
        QuestStage updatedStage = questStageDAO.save(savedStage);

        // Then
        assertEquals(savedStage.getId(), updatedStage.getId());
        assertEquals("Updated Stage Title", updatedStage.getTitle());
        assertEquals(QuestStage.ResultType.WIN, updatedStage.getResultType());
    }

    @Test
    void testDeleteQuestStage() {
        // Given
        QuestStage savedStage = questStageDAO.save(testStage);
        Long stageId = savedStage.getId();

        // When
        questStageDAO.delete(savedStage);

        // Then
        Optional<QuestStage> deletedStage = questStageDAO.findById(stageId);
        assertFalse(deletedStage.isPresent());
    }

    @Test
    void testCountQuestStages() {
        // Given
        QuestStage stage1 = new QuestStage("stage1", "Stage 1", "Description 1");
        stage1.setQuest(testQuest);
        stage1.setStageIndex(1);
        stage1.setResultType(QuestStage.ResultType.NONE);

        QuestStage stage2 = new QuestStage("stage2", "Stage 2", "Description 2");
        stage2.setQuest(testQuest);
        stage2.setStageIndex(2);
        stage2.setResultType(QuestStage.ResultType.WIN);

        questStageDAO.save(stage1);
        questStageDAO.save(stage2);

        // When
        long count = questStageDAO.count();

        // Then
        assertTrue(count >= 2);
    }

    @Test
    void testFindByQuestIdAndStageIdNotFound() {
        // When
        Optional<QuestStage> foundStage = questStageDAO.findByQuestIdAndStageId(999L, "nonexistent");

        // Then
        assertFalse(foundStage.isPresent());
    }

    @Test
    void testFindByQuestWithNoStages() {
        // Given
        Quest emptyQuest = new Quest("Empty Quest", "No stages", testAuthor);
        emptyQuest = questDAO.save(emptyQuest);

        // When
        List<QuestStage> stages = questStageDAO.findByQuest(emptyQuest);

        // Then
        assertTrue(stages.isEmpty());
    }
}
