package com.javarush.lepeshinskij.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.javarush.lepeshinskij.entity.*;
import com.javarush.lepeshinskij.service.QuestService;
import com.javarush.lepeshinskij.service.QuestValidationService;
import com.javarush.lepeshinskij.util.HibernateUtil;
import com.javarush.lepeshinskij.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@MultipartConfig
public class QuestBuilderServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestBuilderServlet.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private QuestService questService;
    private QuestValidationService validationService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
            questService = new QuestService(sessionFactory);
            validationService = new QuestValidationService();
            logger.info("QuestBuilderServlet инициализирован успешно");
        } catch (Exception e) {
            logger.error("Ошибка инициализации QuestBuilderServlet", e);
            throw new ServletException("Не удалось инициализировать QuestBuilderServlet", e);
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
        if (!currentUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав для создания квестов");
            return;
        }
        
        String questIdStr = request.getParameter("questId");
        String action = request.getParameter("action");
        
        try {
            if (questIdStr != null && !questIdStr.trim().isEmpty() && !"null".equalsIgnoreCase(questIdStr)) {
                Long questId = Long.parseLong(questIdStr);
                
                if ("validate".equals(action)) {
                    validateQuestAjax(request, response, questId, currentUser);
                    return;
                } else if ("getStage".equals(action)) {
                    getStageData(request, response, questId, currentUser);
                    return;
                }
                
                showQuestBuilder(request, response, questId, currentUser);
            } else {
                request.getRequestDispatcher("/WEB-INF/jsp/quest-builder-new.jsp").forward(request, response);
            }
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
        if (!currentUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав для создания квестов");
            return;
        }
        
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        logger.info("QuestBuilderServlet.doPost - action: '{}', contentType: '{}', method: '{}'", 
            action, request.getContentType(), request.getMethod());
        
        if (action == null || action.trim().isEmpty()) {
            logger.error("Action parameter is null or empty. All parameters: {}", 
                request.getParameterMap().keySet());
            sendJsonError(response, "Action не указан. Параметры: " + request.getParameterMap().keySet());
            return;
        }
        
        try {
            switch (action) {
                case "createQuest":
                    createQuest(request, response, currentUser);
                    break;
                case "saveStage":
                    saveStage(request, response, currentUser);
                    break;
                case "deleteStage":
                    deleteStage(request, response, currentUser);
                    break;
                case "publish":
                    publishQuest(request, response, currentUser);
                    break;
                case "unpublish":
                    unpublishQuest(request, response, currentUser);
                    break;
                default:
                    logger.warn("Неизвестный action: '{}'", action);
                    sendJsonError(response, "Неизвестный action: " + action);
                    break;
            }
        } catch (Exception e) {
            logger.error("Ошибка в doPost для action: " + action, e);
            sendJsonError(response, "Внутренняя ошибка: " + e.getMessage());
        }
    }
    

    private void showQuestBuilder(HttpServletRequest request, HttpServletResponse response, 
                                  Long questId, User currentUser) 
            throws ServletException, IOException {
        
        Optional<Quest> questOpt = questService.getQuestById(questId);
        
        if (questOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Квест не найден");
            return;
        }
        
        Quest quest = questOpt.get();
        
        if (!quest.getAuthor().getId().equals(currentUser.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет прав для редактирования этого квеста");
            return;
        }
        
        List<QuestStage> stages = loadStagesWithOptions(questId);
        
        quest.setStages(stages);
        QuestValidationService.ValidationResult validationResult = validationService.validateQuest(quest);
        
        request.setAttribute("quest", quest);
        request.setAttribute("stages", stages);
        request.setAttribute("validationResult", validationResult);
        request.setAttribute("questStatusInfo", validationService.getQuestStatusInfo(quest));
        
        request.getRequestDispatcher("/WEB-INF/jsp/quest-builder.jsp").forward(request, response);
    }
    

    private void createQuest(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        
        logger.info("createQuest called - title: '{}', description length: {}", 
            title, description != null ? description.length() : 0);
        
        if (title == null || title.trim().isEmpty()) {
            request.setAttribute("error", "Название квеста не может быть пустым");
            request.getRequestDispatcher("/WEB-INF/jsp/quest-builder-new.jsp").forward(request, response);
            return;
        }
        
        if (description == null || description.trim().isEmpty()) {
            request.setAttribute("error", "Описание квеста не может быть пустым");
            request.getRequestDispatcher("/WEB-INF/jsp/quest-builder-new.jsp").forward(request, response);
            return;
        }
        
        try {
            Quest quest = questService.createQuest(title.trim(), description.trim(), currentUser);
            if (quest == null || quest.getId() == null) {
                logger.error("Quest creation returned null or null ID");
                request.setAttribute("error", "Не удалось создать квест - вернулся null");
                request.getRequestDispatcher("/WEB-INF/jsp/quest-builder-new.jsp").forward(request, response);
                return;
            }
            
            quest.setStatus(QuestStatus.DRAFT);
            questService.updateQuest(quest);
            
            logger.info("Квест создан пользователем {}: {} (ID: {})", 
                currentUser.getUsername(), title, quest.getId());
            
            response.sendRedirect(request.getContextPath() + "/quest-builder?questId=" + quest.getId());
            
        } catch (Exception e) {
            logger.error("Ошибка при создании квеста", e);
            request.setAttribute("error", "Произошла ошибка при создании квеста: " + e.getMessage());
            request.setAttribute("title", title);
            request.setAttribute("description", description);
            request.getRequestDispatcher("/WEB-INF/jsp/quest-builder-new.jsp").forward(request, response);
        }
    }
    

    private void saveStage(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String questIdStr = request.getParameter("questId");
        String stageIdParam = request.getParameter("stageId");
        String title = request.getParameter("title");
        
        String option1Text = request.getParameter("option1Text");
        String option1Target = request.getParameter("option1Target");
        String option1ResultType = request.getParameter("option1ResultType");
        String option2Text = request.getParameter("option2Text");
        String option2Target = request.getParameter("option2Target");
        String option2ResultType = request.getParameter("option2ResultType");
        
        logger.info("saveStage called - questId: '{}', stageId: '{}', title: '{}'", 
            questIdStr, stageIdParam, title);
        
        if (questIdStr == null || questIdStr.trim().isEmpty()) {
            sendJsonError(response, "Quest ID не указан");
            return;
        }
        
        if (title == null || title.trim().isEmpty()) {
            sendJsonError(response, "Текст этапа обязателен");
            return;
        }
        
        if (option1Text == null || option1Text.trim().isEmpty() || 
            option2Text == null || option2Text.trim().isEmpty()) {
            sendJsonError(response, "Каждый этап должен иметь ровно 2 ответа");
            return;
        }
        
        if (option1ResultType == null || option1ResultType.trim().isEmpty()) {
            option1ResultType = "NONE";
        }
        if (option2ResultType == null || option2ResultType.trim().isEmpty()) {
            option2ResultType = "NONE";
        }
        
        if ("NONE".equals(option1ResultType) && option1Target != null && !option1Target.trim().isEmpty()) {
        }
        if ("NONE".equals(option2ResultType) && option2Target != null && !option2Target.trim().isEmpty()) {
        }
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Long questId = Long.parseLong(questIdStr);
            Quest quest = session.get(Quest.class, questId);
            
            if (quest == null) {
                sendJsonError(response, "Квест не найден");
                return;
            }
            
            if (!quest.getAuthor().getId().equals(currentUser.getId())) {
                sendJsonError(response, "У вас нет прав для редактирования этого квеста");
                return;
            }
            
            QuestStage stage;
            boolean isNewStage = (stageIdParam == null || stageIdParam.isEmpty());
            
            if (isNewStage) {
                List<QuestStage> existingStages = session.createQuery(
                    "SELECT s FROM QuestStage s WHERE s.quest.id = :questId", QuestStage.class)
                    .setParameter("questId", questId)
                    .list();
                
                int nextStageIndex = existingStages.size() + 1;
                String newStageId = generateStageId(questId, nextStageIndex);
                
                stage = new QuestStage();
                stage.setQuest(quest);
                stage.setStageId(newStageId);
                stage.setStageIndex(nextStageIndex);
                stage.setCreatedAt(LocalDateTime.now());
            } else {
                stage = session.createQuery(
                    "SELECT s FROM QuestStage s WHERE s.stageId = :stageId AND s.quest.id = :questId", 
                    QuestStage.class)
                    .setParameter("stageId", stageIdParam)
                    .setParameter("questId", questId)
                    .uniqueResult();
                
                if (stage == null) {
                    sendJsonError(response, "Этап не найден");
                    return;
                }
                
                session.createMutationQuery(
                    "DELETE FROM QuestStageOption WHERE stage.id = :stageId")
                    .setParameter("stageId", stage.getId())
                    .executeUpdate();
            }
            
            stage.setTitle(title.trim());
            stage.setDescription("");
            stage.setResultType(QuestStage.ResultType.NONE);
            stage.setUpdatedAt(LocalDateTime.now());
            
            if (isNewStage) {
                session.persist(stage);
                session.flush();
            } else {
                session.merge(stage);
            }
            
            QuestStageOption option1 = new QuestStageOption();
            option1.setStage(stage);
            option1.setOptionKey("option1");
            option1.setOptionText(option1Text.trim());
            option1.setResultType(QuestStageOption.ResultType.valueOf(option1ResultType));
            if ("NONE".equals(option1ResultType) && option1Target != null && !option1Target.trim().isEmpty()) {
                option1.setTargetStageId(option1Target.trim());
            } else if (!"NONE".equals(option1ResultType)) {
                option1.setTargetStageId(null);
            }
            option1.setCreatedAt(LocalDateTime.now());
            session.persist(option1);
            
            QuestStageOption option2 = new QuestStageOption();
            option2.setStage(stage);
            option2.setOptionKey("option2");
            option2.setOptionText(option2Text.trim());
            option2.setResultType(QuestStageOption.ResultType.valueOf(option2ResultType));
            if ("NONE".equals(option2ResultType) && option2Target != null && !option2Target.trim().isEmpty()) {
                option2.setTargetStageId(option2Target.trim());
            } else if (!"NONE".equals(option2ResultType)) {
                option2.setTargetStageId(null);
            }
            option2.setCreatedAt(LocalDateTime.now());
            session.persist(option2);
            
            transaction.commit();
            
            logger.info("Этап {} пользователем {}: {} (stage ID: {})", 
                isNewStage ? "создан" : "обновлен", currentUser.getUsername(), stage.getTitle(), stage.getStageId());
            
            sendJsonSuccess(response, "Этап успешно сохранен", stage.getStageId());
            
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                    logger.info("Transaction rolled back successfully");
                } catch (Exception rollbackEx) {
                    logger.error("Error rolling back transaction", rollbackEx);
                }
            }
            logger.error("Ошибка при сохранении этапа", e);
            sendJsonError(response, "Ошибка при сохранении этапа: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
    
    private void deleteStage(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String questIdStr = request.getParameter("questId");
        String stageId = request.getParameter("stageId");
        
        if (questIdStr == null || stageId == null) {
            sendJsonError(response, "Недостаточно параметров");
            return;
        }
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Long questId = Long.parseLong(questIdStr);
            Quest quest = session.get(Quest.class, questId);
            
            if (quest == null) {
                sendJsonError(response, "Квест не найден");
                return;
            }
            
            if (!quest.getAuthor().getId().equals(currentUser.getId())) {
                sendJsonError(response, "У вас нет прав для редактирования этого квеста");
                return;
            }
            
            QuestStage stage = session.createQuery(
                "SELECT s FROM QuestStage s WHERE s.stageId = :stageId AND s.quest.id = :questId", 
                QuestStage.class)
                .setParameter("stageId", stageId)
                .setParameter("questId", questId)
                .uniqueResult();
            
            if (stage == null) {
                sendJsonError(response, "Этап не найден");
                return;
            }
            
            session.createMutationQuery(
                "DELETE FROM QuestStageOption WHERE stage.id = :stageId OR targetStageId = :targetStageId")
                .setParameter("stageId", stage.getId())
                .setParameter("targetStageId", stageId)
                .executeUpdate();
            
            session.remove(stage);
            
            transaction.commit();
            
            logger.info("Этап удален пользователем {}: {}", currentUser.getUsername(), stageId);
            
            sendJsonSuccess(response, "Этап успешно удален", null);
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при удалении этапа", e);
            sendJsonError(response, "Ошибка при удалении этапа: " + e.getMessage());
        }
    }
    

    private void publishQuest(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String questIdStr = request.getParameter("questId");
        
        if (questIdStr == null) {
            sendJsonError(response, "ID квеста не указан");
            return;
        }
        
        try {
            Long questId = Long.parseLong(questIdStr);
            Optional<Quest> questOpt = questService.getQuestById(questId);
            
            if (questOpt.isEmpty()) {
                sendJsonError(response, "Квест не найден");
                return;
            }
            
            Quest quest = questOpt.get();
            
            if (!quest.getAuthor().getId().equals(currentUser.getId())) {
                sendJsonError(response, "У вас нет прав для публикации этого квеста");
                return;
            }
            
            List<QuestStage> stages = loadStagesWithOptions(questId);
            quest.setStages(stages);
            
            QuestValidationService.ValidationResult validationResult = validationService.validateQuest(quest);
            
            if (!validationResult.isValid()) {
                sendJsonError(response, "Квест не прошел валидацию: " + validationResult.getErrorsAsString());
                return;
            }
            
            quest.setStatus(QuestStatus.PUBLISHED);
            questService.updateQuest(quest);
            
            logger.info("Квест опубликован пользователем {}: {} (ID: {})", 
                currentUser.getUsername(), quest.getTitle(), questId);
            
            sendJsonSuccess(response, "Квест успешно опубликован", null);
            
        } catch (NumberFormatException e) {
            sendJsonError(response, "Неверный формат ID квеста");
        } catch (Exception e) {
            logger.error("Ошибка при публикации квеста", e);
            sendJsonError(response, "Ошибка при публикации квеста: " + e.getMessage());
        }
    }
    

    private void unpublishQuest(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws ServletException, IOException {
        
        String questIdStr = request.getParameter("questId");
        
        if (questIdStr == null) {
            sendJsonError(response, "ID квеста не указан");
            return;
        }
        
        try {
            Long questId = Long.parseLong(questIdStr);
            Optional<Quest> questOpt = questService.getQuestById(questId);
            
            if (questOpt.isEmpty()) {
                sendJsonError(response, "Квест не найден");
                return;
            }
            
            Quest quest = questOpt.get();
            
            if (!quest.getAuthor().getId().equals(currentUser.getId())) {
                sendJsonError(response, "У вас нет прав для изменения статуса этого квеста");
                return;
            }
            
            quest.setStatus(QuestStatus.DRAFT);
            questService.updateQuest(quest);
            
            logger.info("Квест снят с публикации пользователем {}: {} (ID: {})", 
                currentUser.getUsername(), quest.getTitle(), questId);
            
            sendJsonSuccess(response, "Квест переведен в черновик", null);
            
        } catch (NumberFormatException e) {
            sendJsonError(response, "Неверный формат ID квеста");
        } catch (Exception e) {
            logger.error("Ошибка при снятии квеста с публикации", e);
            sendJsonError(response, "Ошибка при снятии квеста с публикации: " + e.getMessage());
        }
    }
    

    private void getStageData(HttpServletRequest request, HttpServletResponse response,
                             Long questId, User currentUser) throws IOException {
        String stageId = request.getParameter("stageId");
        
        if (stageId == null || stageId.trim().isEmpty()) {
            sendJsonError(response, "Stage ID не указан");
            return;
        }
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            QuestStage stage = session.createQuery(
                "SELECT s FROM QuestStage s WHERE s.stageId = :stageId AND s.quest.id = :questId",
                QuestStage.class)
                .setParameter("stageId", stageId)
                .setParameter("questId", questId)
                .uniqueResult();
            
            if (stage == null) {
                sendJsonError(response, "Этап не найден");
                return;
            }
            

            List<QuestStageOption> options = session.createQuery(
                "SELECT o FROM QuestStageOption o WHERE o.stage.id = :stageId ORDER BY o.optionKey",
                QuestStageOption.class)
                .setParameter("stageId", stage.getId())
                .list();
            

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            ObjectNode jsonResponse = objectMapper.createObjectNode();
            jsonResponse.put("success", true);
            jsonResponse.put("stageId", stage.getStageId());
            jsonResponse.put("title", stage.getTitle());
            
            if (!options.isEmpty()) {
                QuestStageOption opt1 = options.size() > 0 ? options.get(0) : null;
                QuestStageOption opt2 = options.size() > 1 ? options.get(1) : null;
                
                if (opt1 != null) {
                    jsonResponse.put("option1Text", opt1.getOptionText());
                    jsonResponse.put("option1ResultType", opt1.getResultType().name());
                    String target1 = opt1.getTargetStageId();
                    jsonResponse.put("option1Target", 
                        (target1 != null && !target1.isEmpty()) ? target1 : "");
                }
                if (opt2 != null) {
                    jsonResponse.put("option2Text", opt2.getOptionText());
                    jsonResponse.put("option2ResultType", opt2.getResultType().name());
                    String target2 = opt2.getTargetStageId();
                    jsonResponse.put("option2Target", 
                        (target2 != null && !target2.isEmpty()) ? target2 : "");
                }
            }
            
            response.getWriter().write(objectMapper.writeValueAsString(jsonResponse));
            
        } catch (Exception e) {
            logger.error("Ошибка при получении данных этапа", e);
            sendJsonError(response, "Ошибка при получении данных этапа: " + e.getMessage());
        }
    }
    

    private void validateQuestAjax(HttpServletRequest request, HttpServletResponse response, 
                                   Long questId, User currentUser) throws IOException {
        
        try {
            Optional<Quest> questOpt = questService.getQuestById(questId);
            
            if (questOpt.isEmpty()) {
                sendJsonError(response, "Квест не найден");
                return;
            }
            
            Quest quest = questOpt.get();
            
            if (!quest.getAuthor().getId().equals(currentUser.getId())) {
                sendJsonError(response, "У вас нет прав для валидации этого квеста");
                return;
            }
            
            
            List<QuestStage> stages = loadStagesWithOptions(questId);
            quest.setStages(stages);
            
            QuestValidationService.ValidationResult validationResult = validationService.validateQuest(quest);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            ObjectNode jsonResponse = objectMapper.createObjectNode();
            jsonResponse.put("valid", validationResult.isValid());
            jsonResponse.set("errors", objectMapper.valueToTree(validationResult.getErrors()));
            jsonResponse.put("statusInfo", validationService.getQuestStatusInfo(quest));
            
            response.getWriter().write(objectMapper.writeValueAsString(jsonResponse));
            
        } catch (Exception e) {
            logger.error("Ошибка при валидации квеста", e);
            sendJsonError(response, "Ошибка при валидации квеста: " + e.getMessage());
        }
    }
    

    private List<QuestStage> loadStagesWithOptions(Long questId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<QuestStage> stages = session.createQuery(
                "SELECT s FROM QuestStage s WHERE s.quest.id = :questId ORDER BY s.stageIndex", 
                QuestStage.class)
                .setParameter("questId", questId)
                .list();
            
            for (QuestStage stage : stages) {
                List<QuestStageOption> options = session.createQuery(
                    "SELECT o FROM QuestStageOption o WHERE o.stage.id = :stageId ORDER BY o.optionKey", 
                    QuestStageOption.class)
                    .setParameter("stageId", stage.getId())
                    .list();
                
                stage.setOptions(options);
            }
            
            return stages;
        }
    }
    

    private String generateStageId(Long questId, int stageIndex) {
        if (stageIndex == 1) {
            return "start";
        }
        return "stage_" + questId + "_" + stageIndex;
    }
    

    private void sendJsonSuccess(HttpServletResponse response, String message, String stageId) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        jsonResponse.put("success", true);
        jsonResponse.put("message", message);
        if (stageId != null) {
            jsonResponse.put("stageId", stageId);
        }
        
        response.getWriter().write(objectMapper.writeValueAsString(jsonResponse));
    }
    
    private void sendJsonError(HttpServletResponse response, String error) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        jsonResponse.put("success", false);
        jsonResponse.put("error", error);
        
        response.getWriter().write(objectMapper.writeValueAsString(jsonResponse));
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("QuestBuilderServlet уничтожен");
    }
}
