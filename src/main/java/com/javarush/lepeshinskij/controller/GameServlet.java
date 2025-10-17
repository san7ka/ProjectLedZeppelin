package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.model.GameLogic;
import com.javarush.lepeshinskij.model.PlayerStats;
import com.javarush.lepeshinskij.model.QuestStage;
import com.javarush.lepeshinskij.service.QuestService;
import com.javarush.lepeshinskij.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GameServlet extends HttpServlet {
    private final QuestService questService = QuestService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GameLogic gameLogic = SessionUtils.getGameLogic(req);

        if (gameLogic.isGameOver()) {
            resp.sendRedirect(req.getContextPath() + "/result");
            return;
        }

        QuestStage currentStage = gameLogic.getCurrentStage();
        req.setAttribute("stage", currentStage);
        String playerName = (String) req.getSession().getAttribute("playerName");
        PlayerStats stats = SessionUtils.getPlayerStats(req, playerName);
        req.setAttribute("playerStats", stats);
        req.getRequestDispatcher("/WEB-INF/jsp/game.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GameLogic gameLogic = SessionUtils.getGameLogic(req);
        String choice = req.getParameter("choice");
        boolean gameOver = questService.processChoice(gameLogic, choice);

        if (gameOver) {
            String playerName = (String) req.getSession().getAttribute("playerName");
            PlayerStats stats = SessionUtils.getPlayerStats(req, playerName);
            questService.updateStats(gameLogic, stats);
            resp.sendRedirect(req.getContextPath() + "/result");
        } else {
            resp.sendRedirect(req.getContextPath() + "/game");
        }
    }
}