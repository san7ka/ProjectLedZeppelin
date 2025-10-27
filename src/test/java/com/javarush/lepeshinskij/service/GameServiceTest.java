package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.dao.UserQuestProgressDAO;
import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.QuestStageOption;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Тесты для GameService
 */
@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private QuestService questService;

    @Mock
    private UserQuestProgressDAO userQuestProgressDAO;

    @InjectMocks
    private GameService gameService;

    private User testUser;
    private Quest testQuest;
    private QuestStage testStage;
    private UserQuestProgress testProgress;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.UserRole.USER);

        testQuest = new Quest();
        testQuest.setId(1L);
        testQuest.setTitle("Test Quest");
        testQuest.setDescription("Test Description");
        testQuest.setAuthor(testUser);

        testStage = new QuestStage();
        testStage.setId(1L);
        testStage.setStageId("start");
        testStage.setTitle("Start Stage");
        testStage.setDescription("Start Description");
        testStage.setQuest(testQuest);
        testStage.setResultType(QuestStage.ResultType.NONE);

        testProgress = new UserQuestProgress();
        testProgress.setId(1L);
        testProgress.setUser(testUser);
        testProgress.setQuest(testQuest);
        testProgress.setCurrentStageId("start");
        testProgress.setIsCompleted(false);
        testProgress.setStartedAt(LocalDateTime.now());
    }

    @Test
    void testStartGame() {
        // Given
        when(userQuestProgressDAO.findByUserAndQuest(testUser, testQuest)).thenReturn(Optional.empty());
        when(questService.getStartStage(testQuest)).thenReturn(Optional.of(testStage));
        when(userQuestProgressDAO.save(any(UserQuestProgress.class))).thenReturn(testProgress);

        // When
        UserQuestProgress progress = gameService.startGame(testUser, testQuest);

        // Then
        assertNotNull(progress);
        assertEquals(testUser, progress.getUser());
        assertEquals(testQuest, progress.getQuest());
        assertEquals("start", progress.getCurrentStageId());
        verify(userQuestProgressDAO).save(any(UserQuestProgress.class));
    }

    @Test
    void testStartGameWithExistingProgress() {
        // Given
        when(userQuestProgressDAO.findByUserAndQuest(testUser, testQuest)).thenReturn(Optional.of(testProgress));
        when(questService.getStartStage(testQuest)).thenReturn(Optional.of(testStage));
        when(userQuestProgressDAO.update(any(UserQuestProgress.class))).thenReturn(testProgress);

        // When
        UserQuestProgress progress = gameService.startGame(testUser, testQuest);

        // Then
        assertNotNull(progress);
        verify(userQuestProgressDAO).update(any(UserQuestProgress.class));
    }

    @Test
    void testStartGameWithQuestId() {
        // Given
        when(questService.getQuestByIdOrNull(1L)).thenReturn(testQuest);
        when(userQuestProgressDAO.findByUserAndQuest(testUser, testQuest)).thenReturn(Optional.empty());
        when(questService.getStartStage(testQuest)).thenReturn(Optional.of(testStage));
        when(userQuestProgressDAO.save(any(UserQuestProgress.class))).thenReturn(testProgress);

        // When
        UserQuestProgress progress = gameService.startGame(testUser, 1L);

        // Then
        assertNotNull(progress);
        verify(questService).getQuestByIdOrNull(1L);
    }

    @Test
    void testStartGameWithNonExistentQuest() {
        // Given
        when(questService.getQuestByIdOrNull(999L)).thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> gameService.startGame(testUser, 999L));
    }

    @Test
    void testGetCurrentStage() {
        // Given
        when(questService.getStage(testQuest, "start")).thenReturn(Optional.of(testStage));

        // When
        QuestStage currentStage = gameService.getCurrentStage(testProgress);

        // Then
        assertNotNull(currentStage);
        assertEquals("start", currentStage.getStageId());
        verify(questService).getStage(testQuest, "start");
    }

    @Test
    void testMakeChoice() {
        // Given
        QuestStage nextStage = new QuestStage();
        nextStage.setStageId("next");
        nextStage.setResultType(QuestStage.ResultType.NONE);
        
        when(questService.getStage(testQuest, "start")).thenReturn(Optional.of(testStage));
        when(testStage.hasOption("choice1")).thenReturn(true);
        when(testStage.getOptionByKey("choice1")).thenReturn(new QuestStageOption("choice1", "Choice 1", "next"));
        when(questService.getStage(testQuest, "next")).thenReturn(Optional.of(nextStage));
        when(userQuestProgressDAO.update(any(UserQuestProgress.class))).thenReturn(testProgress);

        // When
        boolean continues = gameService.makeChoice(testProgress, "choice1");

        // Then
        assertTrue(continues);
        verify(userQuestProgressDAO).update(any(UserQuestProgress.class));
    }

    @Test
    void testMakeChoiceWithInvalidChoice() {
        // Given
        when(questService.getStage(testQuest, "start")).thenReturn(Optional.of(testStage));
        when(testStage.hasOption("invalid")).thenReturn(false);

        // When
        boolean continues = gameService.makeChoice(testProgress, "invalid");

        // Then
        assertFalse(continues);
        verify(userQuestProgressDAO, never()).update(any(UserQuestProgress.class));
    }

    @Test
    void testMakeChoiceEndingGame() {
        // Given
        QuestStage endStage = new QuestStage();
        endStage.setStageId("end");
        endStage.setResultType(QuestStage.ResultType.WIN);
        
        when(questService.getStage(testQuest, "start")).thenReturn(Optional.of(testStage));
        when(testStage.hasOption("choice1")).thenReturn(true);
        when(testStage.getOptionByKey("choice1")).thenReturn(new QuestStageOption("choice1", "Choice 1", "end"));
        when(questService.getStage(testQuest, "end")).thenReturn(Optional.of(endStage));
        when(userQuestProgressDAO.update(any(UserQuestProgress.class))).thenReturn(testProgress);

        // When
        boolean continues = gameService.makeChoice(testProgress, "choice1");

        // Then
        assertFalse(continues);
        verify(userQuestProgressDAO).update(any(UserQuestProgress.class));
    }

    @Test
    void testGetProgress() {
        // Given
        when(userQuestProgressDAO.findByUserAndQuest(testUser, testQuest)).thenReturn(Optional.of(testProgress));

        // When
        Optional<UserQuestProgress> progress = gameService.getProgress(testUser, testQuest);

        // Then
        assertTrue(progress.isPresent());
        assertEquals(testProgress, progress.get());
        verify(userQuestProgressDAO).findByUserAndQuest(testUser, testQuest);
    }

    @Test
    void testGetProgressWithQuestId() {
        // Given
        when(userQuestProgressDAO.findByUserIdAndQuestId(1L, 1L)).thenReturn(Optional.of(testProgress));

        // When
        Optional<UserQuestProgress> progress = gameService.getProgress(testUser, 1L);

        // Then
        assertTrue(progress.isPresent());
        assertEquals(testProgress, progress.get());
        verify(userQuestProgressDAO).findByUserIdAndQuestId(1L, 1L);
    }

    @Test
    void testGetProgressById() {
        // Given
        when(userQuestProgressDAO.findById(1L)).thenReturn(Optional.of(testProgress));

        // When
        UserQuestProgress progress = gameService.getProgressById(1L);

        // Then
        assertNotNull(progress);
        assertEquals(testProgress, progress);
        verify(userQuestProgressDAO).findById(1L);
    }

    @Test
    void testGetActiveGames() {
        // Given
        List<UserQuestProgress> activeGames = Arrays.asList(testProgress);
        when(userQuestProgressDAO.findActiveByUser(testUser)).thenReturn(activeGames);

        // When
        List<UserQuestProgress> games = gameService.getActiveGames(testUser);

        // Then
        assertNotNull(games);
        assertEquals(1, games.size());
        assertEquals(testProgress, games.get(0));
        verify(userQuestProgressDAO).findActiveByUser(testUser);
    }

    @Test
    void testGetCompletedGames() {
        // Given
        testProgress.setIsCompleted(true);
        List<UserQuestProgress> completedGames = Arrays.asList(testProgress);
        when(userQuestProgressDAO.findCompletedByUser(testUser)).thenReturn(completedGames);

        // When
        List<UserQuestProgress> games = gameService.getCompletedGames(testUser);

        // Then
        assertNotNull(games);
        assertEquals(1, games.size());
        assertEquals(testProgress, games.get(0));
        verify(userQuestProgressDAO).findCompletedByUser(testUser);
    }

    @Test
    void testGetAllUserGames() {
        // Given
        List<UserQuestProgress> allGames = Arrays.asList(testProgress);
        when(userQuestProgressDAO.findByUser(testUser)).thenReturn(allGames);

        // When
        List<UserQuestProgress> games = gameService.getAllUserGames(testUser);

        // Then
        assertNotNull(games);
        assertEquals(1, games.size());
        assertEquals(testProgress, games.get(0));
        verify(userQuestProgressDAO).findByUser(testUser);
    }

    @Test
    void testIsUserPlayingQuest() {
        // Given
        when(userQuestProgressDAO.isUserPlayingQuest(testUser, testQuest)).thenReturn(true);

        // When
        boolean isPlaying = gameService.isUserPlayingQuest(testUser, testQuest);

        // Then
        assertTrue(isPlaying);
        verify(userQuestProgressDAO).isUserPlayingQuest(testUser, testQuest);
    }

    @Test
    void testHasUserCompletedQuest() {
        // Given
        when(userQuestProgressDAO.hasUserCompletedQuest(testUser, testQuest)).thenReturn(false);

        // When
        boolean hasCompleted = gameService.hasUserCompletedQuest(testUser, testQuest);

        // Then
        assertFalse(hasCompleted);
        verify(userQuestProgressDAO).hasUserCompletedQuest(testUser, testQuest);
    }

    @Test
    void testResetGame() {
        // Given
        when(userQuestProgressDAO.update(any(UserQuestProgress.class))).thenReturn(testProgress);

        // When
        UserQuestProgress resetProgress = gameService.resetGame(testProgress);

        // Then
        assertNotNull(resetProgress);
        verify(userQuestProgressDAO).update(any(UserQuestProgress.class));
    }

    @Test
    void testForceCompleteGame() {
        // Given
        when(userQuestProgressDAO.update(any(UserQuestProgress.class))).thenReturn(testProgress);

        // When
        gameService.forceCompleteGame(testProgress, true);

        // Then
        assertTrue(testProgress.getIsCompleted());
        verify(userQuestProgressDAO).update(any(UserQuestProgress.class));
    }

    @Test
    void testIsValidChoice() {
        // Given
        when(questService.getStage(testQuest, "start")).thenReturn(Optional.of(testStage));
        when(testStage.hasOption("choice1")).thenReturn(true);

        // When
        boolean isValid = gameService.isValidChoice(testProgress, "choice1");

        // Then
        assertTrue(isValid);
        verify(questService).getStage(testQuest, "start");
    }

    @Test
    void testGetAvailableChoices() {
        // Given
        QuestStageOption option1 = new QuestStageOption("choice1", "Choice 1", "next1");
        QuestStageOption option2 = new QuestStageOption("choice2", "Choice 2", "next2");
        when(questService.getStage(testQuest, "start")).thenReturn(Optional.of(testStage));
        when(testStage.getOptions()).thenReturn(Arrays.asList(option1, option2));

        // When
        List<String> choices = gameService.getAvailableChoices(testProgress);

        // Then
        assertNotNull(choices);
        assertEquals(2, choices.size());
        assertTrue(choices.contains("choice1"));
        assertTrue(choices.contains("choice2"));
    }

    @Test
    void testGetUserQuestProgress() {
        // Given
        when(userQuestProgressDAO.findByUserIdAndQuestId(1L, 1L)).thenReturn(Optional.of(testProgress));

        // When
        UserQuestProgress progress = gameService.getUserQuestProgress(testUser, testQuest);

        // Then
        assertNotNull(progress);
        assertEquals(testProgress, progress);
        verify(userQuestProgressDAO).findByUserIdAndQuestId(1L, 1L);
    }

    @Test
    void testGetUserQuestProgressWithUserId() {
        // Given
        List<UserQuestProgress> progressList = Arrays.asList(testProgress);
        when(userQuestProgressDAO.findByUserId(1L)).thenReturn(progressList);

        // When
        List<UserQuestProgress> progress = gameService.getUserQuestProgress(1L);

        // Then
        assertNotNull(progress);
        assertEquals(1, progress.size());
        assertEquals(testProgress, progress.get(0));
        verify(userQuestProgressDAO).findByUserId(1L);
    }
}