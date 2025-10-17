package com.javarush.lepeshinskij.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuestLogicTest {

    private GameLogic gameLogic;

    @BeforeEach
    public void setup() {
        gameLogic = new GameLogic();
    }

    @Test
    public void testWinPathThroughBridge() {
        assertFalse(gameLogic.processChoice("right"));
        assertEquals("right", gameLogic.getCurrentStageId());
        
        assertTrue(gameLogic.processChoice("bridge"));
        assertEquals(Quest.WIN, gameLogic.getCurrentStageId());
        assertTrue(gameLogic.isGameOver());
        assertTrue(gameLogic.isWin());
    }

    @Test
    public void testWinPathThroughHoney() {
        assertFalse(gameLogic.processChoice("left"));
        assertEquals("left", gameLogic.getCurrentStageId());
        
        assertFalse(gameLogic.processChoice("fight"));
        assertEquals("fight", gameLogic.getCurrentStageId());
        
        assertTrue(gameLogic.processChoice("take_honey"));
        assertEquals(Quest.WIN, gameLogic.getCurrentStageId());
        assertTrue(gameLogic.isGameOver());
        assertTrue(gameLogic.isWin());
    }

    @Test
    public void testLosePathThroughRunning() {
        assertFalse(gameLogic.processChoice("left"));
        assertEquals("left", gameLogic.getCurrentStageId());
        
        assertTrue(gameLogic.processChoice("run"));
        assertEquals(Quest.LOSE, gameLogic.getCurrentStageId());
        assertTrue(gameLogic.isGameOver());
        assertFalse(gameLogic.isWin());
    }

    @Test
    public void testLosePathThroughSwimming() {
        assertFalse(gameLogic.processChoice("right"));
        assertEquals("right", gameLogic.getCurrentStageId());
        
        assertTrue(gameLogic.processChoice("swim"));
        assertEquals(Quest.LOSE, gameLogic.getCurrentStageId());
        assertTrue(gameLogic.isGameOver());
        assertFalse(gameLogic.isWin());
    }

    @Test
    public void testLosePathThroughLeavingHoney() {
        assertFalse(gameLogic.processChoice("left"));
        assertEquals("left", gameLogic.getCurrentStageId());
        
        assertFalse(gameLogic.processChoice("fight"));
        assertEquals("fight", gameLogic.getCurrentStageId());
        
        assertTrue(gameLogic.processChoice("leave_honey"));
        assertEquals(Quest.LOSE, gameLogic.getCurrentStageId());
        assertTrue(gameLogic.isGameOver());
        assertFalse(gameLogic.isWin());
    }
}
