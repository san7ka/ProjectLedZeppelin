package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.model.PlayerStats;
import com.javarush.lepeshinskij.service.QuestService;
import org.hibernate.SessionFactory;
import com.javarush.lepeshinskij.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class WelcomeServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(WelcomeServlet.class);
    
    private QuestService questService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
            questService = new QuestService(sessionFactory);
            logger.info("WelcomeServlet инициализирован успешно");
        } catch (Exception e) {
            logger.error("Ошибка инициализации WelcomeServlet", e);
            throw new ServletException("Не удалось инициализировать WelcomeServlet", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            User currentUser = SessionUtils.getCurrentUser(request);
            
            PlayerStats playerStats = null;
            if (currentUser == null) {
                playerStats = SessionUtils.getPlayerStats(request, null);
            }
            
            List<Quest> availableQuests = questService.getAllQuestsWithDetails();
            
            List<Quest> popularQuests = availableQuests.stream()
                .limit(3)
                .toList();
            
            List<Quest> recentQuests = questService.getAllQuestsWithDetails().stream()
                .limit(3)
                .toList();
            
            request.setAttribute("user", currentUser);
            request.setAttribute("playerStats", playerStats);
            request.setAttribute("availableQuests", availableQuests);
            request.setAttribute("popularQuests", popularQuests);
            request.setAttribute("recentQuests", recentQuests);
            request.setAttribute("totalQuests", availableQuests.size());
            
            request.getRequestDispatcher("/WEB-INF/jsp/welcome.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Ошибка в doGet", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String playerName = request.getParameter("playerName");
            
            SessionUtils.resetGameState(request);
            
            if (playerName != null && !playerName.trim().isEmpty()) {
                SessionUtils.getPlayerStats(request, playerName.trim());
                request.getSession().setAttribute("playerName", playerName.trim());
            } else {
                SessionUtils.getPlayerStats(request, "Гость");
                request.getSession().setAttribute("playerName", "Гость");
            }
            
            response.sendRedirect(request.getContextPath() + "/game");
            
        } catch (Exception e) {
            logger.error("Ошибка в doPost", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("WelcomeServlet уничтожен");
    }
}
