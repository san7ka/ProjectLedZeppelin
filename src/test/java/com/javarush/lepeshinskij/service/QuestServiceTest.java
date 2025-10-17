package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.model.GameLogic;
import com.javarush.lepeshinskij.model.PlayerStats;
import com.javarush.lepeshinskij.model.QuestStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuestServiceTest {

    private QuestService questService;
    private GameLogic gameLogic;
    private PlayerStats playerStats;

    @BeforeEach
    public void setup() {
        questService = QuestService.getInstance();
        gameLogic = questService.initializeGame();
        playerStats = new PlayerStats("TestPlayer");
    }

    @Test
    public void testInitializeGame() {
        GameLogic newGameLogic = questService.initializeGame();
        assertNotNull(newGameLogic);
        assertNotNull(newGameLogic.getCurrentStage());
        assertEquals("start", newGameLogic.getCurrentStageId());
    }

    @Test
    public void testGetCurrentStage() {
        QuestStage stage = questService.getCurrentStage(gameLogic);
        assertNotNull(stage);
        assertEquals("Начало игры", stage.getTitle());
        assertTrue(stage.getOptions().size() > 0);
    }

    @Test
    public void testProcessChoice() {
        boolean gameOver = questService.processChoice(gameLogic, "left");
        assertFalse(gameOver);

        gameOver = questService.processChoice(gameLogic, "run");
        assertTrue(gameOver);
    }

    @Test
    public void testUpdateStatsWin() {
        gameLogic.processChoice("right");
        gameLogic.processChoice("bridge");

        questService.updateStats(gameLogic, playerStats);
        assertEquals(1, playerStats.getGamesPlayed());
        assertEquals(1, playerStats.getWins());
        assertEquals(0, playerStats.getLosses());
    }

    @Test
    public void testUpdateStatsLose() {
        gameLogic.processChoice("left");
        gameLogic.processChoice("run");

        questService.updateStats(gameLogic, playerStats);
        assertEquals(1, playerStats.getGamesPlayed());
        assertEquals(0, playerStats.getWins());
        assertEquals(1, playerStats.getLosses());
    }

    @Test
    public void testResetGame() {
        questService.processChoice(gameLogic, "left");
        assertNotEquals("start", gameLogic.getCurrentStageId());

        questService.resetGame(gameLogic);
        assertEquals("start", gameLogic.getCurrentStageId());
    }
}