package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import com.javarush.lepeshinskij.entity.UserStatistics;
import com.javarush.lepeshinskij.service.GameService;
import com.javarush.lepeshinskij.service.UserService;
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
import java.util.List;


public class UserControllerServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(UserControllerServlet.class);
    
    private UserService userService;
    private GameService gameService;
    private UserStatisticsService statisticsService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
            userService = new UserService(sessionFactory);
            gameService = new GameService(sessionFactory);
            statisticsService = new UserStatisticsService(sessionFactory);
            logger.info("UserControllerServlet инициализирован успешно");
        } catch (Exception e) {
            logger.error("Ошибка инициализации UserControllerServlet", e);
            throw new ServletException("Не удалось инициализировать UserControllerServlet", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!SessionUtils.isUserLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        User currentUser = SessionUtils.getCurrentUser(request);
        if (currentUser == null) {
            logger.error("Текущий пользователь null после проверки авторизации");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка получения пользователя");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }
        
        logger.debug("UserControllerServlet.doGet - pathInfo: '{}', requestURI: '{}', servletPath: '{}'", 
            pathInfo, request.getRequestURI(), request.getServletPath());
        
        try {
            switch (pathInfo) {
                case "/":
                case "/profile":
                    showProfile(request, response, currentUser);
                    break;
                case "/quests":
                    showUserQuests(request, response, currentUser);
                    break;
                case "/statistics":
                    showStatistics(request, response, currentUser);
                    break;
                case "/admin":
                    showAdminPanel(request, response, currentUser);
                    break;
                default:
                    logger.warn("Неизвестный pathInfo: '{}' для URI: '{}'", pathInfo, request.getRequestURI());
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (Exception e) {
            logger.error("Ошибка в doGet для pathInfo: '{}', URI: '{}'", pathInfo, request.getRequestURI(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!SessionUtils.isUserLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        User currentUser = SessionUtils.getCurrentUser(request);
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }
        
        try {
            switch (pathInfo) {
                case "/update":
                    updateProfile(request, response, currentUser);
                    break;
                case "/admin/delete-user":
                    deleteUser(request, response, currentUser);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (Exception e) {
            logger.error("Ошибка в doPost", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }
    
    private void showProfile(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        logger.info("Загрузка профиля для пользователя: {} (ID: {})", currentUser.getUsername(), currentUser.getId());
        
        try {
            List<UserQuestProgress> activeGames = gameService.getActiveGames(currentUser);
            logger.debug("Активных игр: {}", activeGames.size());
            
            List<UserQuestProgress> completedGames = gameService.getCompletedGames(currentUser);
            logger.debug("Завершенных игр: {}", completedGames.size());
            
            UserStatistics statistics = null;
            try {
                statistics = statisticsService.getUserStatistics(currentUser);
                logger.info("Статистика загружена: {}", statistics);
            } catch (Exception e) {
                logger.error("Ошибка при загрузке статистики для пользователя: {}", currentUser.getUsername(), e);
            }
            
            request.setAttribute("user", currentUser);
            request.setAttribute("activeGames", activeGames);
            request.setAttribute("completedGames", completedGames);
            request.setAttribute("statistics", statistics);
            
            request.getRequestDispatcher("/WEB-INF/jsp/user-profile.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Ошибка при загрузке профиля для пользователя: {}", currentUser.getUsername(), e);
            throw e;
        }
    }
    
    private void showUserQuests(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        logger.info("Загрузка квестов для пользователя: {}", currentUser.getUsername());
        
        try {
            List<com.javarush.lepeshinskij.entity.Quest> userQuests = 
                new com.javarush.lepeshinskij.service.QuestService((SessionFactory) getServletContext().getAttribute("sessionFactory")).getQuestsByAuthor(currentUser);
            
            request.setAttribute("user", currentUser);
            request.setAttribute("quests", userQuests);
            
            response.sendRedirect(request.getContextPath() + "/quest-editor");
        } catch (Exception e) {
            logger.error("Ошибка при загрузке квестов пользователя: {}", currentUser.getUsername(), e);
            throw e;
        }
    }
    
    private void showStatistics(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        List<UserQuestProgress> allGames = gameService.getAllUserGames(currentUser);
        long totalGames = allGames.size();
        long completedGames = allGames.stream().filter(UserQuestProgress::getIsCompleted).count();
        
        request.setAttribute("user", currentUser);
        request.setAttribute("totalGames", totalGames);
        request.setAttribute("completedGames", completedGames);
        request.setAttribute("allGames", allGames);
        
        request.getRequestDispatcher("/WEB-INF/jsp/statistics.jsp").forward(request, response);
    }
    
    private void showAdminPanel(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        if (!currentUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав администратора");
            return;
        }
        
        List<User> allUsers = userService.getAllUsers();
        
        request.setAttribute("user", currentUser);
        request.setAttribute("allUsers", allUsers);
        
        request.getRequestDispatcher("/WEB-INF/jsp/admin.jsp").forward(request, response);
    }
    
    private void updateProfile(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        try {
            if (email != null && !email.trim().isEmpty()) {
                currentUser.setEmail(email.trim());
            }
            
            if (password != null && !password.isEmpty()) {
                if (!password.equals(confirmPassword)) {
                    request.setAttribute("error", "Пароли не совпадают");
                    showProfile(request, response, currentUser);
                    return;
                }
                
                if (password.length() < 6) {
                    request.setAttribute("error", "Пароль должен содержать минимум 6 символов");
                    showProfile(request, response, currentUser);
                    return;
                }
                
                userService.changePassword(currentUser, password);
            }
            
            userService.updateUser(currentUser);
            
            logger.info("Профиль пользователя {} обновлен", currentUser.getUsername());
            
            request.setAttribute("success", "Профиль успешно обновлен");
            showProfile(request, response, currentUser);
            
        } catch (Exception e) {
            logger.error("Ошибка при обновлении профиля", e);
            request.setAttribute("error", "Произошла ошибка при обновлении профиля");
            showProfile(request, response, currentUser);
        }
    }
    
    private void deleteUser(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        if (!currentUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав администратора");
            return;
        }
        
        String userIdStr = request.getParameter("userId");
        
        if (userIdStr == null || userIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID пользователя не указан");
            return;
        }
        
        try {
            Long userId = Long.parseLong(userIdStr);
            
            if (userId.equals(currentUser.getId())) {
                request.setAttribute("error", "Вы не можете удалить свой собственный аккаунт");
                showAdminPanel(request, response, currentUser);
                return;
            }
            
            userService.deleteUser(userId);
            
            logger.info("Пользователь с ID {} удален администратором {}", userId, currentUser.getUsername());
            
            response.sendRedirect(request.getContextPath() + "/user/admin");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID пользователя");
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("UserControllerServlet уничтожен");
    }
}
