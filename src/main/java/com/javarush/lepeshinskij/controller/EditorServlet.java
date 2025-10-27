package com.javarush.lepeshinskij.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.QuestStageOption;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.service.QuestService;
import org.hibernate.SessionFactory;
import com.javarush.lepeshinskij.util.HibernateUtil;
import com.javarush.lepeshinskij.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class EditorServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(EditorServlet.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private QuestService questService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
            questService = new QuestService(sessionFactory);
            logger.info("EditorServlet инициализирован успешно");
        } catch (Exception e) {
            logger.error("Ошибка инициализации EditorServlet", e);
            throw new ServletException("Не удалось инициализировать EditorServlet", e);
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
        String editStageId = request.getParameter("edit");
        
        if (editStageId != null && "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            loadStageDataAsJson(request, response, editStageId, currentUser);
            return;
        }
        
        String questIdStr = request.getParameter("questId");
        if (questIdStr == null || questIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Quest ID не указан");
            return;
        }
        
        try {
            Long questId = Long.parseLong(questIdStr);
            Optional<Quest> questOpt = questService.getQuestById(questId);
            
            if (questOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Квест не найден");
                return;
            }
            
            Quest quest = questOpt.get();
            
            if (!quest.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав для редактирования этого квеста");
                return;
            }
            
            Map<String, QuestStage> stagesMap = loadStagesWithOptions(questId);
            
            request.setAttribute("quest", quest);
            request.setAttribute("stages", stagesMap);
            request.setAttribute("user", currentUser);
            
            request.getRequestDispatcher("/WEB-INF/jsp/editor.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID квеста");
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
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action не указан");
            return;
        }
        
        try {
            switch (action) {
                case "add":
                    addStage(request, response, currentUser);
                    break;
                case "update":
                    updateStage(request, response, currentUser);
                    break;
                case "delete":
                    deleteStage(request, response, currentUser);
                    break;
                case "reorder":
                    reorderStages(request, response, currentUser);
                    break;
                case "setStart":
                    setStartStage(request, response, currentUser);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неизвестный action: " + action);
                    break;
            }
        } catch (Exception e) {
            logger.error("Ошибка в doPost для action: " + action, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }
    
    private void loadStageDataAsJson(HttpServletRequest request, HttpServletResponse response, 
                                     String stageId, User currentUser) throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Find the stage
            QuestStage stage = session.createQuery(
                "SELECT s FROM QuestStage s WHERE s.stageId = :stageId", QuestStage.class)
                .setParameter("stageId", stageId)
                .uniqueResult();
            
            if (stage == null) {
                ObjectNode error = objectMapper.createObjectNode();
                error.put("error", "Этап не найден");
                response.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }
            
            // Check permissions
            Quest quest = stage.getQuest();
            if (!quest.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
                ObjectNode error = objectMapper.createObjectNode();
                error.put("error", "У вас нет прав для редактирования этого этапа");
                response.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }
            
            List<QuestStageOption> options = session.createQuery(
                "SELECT o FROM QuestStageOption o WHERE o.stage.id = :stageId ORDER BY o.id", 
                QuestStageOption.class)
                .setParameter("stageId", stage.getId())
                .list();
            
            Map<String, String> children = new LinkedHashMap<>();
            for (QuestStageOption option : options) {
                children.put(option.getTargetStageId(), option.getOptionText());
            }
            
            List<QuestStageOption> parentOptions = session.createQuery(
                "SELECT o FROM QuestStageOption o WHERE o.targetStageId = :stageId AND o.stage.quest.id = :questId", 
                QuestStageOption.class)
                .setParameter("stageId", stageId)
                .setParameter("questId", quest.getId())
                .list();
            
            Map<String, String> parents = new LinkedHashMap<>();
            for (QuestStageOption option : parentOptions) {
                parents.put(option.getStage().getStageId(), option.getOptionText());
            }
            
            ObjectNode stageData = objectMapper.createObjectNode();
            stageData.put("stageId", stage.getStageId());
            stageData.put("title", stage.getTitle());
            stageData.put("description", stage.getDescription());
            stageData.put("stageIndex", stage.getStageIndex());
            stageData.set("children", objectMapper.valueToTree(children));
            stageData.set("parents", objectMapper.valueToTree(parents));
            
            response.getWriter().write(objectMapper.writeValueAsString(stageData));
            
        } catch (Exception e) {
            logger.error("Ошибка при загрузке данных этапа: " + stageId, e);
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", "Ошибка при загрузке данных этапа: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }
    
    private void addStage(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String questIdStr = request.getParameter("questId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        
        if (questIdStr == null || title == null || description == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Недостаточно параметров");
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
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав для редактирования этого квеста");
                return;
            }
            
            List<QuestStage> existingStages = questService.getQuestStages(questId);
            int nextStageIndex = existingStages.size() + 1;
            String stageId = "stage_" + questId + "_" + nextStageIndex;
            
            questService.createStage(quest, stageId, title.trim(), description.trim(), "NONE", nextStageIndex);
            
            logger.info("Этап создан пользователем {}: {} для квеста {}", 
                currentUser.getUsername(), title, quest.getTitle());
            
            response.sendRedirect(request.getContextPath() + "/editor?questId=" + questId);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID квеста");
        }
    }
    
    private void updateStage(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String stageId = request.getParameter("stageId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String[] parentIds = request.getParameterValues("parentIds");
        String[] parentButtonTexts = request.getParameterValues("parentButtonTexts");
        String[] optionKeys = request.getParameterValues("optionKeys");
        String[] optionValues = request.getParameterValues("optionValues");
        
        if (stageId == null || title == null || description == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Недостаточно параметров");
            return;
        }
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Find the stage
            QuestStage stage = session.createQuery(
                "SELECT s FROM QuestStage s WHERE s.stageId = :stageId", QuestStage.class)
                .setParameter("stageId", stageId)
                .uniqueResult();
            
            if (stage == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Этап не найден");
                return;
            }
            
            // Check permissions
            Quest quest = stage.getQuest();
            if (!quest.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав для редактирования этого этапа");
                return;
            }
            
            stage.setTitle(title.trim());
            stage.setDescription(description.trim());
            stage.setUpdatedAt(LocalDateTime.now());
            session.merge(stage);
            
            session.createMutationQuery(
                "DELETE FROM QuestStageOption WHERE stage.id = :stageId")
                .setParameter("stageId", stage.getId())
                .executeUpdate();
            
            if (optionKeys != null && optionValues != null) {
                for (int i = 0; i < optionKeys.length && i < optionValues.length; i++) {
                    if (optionKeys[i] != null && !optionKeys[i].isEmpty() && 
                        optionValues[i] != null && !optionValues[i].isEmpty()) {
                        
                        QuestStageOption option = new QuestStageOption();
                        option.setStage(stage);
                        option.setOptionKey(optionKeys[i]);
                        option.setOptionText(optionValues[i]);
                        option.setTargetStageId(optionKeys[i]);
                        option.setCreatedAt(LocalDateTime.now());
                        session.persist(option);
                    }
                }
            }
            
            session.createMutationQuery(
                "DELETE FROM QuestStageOption WHERE targetStageId = :stageId AND stage.quest.id = :questId")
                .setParameter("stageId", stageId)
                .setParameter("questId", quest.getId())
                .executeUpdate();
            
            if (parentIds != null && parentButtonTexts != null) {
                for (int i = 0; i < parentIds.length && i < parentButtonTexts.length; i++) {
                    if (parentIds[i] != null && !parentIds[i].isEmpty() && 
                        parentButtonTexts[i] != null && !parentButtonTexts[i].isEmpty()) {
                        
                        QuestStage parentStage = session.createQuery(
                            "SELECT s FROM QuestStage s WHERE s.stageId = :stageId AND s.quest.id = :questId", 
                            QuestStage.class)
                            .setParameter("stageId", parentIds[i])
                            .setParameter("questId", quest.getId())
                            .uniqueResult();
                        
                        if (parentStage != null) {
                            QuestStageOption option = new QuestStageOption();
                            option.setStage(parentStage);
                            option.setOptionKey(stageId);
                            option.setOptionText(parentButtonTexts[i]);
                            option.setTargetStageId(stageId);
                            option.setCreatedAt(LocalDateTime.now());
                            session.persist(option);
                        }
                    }
                }
            }
            
            transaction.commit();
            
            logger.info("Этап обновлен пользователем {}: {}", currentUser.getUsername(), stageId);
            
            response.sendRedirect(request.getContextPath() + "/editor?questId=" + quest.getId());
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при обновлении этапа: " + stageId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Ошибка при обновлении этапа: " + e.getMessage());
        }
    }
    
    private void deleteStage(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String stageId = request.getParameter("stageId");
        
        if (stageId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Stage ID не указан");
            return;
        }
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            QuestStage stage = session.createQuery(
                "SELECT s FROM QuestStage s WHERE s.stageId = :stageId", QuestStage.class)
                .setParameter("stageId", stageId)
                .uniqueResult();
            
            if (stage == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Этап не найден");
                return;
            }
            
            Quest quest = stage.getQuest();
            if (!quest.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав для удаления этого этапа");
                return;
            }
            
            Long questId = quest.getId();
            
            session.createMutationQuery(
                "DELETE FROM QuestStageOption WHERE stage.id = :stageId OR targetStageId = :targetStageId")
                .setParameter("stageId", stage.getId())
                .setParameter("targetStageId", stageId)
                .executeUpdate();
            
            session.remove(stage);
            
            transaction.commit();
            
            logger.info("Этап удален пользователем {}: {}", currentUser.getUsername(), stageId);
            
            response.sendRedirect(request.getContextPath() + "/editor?questId=" + questId);
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при удалении этапа: " + stageId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Ошибка при удалении этапа: " + e.getMessage());
        }
    }
    
    private void reorderStages(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String[] stageOrder = request.getParameterValues("stageOrder[]");
        
        if (stageOrder == null || stageOrder.length == 0) {
            sendJsonResponse(response, false, "Порядок этапов не указан");
            return;
        }
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            for (int i = 0; i < stageOrder.length; i++) {
                String stageId = stageOrder[i];
                int newIndex = i + 1;
                
                QuestStage stage = session.createQuery(
                    "SELECT s FROM QuestStage s WHERE s.stageId = :stageId", QuestStage.class)
                    .setParameter("stageId", stageId)
                    .uniqueResult();
                
                if (stage != null) {
                    Quest quest = stage.getQuest();
                    if (!quest.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
                        sendJsonResponse(response, false, "У вас нет прав для изменения порядка этапов");
                        return;
                    }
                    
                    stage.setStageIndex(newIndex);
                    stage.setUpdatedAt(LocalDateTime.now());
                    session.merge(stage);
                }
            }
            
            transaction.commit();
            
            logger.info("Порядок этапов изменен пользователем {}", currentUser.getUsername());
            
            sendJsonResponse(response, true, "Порядок этапов успешно обновлен");
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при изменении порядка этапов", e);
            sendJsonResponse(response, false, "Ошибка при изменении порядка этапов: " + e.getMessage());
        }
    }
    
    private void setStartStage(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String stageId = request.getParameter("stageId");
        
        if (stageId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Stage ID не указан");
            return;
        }
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            QuestStage stage = session.createQuery(
                "SELECT s FROM QuestStage s WHERE s.stageId = :stageId", QuestStage.class)
                .setParameter("stageId", stageId)
                .uniqueResult();
            
            if (stage == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Этап не найден");
                return;
            }
            
            Quest quest = stage.getQuest();
            if (!quest.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав для изменения стартового этапа");
                return;
            }
            
            quest.setStartStageId(stageId);
            quest.setUpdatedAt(LocalDateTime.now());
            session.merge(quest);
            
            transaction.commit();
            
            logger.info("Стартовый этап установлен пользователем {}: {}", currentUser.getUsername(), stageId);
            
            response.sendRedirect(request.getContextPath() + "/editor?questId=" + quest.getId());
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при установке стартового этапа: " + stageId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Ошибка при установке стартового этапа: " + e.getMessage());
        }
    }
    
    private Map<String, QuestStage> loadStagesWithOptions(Long questId) {
        Map<String, QuestStage> stagesMap = new LinkedHashMap<>();
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<QuestStage> stages = session.createQuery(
                "SELECT s FROM QuestStage s WHERE s.quest.id = :questId ORDER BY s.stageIndex", 
                QuestStage.class)
                .setParameter("questId", questId)
                .list();
            
            for (QuestStage stage : stages) {
                List<QuestStageOption> options = session.createQuery(
                    "SELECT o FROM QuestStageOption o WHERE o.stage.id = :stageId ORDER BY o.id", 
                    QuestStageOption.class)
                    .setParameter("stageId", stage.getId())
                    .list();
                
                Map<String, String> optionsMap = new LinkedHashMap<>();
                for (QuestStageOption option : options) {
                    optionsMap.put(option.getTargetStageId(), option.getOptionText());
                }
                
                stage.setOptionsMap(optionsMap);
                stagesMap.put(stage.getStageId(), stage);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при загрузке этапов с опциями для квеста: " + questId, e);
        }
        
        return stagesMap;
    }
    
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        jsonResponse.put("success", success);
        jsonResponse.put(success ? "message" : "error", message);
        
        response.getWriter().write(objectMapper.writeValueAsString(jsonResponse));
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("EditorServlet уничтожен");
    }
}
