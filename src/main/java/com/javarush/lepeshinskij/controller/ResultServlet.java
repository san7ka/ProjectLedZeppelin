package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.model.GameLogic;
import com.javarush.lepeshinskij.model.PlayerStats;
import com.javarush.lepeshinskij.service.QuestService;
import com.javarush.lepeshinskij.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResultServlet extends HttpServlet {
    private final QuestService questService = QuestService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GameLogic gameLogic = SessionUtils.getGameLogic(req);

        if (!gameLogic.isGameOver()) {
            resp.sendRedirect(req.getContextPath() + "/game");
            return;
        }

        PlayerStats stats = SessionUtils.getPlayerStats(req, null);
        req.setAttribute("isWin", gameLogic.isWin());
        req.setAttribute("playerStats", stats);
        req.getRequestDispatcher("/WEB-INF/jsp/result.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GameLogic newGameLogic = questService.initializeGame();
        req.getSession().setAttribute(QuestService.GAME_LOGIC_ATTR, newGameLogic);
        resp.sendRedirect(req.getContextPath() + "/game");
    }
}