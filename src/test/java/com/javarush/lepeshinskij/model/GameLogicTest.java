package com.javarush.lepeshinskij.model;

import com.javarush.lepeshinskij.model.GameLogic;
import com.javarush.lepeshinskij.model.Quest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameLogicTest {

    private GameLogic gameLogic;

    @BeforeEach
    public void setup() {
        gameLogic = new GameLogic();
    }

    @Test
    public void testInitialState() {
        assertEquals(Quest.START, gameLogic.getCurrentStageId());
        assertFalse(gameLogic.isGameOver());
        assertFalse(gameLogic.isWin());
    }

    @Test
    public void testProcessChoice() {
        boolean gameOver = gameLogic.processChoice("left");

        assertFalse(gameOver);
        assertEquals("left", gameLogic.getCurrentStageId());
        assertFalse(gameLogic.isGameOver());

        gameOver = gameLogic.processChoice("run");
        assertTrue(gameOver);
        assertEquals(Quest.LOSE, gameLogic.getCurrentStageId());
        assertTrue(gameLogic.isGameOver());
        assertFalse(gameLogic.isWin());
    }

    @Test
    public void testWinPath() {
        gameLogic.processChoice("right");

        boolean gameOver = gameLogic.processChoice("bridge");
        assertTrue(gameOver);
        assertEquals(Quest.WIN, gameLogic.getCurrentStageId());
        assertTrue(gameLogic.isGameOver());
        assertTrue(gameLogic.isWin());
    }

    @Test
    public void testReset() {
        gameLogic.processChoice("left");
        gameLogic.processChoice("run");
        assertTrue(gameLogic.isGameOver());

        gameLogic.reset();
        assertEquals(Quest.START, gameLogic.getCurrentStageId());
        assertFalse(gameLogic.isGameOver());
        assertFalse(gameLogic.isWin());
    }
}