package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.model.Quest;
import com.javarush.lepeshinskij.model.QuestStage;
import com.javarush.lepeshinskij.service.QuestService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QuestEditorServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(QuestEditorServlet.class.getName());
    private transient QuestService questService;

    @Override
    public void init() throws ServletException {
        questService = QuestService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Quest quest = questService.getQuest();
            String editStageId = req.getParameter("edit");
            
            boolean isAjaxRequest = "XMLHttpRequest".equals(req.getHeader("X-Requested-With")) ||
                                 (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));
            
            if (editStageId != null && !editStageId.isEmpty() && isAjaxRequest) {
                QuestStage stage = quest.getStage(editStageId);
                if (stage != null) {
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> response = new HashMap<>();
                    response.put("title", stage.getTitle());
                    response.put("description", stage.getDescription());
                    response.put("stageIndex", stage.getStageIndex());
                    response.put("children", stage.getOptions());
                    response.put("parents", quest.getParentsOf(editStageId));
                    
                    logger.info("Sending stage data: " + mapper.writeValueAsString(response));
                    mapper.writeValue(resp.getWriter(), response);
                    return;
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Stage not found");
                    return;
                }
            }
            
            req.setAttribute("quest", quest);
            req.setAttribute("stages", quest.getStages());
            req.setAttribute("currentStageId", editStageId);
            req.setAttribute("error", req.getSession().getAttribute("error"));
            req.setAttribute("message", req.getSession().getAttribute("message"));
            req.setAttribute("errorStageId", req.getSession().getAttribute("errorStageId"));
            req.getSession().removeAttribute("error");
            req.getSession().removeAttribute("message");
            req.getSession().removeAttribute("errorStageId");
            req.getRequestDispatcher("/WEB-INF/jsp/editor.jsp").forward(req, resp);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading editor page", e);
            req.setAttribute("error", "Error loading editor: " + e.getMessage());
            req.setAttribute("quest", questService.getQuest());
            req.setAttribute("stages", questService.getQuest().getStages());
            req.getRequestDispatcher("/WEB-INF/jsp/editor.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        
        switch (action) {
            case "add" -> handleAddStage(req, resp);
            case "update" -> handleUpdateStage(req, resp);
            case "delete" -> handleDeleteStage(req, resp);
            case "setStart" -> handleSetStartStage(req, resp);
            case "reorder" -> handleReorderStages(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }

    private void handleAddStage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String stageId = generateUniqueStageId();
            String title = validateAndGetParameter(req, "title", "Title is required");
            String description = validateAndGetParameter(req, "description", "Description is required");
            Map<String, String> options = parseOptions(req);
            QuestStage.ResultType resultType = parseResultType(req);
            
            QuestStage stage = new QuestStage(title, description, options, resultType);
            questService.addStage(stageId, stage);
            req.getSession().setAttribute("message", "Stage added successfully");
            resp.sendRedirect("editor?edit=" + stageId);
        } catch (IllegalArgumentException e) {
            handleError(req, resp, e.getMessage(), e, "editor");
        } catch (Exception e) {
            handleError(req, resp, "Failed to add stage: " + e.getMessage(), e, "editor");
        }
    }
    
    private String generateUniqueStageId() {
        String baseId = "stage_" + UUID.randomUUID().toString().substring(0, 8);
        Quest quest = questService.getQuest();
        if (quest.getStage(baseId) != null) {
            return generateUniqueStageId();
        }
        return baseId;
    }

    private void handleUpdateStage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String stageId = validateAndGetParameter(req, "stageId", "Stage ID is required");
            String title = validateAndGetParameter(req, "title", "Title is required");
            String description = validateAndGetParameter(req, "description", "Description is required");

            QuestStage existingStage = questService.getQuest().getStage(stageId);
            if (existingStage == null) {
                throw new IllegalArgumentException("Stage not found: " + stageId);
            }

            QuestStage.ResultType resultType = parseResultType(req);
            if (resultType == QuestStage.ResultType.NONE && existingStage.getResultType() != QuestStage.ResultType.NONE) {
                resultType = existingStage.getResultType();
            }

            Map<String, String> currentOptions = new HashMap<>(existingStage.getOptions());
            QuestStage stage = new QuestStage(title, description, currentOptions, resultType);
            questService.updateStage(stageId, stage);
            
            String[] parentIds = req.getParameterValues("parentIds");
            String[] parentButtonTexts = req.getParameterValues("parentButtonTexts");
            
            Map<String, String> currentParents = questService.getQuest().getParentsOf(stageId);
            Map<String, String> newParents = new HashMap<>();
            
            if (parentIds != null && parentButtonTexts != null) {
                for (int i = 0; i < parentIds.length; i++) {
                    String parentId = parentIds[i].trim();
                    String buttonText = i < parentButtonTexts.length ? parentButtonTexts[i].trim() : "";
                    if (!parentId.isEmpty() && !buttonText.isEmpty()) {
                        newParents.put(parentId, buttonText);
                    }
                }
            }
            
            for (String oldParentId : currentParents.keySet()) {
                if (!newParents.containsKey(oldParentId)) {
                    questService.getQuest().removeChildFromParent(oldParentId, stageId);
                }
            }
            
            for (Map.Entry<String, String> entry : newParents.entrySet()) {
                questService.getQuest().addChildToParent(entry.getKey(), stageId, entry.getValue());
            }
            
            Map<String, String> children = parseOptions(req);
            QuestStage updatedStage = new QuestStage(title, description, children, resultType);
            questService.updateStage(stageId, updatedStage);

            req.getSession().setAttribute("message", "Stage updated successfully");
            resp.sendRedirect("editor?edit=" + stageId);
        } catch (IllegalArgumentException e) {
            handleError(req, resp, e.getMessage(), e, "editor?edit=" + req.getParameter("stageId"));
        } catch (Exception e) {
            handleError(req, resp, "Failed to update stage: " + e.getMessage(), e,
                       "editor?edit=" + req.getParameter("stageId"));
        }
    }

    private void handleDeleteStage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String stageId = validateAndGetParameter(req, "stageId", "Stage ID is required");

            // Check if stage has child stages (options pointing to other stages)
            if (stageHasChildren(stageId)) {
                throw new IllegalStateException("Cannot delete stage: it has child stages. Remove all options from this stage first.");
            }

            // Check if stage is being used in transitions
            if (isStageInUse(stageId)) {
                throw new IllegalStateException("Cannot delete stage: it is being used by other stages");
            }

            questService.deleteStage(stageId);
            req.getSession().setAttribute("message", "Stage deleted successfully");
            resp.sendRedirect("editor");
        } catch (IllegalArgumentException e) {
            // Handle validation errors (e.g., trying to delete critical stages)
            handleError(req, resp, e.getMessage(), e, "editor");
        } catch (IllegalStateException e) {
            // For dependency-related errors, mark the offending stage to highlight in UI
            String stageId = req.getParameter("stageId");
            if (stageId != null) {
                req.getSession().setAttribute("errorStageId", stageId);
            }
            handleError(req, resp, "Failed to delete stage: " + e.getMessage(), e, "editor");
        } catch (Exception e) {
            handleError(req, resp, "Failed to delete stage: " + e.getMessage(), e, "editor");
        }
    }

    private void handleSetStartStage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String stageId = validateAndGetParameter(req, "stageId", "Stage ID is required");
            questService.getQuest().setStartStage(stageId);
            req.getSession().setAttribute("message", "Start stage set successfully");
            resp.sendRedirect("editor?edit=" + stageId);
        } catch (Exception e) {
            handleError(req, resp, "Failed to set start stage: " + e.getMessage(), e, "editor");
        }
    }

    private Map<String, String> parseOptions(HttpServletRequest req) {
        Map<String, String> options = new HashMap<>();
        String[] optionKeys = req.getParameterValues("optionKeys");
        String[] optionValues = req.getParameterValues("optionValues");

        if (optionKeys != null && optionValues != null) {
            for (int i = 0; i < optionKeys.length; i++) {
                String key = optionKeys[i] != null ? optionKeys[i].trim() : "";
                String value = optionValues[i] != null ? optionValues[i].trim() : "";
                if (!key.isEmpty() && !value.isEmpty()) {
                    options.put(key, value);
                }
            }
        }
        return options;
    }
    
    private QuestStage.ResultType parseResultType(HttpServletRequest req) {
        String resultTypeParam = req.getParameter("resultType");
        if (resultTypeParam != null && !resultTypeParam.trim().isEmpty()) {
            try {
                return QuestStage.ResultType.valueOf(resultTypeParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid result type, default to NONE
                logger.warning("Invalid result type: " + resultTypeParam);
            }
        }
        return QuestStage.ResultType.NONE;
    }

    private boolean isStageInUse(String stageId) {
        Quest quest = questService.getQuest();
        return quest.getStages().values().stream()
            .flatMap(stage -> stage.getOptions().values().stream())
            .anyMatch(target -> target.equals(stageId));
    }

    private boolean stageHasChildren(String stageId) {
        Quest quest = questService.getQuest();
        QuestStage stage = quest.getStage(stageId);
        return stage != null && !stage.getOptions().isEmpty();
    }

    private String validateAndGetParameter(HttpServletRequest req, String paramName, String errorMessage) {
        String value = req.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value.trim();
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp, String message, Throwable e, String redirectPath) throws IOException {
        logger.log(Level.SEVERE, message, e);
        req.getSession().setAttribute("error", message);
        resp.sendRedirect(redirectPath != null ? redirectPath : "editor");
    }
    
    private void handleReorderStages(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String[] stageOrder = req.getParameterValues("stageOrder[]");
            if (stageOrder == null || stageOrder.length == 0) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No stage order provided");
                return;
            }
            
            questService.reorderStages(Arrays.asList(stageOrder));
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\":true}");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reordering stages", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\":false,\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
