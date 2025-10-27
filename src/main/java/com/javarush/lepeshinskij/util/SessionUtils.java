package com.javarush.lepeshinskij.util;

import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.model.PlayerStats;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {

    public static final String USER_ATTR = "user";
    public static final String USER_ID_ATTR = "userId";
    public static final String USERNAME_ATTR = "username";
    public static final String IS_ADMIN_ATTR = "isAdmin";
    public static final String PLAYER_STATS_ATTR = "playerStats";

    public static boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(USER_ATTR) != null;
    }

    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(USER_ATTR);
    }

    public static Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (Long) session.getAttribute(USER_ID_ATTR);
    }

    public static String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(USERNAME_ATTR);
    }

    public static boolean isCurrentUserAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Boolean isAdmin = (Boolean) session.getAttribute(IS_ADMIN_ATTR);
        return isAdmin != null && isAdmin;
    }

    public static void setUserInSession(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ATTR, user);
        session.setAttribute(USER_ID_ATTR, user.getId());
        session.setAttribute(USERNAME_ATTR, user.getUsername());
        session.setAttribute(IS_ADMIN_ATTR, user.isAdmin());
        session.removeAttribute(PLAYER_STATS_ATTR);
    }

    public static void clearUserFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(USER_ATTR);
            session.removeAttribute(USER_ID_ATTR);
            session.removeAttribute(USERNAME_ATTR);
            session.removeAttribute(IS_ADMIN_ATTR);
        }
    }

    public static PlayerStats getPlayerStats(HttpServletRequest request, String playerName) {
        HttpSession session = request.getSession(true);
        PlayerStats playerStats = (PlayerStats) session.getAttribute(PLAYER_STATS_ATTR);
        String currentPlayerName = playerStats != null ? playerStats.getPlayerName() : null;

        if (playerStats == null) {
            playerStats = new PlayerStats(playerName != null ? playerName : "Гость");
            session.setAttribute(PLAYER_STATS_ATTR, playerStats);
        } else if (playerName != null && !playerName.isEmpty() && !playerName.equals(currentPlayerName)) {
            playerStats = new PlayerStats(playerName);
            session.setAttribute(PLAYER_STATS_ATTR, playerStats);
        }

        return playerStats;
    }

    public static void resetGameState(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(PLAYER_STATS_ATTR);
        }
    }

    public static PlayerStats getOrCreatePlayerStats(HttpServletRequest request, String playerName) {
        HttpSession session = request.getSession(true);
        PlayerStats playerStats = (PlayerStats) session.getAttribute(PLAYER_STATS_ATTR);
        String currentPlayerName = playerStats != null ? playerStats.getPlayerName() : null;

        if (playerStats == null) {
            playerStats = new PlayerStats(playerName != null ? playerName : "Гость");
            session.setAttribute(PLAYER_STATS_ATTR, playerStats);
        } else if (playerName != null && !playerName.isEmpty() && !playerName.equals(currentPlayerName)) {
            playerStats = new PlayerStats(playerName);
            session.setAttribute(PLAYER_STATS_ATTR, playerStats);
        }

        return playerStats;
    }
}