package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.entity.SessionHistory;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.service.QuestPlayService;
import com.javarush.lepeshinskij.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class GameActionServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(GameActionServlet.class);
    
    private QuestPlayService questPlayService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
            questPlayService = new QuestPlayService(sessionFactory);
            logger.info("GameActionServlet инициализирован успешно");
        } catch (Exception e) {
            logger.error("Ошибка инициализации GameActionServlet", e);
            throw new ServletException("Не удалось инициализировать GameActionServlet", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!SessionUtils.isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Требуется авторизация");
            return;
        }
        
        User currentUser = SessionUtils.getCurrentUser(request);
        String action = request.getParameter("action");
        String sessionIdStr = request.getParameter("sessionId");
        
        if (sessionIdStr == null || sessionIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID сессии не указан");
            return;
        }
        
        try {
            Long sessionId = Long.parseLong(sessionIdStr);
            
            switch (action) {
                case "goBack":
                    handleGoBack(request, response, sessionId, currentUser);
                    break;
                case "getHistory":
                    handleGetHistory(request, response, sessionId, currentUser);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неизвестное действие");
            }
            
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID сессии", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID");
        } catch (Exception e) {
            logger.error("Ошибка обработки действия", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка обработки");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!SessionUtils.isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Требуется авторизация");
            return;
        }
        
        User currentUser = SessionUtils.getCurrentUser(request);
        String action = request.getParameter("action");
        String sessionIdStr = request.getParameter("sessionId");
        
        if (sessionIdStr == null || sessionIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID сессии не указан");
            return;
        }
        
        try {
            Long sessionId = Long.parseLong(sessionIdStr);
            
            switch (action) {
                case "canGoBack":
                    handleCanGoBack(request, response, sessionId);
                    break;
                case "getHistory":
                    handleGetHistory(request, response, sessionId, currentUser);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неизвестное действие");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка обработки запроса", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка обработки");
        }
    }
    
    private void handleGoBack(HttpServletRequest request, HttpServletResponse response, 
                             Long sessionId, User currentUser) 
            throws IOException {
        
        try {
            logger.info("Откат для сессии ID: {} пользователем: {}", sessionId, currentUser.getUsername());
            
            if (!questPlayService.canGoBack(sessionId)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Невозможно откатиться назад");
                return;
            }
            
            questPlayService.goBack(sessionId);
            
            logger.info("Откат выполнен успешно для сессии ID: {}", sessionId);
            
            // Редирект обратно к игре
            response.sendRedirect(request.getContextPath() + "/game?progressId=" + sessionId);
            
        } catch (IllegalStateException e) {
            logger.warn("Невозможно откатиться: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка при откате", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при откате");
        }
    }
    
    private void handleGetHistory(HttpServletRequest request, HttpServletResponse response, 
                                 Long sessionId, User currentUser) 
            throws ServletException, IOException {
        
        try {
            logger.debug("Получение истории для сессии ID: {}", sessionId);
            
            List<SessionHistory> history = questPlayService.getHistory(sessionId);
            
            request.setAttribute("history", history);
            request.setAttribute("sessionId", sessionId);
            
            request.getRequestDispatcher("/WEB-INF/jsp/game-history.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Ошибка получения истории", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка получения истории");
        }
    }
    
    private void handleCanGoBack(HttpServletRequest request, HttpServletResponse response, 
                                Long sessionId) 
            throws IOException {
        
        try {
            boolean canGoBack = questPlayService.canGoBack(sessionId);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"canGoBack\": " + canGoBack + "}");
            
        } catch (Exception e) {
            logger.error("Ошибка проверки возможности отката", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка проверки");
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("GameActionServlet уничтожен");
    }
}
