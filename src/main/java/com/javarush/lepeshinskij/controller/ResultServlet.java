package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import com.javarush.lepeshinskij.model.PlayerStats;
import com.javarush.lepeshinskij.service.GameService;
import com.javarush.lepeshinskij.service.UserStatisticsService;
import org.hibernate.SessionFactory;
import com.javarush.lepeshinskij.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ResultServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(ResultServlet.class);
    
    private GameService gameService;
    private UserStatisticsService statisticsService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
            gameService = new GameService(sessionFactory);
            statisticsService = new UserStatisticsService(sessionFactory);
            logger.info("ResultServlet инициализирован успешно");
        } catch (Exception e) {
            logger.error("Ошибка инициализации ResultServlet", e);
            throw new ServletException("Не удалось инициализировать ResultServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String progressIdStr = request.getParameter("progressId");
            
            if (progressIdStr != null && !progressIdStr.isEmpty()) {
                showResultForRegisteredUser(request, response, Long.parseLong(progressIdStr));
            } else {
                showResultForAnonymousUser(request, response);
            }
            
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID прогресса", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID прогресса");
        } catch (Exception e) {
            logger.error("Ошибка в doGet", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String action = request.getParameter("action");
            
            if ("restart".equals(action)) {
                String questIdStr = request.getParameter("questId");
                if (questIdStr != null && !questIdStr.isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/game?questId=" + questIdStr);
                } else {
                    response.sendRedirect(request.getContextPath() + "/game");
                }
            } else if ("new_quest".equals(action)) {
                response.sendRedirect(request.getContextPath() + "/game");
            } else {
                response.sendRedirect(request.getContextPath() + "/game");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка в doPost", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }
    
    private void showResultForRegisteredUser(HttpServletRequest request, HttpServletResponse response, Long progressId) 
            throws ServletException, IOException {
        
        try {
            UserQuestProgress progress = gameService.getProgressById(progressId);
            if (progress == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Прогресс игры не найден");
                return;
            }
            
            User currentUser = SessionUtils.getCurrentUser(request);
            
            if (currentUser != null && !progress.getUser().getId().equals(currentUser.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет доступа к этому результату");
                return;
            }
            
            if (!progress.getIsCompleted()) {
                response.sendRedirect(request.getContextPath() + "/game?progressId=" + progressId);
                return;
            }
            
            QuestStage finalStage = gameService.getCurrentStage(progress);
            
            boolean isWin = false;
            if (finalStage != null) {
                isWin = finalStage.getResultType() == QuestStage.ResultType.WIN;
            }
            
            if (currentUser != null) {
                var allGames = gameService.getAllUserGames(currentUser);
                long totalGames = allGames.size();
                long completedGames = allGames.stream()
                    .filter(UserQuestProgress::getIsCompleted)
                    .count();
                long wonGames = allGames.stream()
                    .filter(UserQuestProgress::getIsCompleted)
                    .filter(g -> {
                        QuestStage stage = gameService.getCurrentStage(g);
                        return stage != null && stage.isWinStage();
                    })
                    .count();
                
                request.setAttribute("totalGames", totalGames);
                request.setAttribute("completedGames", completedGames);
                request.setAttribute("wonGames", wonGames);
                
                logger.debug("Статистика пользователя {}: всего игр={}, завершено={}, побед={}",
                    currentUser.getUsername(), totalGames, completedGames, wonGames);
            }
            
            request.setAttribute("progress", progress);
            request.setAttribute("quest", progress.getQuest());
            request.setAttribute("finalStage", finalStage);
            request.setAttribute("isWin", isWin);
            request.setAttribute("user", currentUser);
            
            request.getRequestDispatcher("/WEB-INF/jsp/result.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Ошибка при показе результата", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при показе результата");
        }
    }
    
    private void showResultForAnonymousUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        PlayerStats playerStats = SessionUtils.getPlayerStats(request, null);
        if (playerStats == null) {
            response.sendRedirect(request.getContextPath() + "/welcome");
            return;
        }
        
        Boolean isWin = (Boolean) request.getSession().getAttribute("isWin");
        QuestStage finalStage = (QuestStage) request.getSession().getAttribute("finalStage");
        Quest quest = (Quest) request.getSession().getAttribute("lastQuest");
        Boolean isAnonymous = (Boolean) request.getSession().getAttribute("isAnonymous");
        
        request.setAttribute("playerStats", playerStats);
        
        if (isWin != null) {
            request.setAttribute("isWin", isWin);
        }
        
        if (finalStage != null) {
            request.setAttribute("finalStage", finalStage);
        }
        
        if (quest != null) {
            request.setAttribute("quest", quest);
        }
        
        if (isAnonymous != null) {
            request.setAttribute("isAnonymous", isAnonymous);
        }
        
        request.getSession().removeAttribute("isWin");
        request.getSession().removeAttribute("finalStage");
        request.getSession().removeAttribute("lastQuest");
        request.getSession().removeAttribute("isAnonymous");
        
        request.getRequestDispatcher("/WEB-INF/jsp/result.jsp").forward(request, response);
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("ResultServlet уничтожен");
    }
}
