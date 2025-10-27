package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.dao.QuestDAO;
import com.javarush.lepeshinskij.dao.QuestStageDAO;
import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Тесты для QuestService
 */
@ExtendWith(MockitoExtension.class)
public class QuestServiceTest {

    @Mock
    private QuestDAO questDAO;

    @Mock
    private QuestStageDAO questStageDAO;

    @InjectMocks
    private QuestService questService;

    private User testAuthor;
    private Quest testQuest;
    private QuestStage testStage;

    @BeforeEach
    void setUp() {
        testAuthor = new User();
        testAuthor.setId(1L);
        testAuthor.setUsername("testauthor");
        testAuthor.setEmail("author@example.com");
        testAuthor.setRole(User.UserRole.USER);

        testQuest = new Quest();
        testQuest.setId(1L);
        testQuest.setTitle("Test Quest");
        testQuest.setDescription("Test Description");
        testQuest.setAuthor(testAuthor);

        testStage = new QuestStage();
        testStage.setId(1L);
        testStage.setStageId("start");
        testStage.setTitle("Start Stage");
        testStage.setDescription("Start Description");
        testStage.setQuest(testQuest);
        testStage.setResultType(QuestStage.ResultType.NONE);
    }

    @Test
    void testCreateQuest() {
        // Given
        when(questDAO.save(any(Quest.class))).thenReturn(testQuest);

        // When
        Quest createdQuest = questService.createQuest("Test Quest", "Test Description", testAuthor);

        // Then
        assertNotNull(createdQuest);
        assertEquals("Test Quest", createdQuest.getTitle());
        assertEquals("Test Description", createdQuest.getDescription());
        assertEquals(testAuthor, createdQuest.getAuthor());
        verify(questDAO).save(any(Quest.class));
    }

    @Test
    void testGetQuestById() {
        // Given
        when(questDAO.findById(1L)).thenReturn(Optional.of(testQuest));

        // When
        Optional<Quest> foundQuest = questService.getQuestById(1L);

        // Then
        assertTrue(foundQuest.isPresent());
        assertEquals("Test Quest", foundQuest.get().getTitle());
        verify(questDAO).findById(1L);
    }

    @Test
    void testGetQuestByIdNotFound() {
        // Given
        when(questDAO.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Quest> foundQuest = questService.getQuestById(999L);

        // Then
        assertFalse(foundQuest.isPresent());
        verify(questDAO).findById(999L);
    }

    @Test
    void testGetQuestByIdOrNull() {
        // Given
        when(questDAO.getById(1L)).thenReturn(testQuest);

        // When
        Quest foundQuest = questService.getQuestByIdOrNull(1L);

        // Then
        assertNotNull(foundQuest);
        assertEquals("Test Quest", foundQuest.getTitle());
        verify(questDAO).getById(1L);
    }

    @Test
    void testGetAllQuests() {
        // Given
        List<Quest> quests = Arrays.asList(testQuest);
        when(questDAO.findAll()).thenReturn(quests);

        // When
        List<Quest> allQuests = questService.getAllQuests();

        // Then
        assertNotNull(allQuests);
        assertEquals(1, allQuests.size());
        assertEquals("Test Quest", allQuests.get(0).getTitle());
        verify(questDAO).findAll();
    }

    @Test
    void testGetQuestsByAuthor() {
        // Given
        List<Quest> quests = Arrays.asList(testQuest);
        when(questDAO.findByAuthor(testAuthor)).thenReturn(quests);

        // When
        List<Quest> authorQuests = questService.getQuestsByAuthor(testAuthor);

        // Then
        assertNotNull(authorQuests);
        assertEquals(1, authorQuests.size());
        assertEquals("Test Quest", authorQuests.get(0).getTitle());
        verify(questDAO).findByAuthor(testAuthor);
    }

    @Test
    void testGetQuestsByAuthorId() {
        // Given
        List<Quest> quests = Arrays.asList(testQuest);
        when(questDAO.findByAuthorId(1L)).thenReturn(quests);

        // When
        List<Quest> authorQuests = questService.getQuestsByAuthorId(1L);

        // Then
        assertNotNull(authorQuests);
        assertEquals(1, authorQuests.size());
        assertEquals("Test Quest", authorQuests.get(0).getTitle());
        verify(questDAO).findByAuthorId(1L);
    }

    @Test
    void testSearchQuestsByTitle() {
        // Given
        List<Quest> quests = Arrays.asList(testQuest);
        when(questDAO.findByTitleContaining("Test")).thenReturn(quests);

        // When
        List<Quest> foundQuests = questService.searchQuestsByTitle("Test");

        // Then
        assertNotNull(foundQuests);
        assertEquals(1, foundQuests.size());
        assertEquals("Test Quest", foundQuests.get(0).getTitle());
        verify(questDAO).findByTitleContaining("Test");
    }

    @Test
    void testUpdateQuest() {
        // Given
        when(questDAO.update(any(Quest.class))).thenReturn(testQuest);

        // When
        Quest updatedQuest = questService.updateQuest(testQuest);

        // Then
        assertNotNull(updatedQuest);
        verify(questDAO).update(testQuest);
    }

    @Test
    void testDeleteQuest() {
        // Given
        doNothing().when(questDAO).delete(testQuest);

        // When
        questService.deleteQuest(testQuest);

        // Then
        verify(questDAO).delete(testQuest);
    }

    @Test
    void testDeleteQuestById() {
        // Given
        when(questDAO.deleteById(1L)).thenReturn(true);

        // When
        boolean deleted = questService.deleteQuestById(1L);

        // Then
        assertTrue(deleted);
        verify(questDAO).deleteById(1L);
    }

    @Test
    void testAddStage() {
        // Given
        when(questStageDAO.save(any(QuestStage.class))).thenReturn(testStage);

        // When
        QuestStage addedStage = questService.addStage(testQuest, "start", "Start Stage", "Start Description", QuestStage.ResultType.NONE);

        // Then
        assertNotNull(addedStage);
        assertEquals("start", addedStage.getStageId());
        assertEquals("Start Stage", addedStage.getTitle());
        assertEquals(testQuest, addedStage.getQuest());
        verify(questStageDAO).save(any(QuestStage.class));
    }

    @Test
    void testGetStage() {
        // Given
        when(questStageDAO.findByQuestAndStageId(testQuest, "start")).thenReturn(Optional.of(testStage));

        // When
        Optional<QuestStage> foundStage = questService.getStage(testQuest, "start");

        // Then
        assertTrue(foundStage.isPresent());
        assertEquals("start", foundStage.get().getStageId());
        verify(questStageDAO).findByQuestAndStageId(testQuest, "start");
    }

    @Test
    void testGetQuestStages() {
        // Given
        List<QuestStage> stages = Arrays.asList(testStage);
        when(questStageDAO.findByQuestOrderByStageIndex(testQuest)).thenReturn(stages);

        // When
        List<QuestStage> questStages = questService.getQuestStages(testQuest);

        // Then
        assertNotNull(questStages);
        assertEquals(1, questStages.size());
        assertEquals("start", questStages.get(0).getStageId());
        verify(questStageDAO).findByQuestOrderByStageIndex(testQuest);
    }

    @Test
    void testGetStartStage() {
        // Given
        when(questStageDAO.findStartStageByQuest(testQuest)).thenReturn(Optional.of(testStage));

        // When
        Optional<QuestStage> startStage = questService.getStartStage(testQuest);

        // Then
        assertTrue(startStage.isPresent());
        assertEquals("start", startStage.get().getStageId());
        verify(questStageDAO).findStartStageByQuest(testQuest);
    }

    @Test
    void testUpdateStage() {
        // Given
        when(questStageDAO.update(any(QuestStage.class))).thenReturn(testStage);

        // When
        QuestStage updatedStage = questService.updateStage(testStage);

        // Then
        assertNotNull(updatedStage);
        verify(questStageDAO).update(testStage);
    }

    @Test
    void testDeleteStage() {
        // Given
        doNothing().when(questStageDAO).delete(testStage);

        // When
        questService.deleteStage(testStage);

        // Then
        verify(questStageDAO).delete(testStage);
    }

    @Test
    void testDeleteStageById() {
        // Given
        when(questStageDAO.deleteById(1L)).thenReturn(true);

        // When
        boolean deleted = questService.deleteStageById(1L);

        // Then
        assertTrue(deleted);
        verify(questStageDAO).deleteById(1L);
    }

    @Test
    void testGetQuestCount() {
        // Given
        when(questDAO.count()).thenReturn(5L);

        // When
        long count = questService.getQuestCount();

        // Then
        assertEquals(5L, count);
        verify(questDAO).count();
    }

    @Test
    void testGetQuestCountByAuthor() {
        // Given
        when(questDAO.countByAuthor(testAuthor)).thenReturn(3L);

        // When
        long count = questService.getQuestCountByAuthor(testAuthor);

        // Then
        assertEquals(3L, count);
        verify(questDAO).countByAuthor(testAuthor);
    }

    @Test
    void testGetStageCount() {
        // Given
        when(questStageDAO.countByQuest(testQuest)).thenReturn(5L);

        // When
        long count = questService.getStageCount(testQuest);

        // Then
        assertEquals(5L, count);
        verify(questStageDAO).countByQuest(testQuest);
    }

    @Test
    void testQuestExists() {
        // Given
        when(questDAO.existsById(1L)).thenReturn(true);

        // When
        boolean exists = questService.questExists(1L);

        // Then
        assertTrue(exists);
        verify(questDAO).existsById(1L);
    }

    @Test
    void testStageExists() {
        // Given
        when(questStageDAO.existsByQuestAndStageId(testQuest, "start")).thenReturn(true);

        // When
        boolean exists = questService.stageExists(testQuest, "start");

        // Then
        assertTrue(exists);
        verify(questStageDAO).existsByQuestAndStageId(testQuest, "start");
    }

    @Test
    void testGetStageById() {
        // Given
        when(questStageDAO.findById(1L)).thenReturn(Optional.of(testStage));

        // When
        QuestStage foundStage = questService.getStageById(1L);

        // Then
        assertNotNull(foundStage);
        assertEquals("start", foundStage.getStageId());
        verify(questStageDAO).findById(1L);
    }

    @Test
    void testGetStageByIdNotFound() {
        // Given
        when(questStageDAO.findById(999L)).thenReturn(Optional.empty());

        // When
        QuestStage foundStage = questService.getStageById(999L);

        // Then
        assertNull(foundStage);
        verify(questStageDAO).findById(999L);
    }
}