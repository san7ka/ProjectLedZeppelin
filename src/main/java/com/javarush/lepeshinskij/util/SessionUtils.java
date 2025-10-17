package com.javarush.lepeshinskij.util;

import com.javarush.lepeshinskij.model.GameLogic;
import com.javarush.lepeshinskij.model.PlayerStats;
import com.javarush.lepeshinskij.service.QuestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {

    public static GameLogic getGameLogic(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        GameLogic gameLogic = (GameLogic) session.getAttribute(QuestService.GAME_LOGIC_ATTR);

        if (gameLogic == null) {
            gameLogic = new GameLogic();
            session.setAttribute(QuestService.GAME_LOGIC_ATTR, gameLogic);
        }

        return gameLogic;
    }

    public static PlayerStats getPlayerStats(HttpServletRequest request, String playerName) {
        HttpSession session = request.getSession(true);
        PlayerStats playerStats = (PlayerStats) session.getAttribute(QuestService.PLAYER_STATS_ATTR);
        String currentPlayerName = playerStats != null ? playerStats.getPlayerName() : null;

        if (playerStats == null) {
            playerStats = new PlayerStats(playerName != null ? playerName : "Гость");
            session.setAttribute(QuestService.PLAYER_STATS_ATTR, playerStats);
        } else if (playerName != null && !playerName.isEmpty() && !playerName.equals(currentPlayerName)) {
            playerStats = new PlayerStats(playerName);
            session.setAttribute(QuestService.PLAYER_STATS_ATTR, playerStats);
        }

        return playerStats;
    }

    public static void resetGameState(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(QuestService.GAME_LOGIC_ATTR);
        }
    }
}