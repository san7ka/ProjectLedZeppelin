package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.model.PlayerStats;
import com.javarush.lepeshinskij.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WelcomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PlayerStats stats = SessionUtils.getPlayerStats(req, null);
        req.setAttribute("playerStats", stats);
        req.getRequestDispatcher("/WEB-INF/jsp/welcome.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String playerName = req.getParameter("playerName");
        SessionUtils.resetGameState(req);
        SessionUtils.getPlayerStats(req, playerName);
        resp.sendRedirect(req.getContextPath() + "/game");
    }
}