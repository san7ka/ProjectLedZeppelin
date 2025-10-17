package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.model.GameLogic;
import com.javarush.lepeshinskij.model.PlayerStats;
import com.javarush.lepeshinskij.model.Quest;
import com.javarush.lepeshinskij.model.QuestStage;

public class QuestService {
    private static volatile QuestService instance;
    private final Quest quest;
    
    public static final String GAME_LOGIC_ATTR = "gameLogic";
    public static final String PLAYER_STATS_ATTR = "playerStats";
    
    private QuestService() {
        this.quest = new Quest();
    }
    
    public static QuestService getInstance() {
        if (instance == null) {
            synchronized (QuestService.class) {
                if (instance == null) {
                    instance = new QuestService();
                }
            }
        }
        return instance;
    }

    public GameLogic initializeGame() {
        return new GameLogic();
    }

    public QuestStage getCurrentStage(GameLogic gameLogic) {
        return gameLogic.getCurrentStage();
    }

    public boolean processChoice(GameLogic gameLogic, String choice) {
        return gameLogic.processChoice(choice);
    }

    public void updateStats(GameLogic gameLogic, PlayerStats playerStats) {
        playerStats.incrementGamesPlayed();
        if (gameLogic.isWin()) {
            playerStats.incrementGamesWon();
        } else if (gameLogic.isGameOver()) {
            playerStats.incrementLosses();
        }
    }

    public void resetGame(GameLogic gameLogic) {
        gameLogic.reset();
    }

    public Quest getQuest() {
        return quest;
    }

    public void addStage(String stageId, QuestStage stage) {
        quest.addStage(stageId, stage);
    }

    public void updateStage(String stageId, QuestStage stage) {
        quest.updateStage(stageId, stage);
    }

    public void deleteStage(String stageId) {
        quest.deleteStage(stageId);
    }
    
    public void reorderStages(java.util.List<String> stageOrder) {
        quest.reorderStages(stageOrder);
    }
}