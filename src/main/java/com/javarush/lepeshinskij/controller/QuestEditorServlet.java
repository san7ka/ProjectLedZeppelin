package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.User;
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


public class QuestEditorServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestEditorServlet.class);
    
    private QuestService questService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
            questService = new QuestService(sessionFactory);
            logger.info("QuestEditorServlet инициализирован успешно");
        } catch (Exception e) {
            logger.error("Ошибка инициализации QuestEditorServlet", e);
            throw new ServletException("Не удалось инициализировать QuestEditorServlet", e);
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
        
        logger.debug("QuestEditorServlet.doGet - pathInfo: '{}', requestURI: '{}', servletPath: '{}'", 
            pathInfo, request.getRequestURI(), request.getServletPath());
        
        try {
            if ("/".equals(pathInfo)) {
                showQuestList(request, response, currentUser);
            } else {
                logger.warn("Неизвестный pathInfo: '{}' для URI: '{}' - используйте /quest-builder для создания/редактирования", 
                    pathInfo, request.getRequestURI());
                response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                    "Для создания и редактирования квестов используйте /quest-builder");
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
            if ("/delete".equals(pathInfo)) {
                deleteQuest(request, response, currentUser);
            } else {
                logger.warn("Неизвестный pathInfo для POST: '{}' - используйте /quest-builder", pathInfo);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                    "Для создания и редактирования квестов используйте /quest-builder");
            }
        } catch (Exception e) {
            logger.error("Ошибка в doPost", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }
    
    private void showQuestList(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        logger.info("Загрузка списка квестов для пользователя: {} (ID: {})", currentUser.getUsername(), currentUser.getId());
        
        try {
            List<Quest> userQuests;
            
            if (currentUser.isAdmin()) {
                logger.info("Пользователь {} является администратором - загрузка всех квестов", currentUser.getUsername());
                userQuests = questService.getAllQuestsWithDetails();
                request.setAttribute("isAdminView", true);
            } else {
                userQuests = questService.getQuestsByAuthor(currentUser);
                request.setAttribute("isAdminView", false);
            }
            
            logger.debug("Найдено квестов: {}", userQuests.size());
            
            request.setAttribute("quests", userQuests);
            request.setAttribute("user", currentUser);
            
            request.getRequestDispatcher("/WEB-INF/jsp/quest-list.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Ошибка при загрузке списка квестов для пользователя: {}", currentUser.getUsername(), e);
            throw e;
        }
    }
    
    private void deleteQuest(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String questIdStr = request.getParameter("id");
        
        if (questIdStr == null || questIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID квеста не указан");
            return;
        }
        
        try {
            Long questId = Long.parseLong(questIdStr);
            Quest quest = questService.getQuestByIdOrNull(questId);
            
            if (quest == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Квест не найден");
                return;
            }
            
            if (!quest.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав для удаления этого квеста");
                return;
            }
            
            questService.deleteQuest(questId);
            
            logger.info("Квест удален пользователем {}: {} (ID: {})", 
                currentUser.getUsername(), quest.getTitle(), questId);
            
            response.sendRedirect(request.getContextPath() + "/quest-editor/");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID квеста");
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("QuestEditorServlet уничтожен");
    }
}
