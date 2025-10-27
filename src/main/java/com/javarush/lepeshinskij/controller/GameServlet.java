package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import com.javarush.lepeshinskij.model.PlayerStats;
import com.javarush.lepeshinskij.service.GameService;
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
import java.util.Optional;

public class GameServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(GameServlet.class);
    
    private GameService gameService;
    private QuestService questService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
            gameService = new GameService(sessionFactory);
            questService = new QuestService(sessionFactory);
            logger.info("GameServlet инициализирован успешно");
        } catch (Exception e) {
            logger.error("Ошибка инициализации GameServlet", e);
            throw new ServletException("Не удалось инициализировать GameServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String questIdStr = request.getParameter("questId");
            String progressIdStr = request.getParameter("progressId");
            
            if (progressIdStr != null && !progressIdStr.isEmpty()) {
                loadExistingGame(request, response, Long.parseLong(progressIdStr));
                return;
            }
            
            if (questIdStr != null && !questIdStr.isEmpty()) {
                startNewGame(request, response, Long.parseLong(questIdStr));
                return;
            }
            
            showQuestSelection(request, response);
            
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID квеста или прогресса", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID");
        } catch (Exception e) {
            logger.error("Ошибка в doGet", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String progressIdStr = request.getParameter("progressId");
            String choice = request.getParameter("choice");
            String anonymousGame = request.getParameter("anonymousGame");
            
            if (choice == null || choice.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Выбор не указан");
                return;
            }
            
            if ("true".equals(anonymousGame)) {
                processAnonymousChoice(request, response, choice);
                return;
            }
            
            if (progressIdStr == null || progressIdStr.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID прогресса не указан");
                return;
            }
            
            Long progressId = Long.parseLong(progressIdStr);
            processChoice(request, response, progressId, choice);
            
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID прогресса", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID прогресса");
        } catch (Exception e) {
            logger.error("Ошибка в doPost", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }
    
    private void showQuestSelection(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            List<Quest> availableQuests = questService.getAllQuests();
            request.setAttribute("quests", availableQuests);
        } catch (Exception e) {
            logger.error("Ошибка при загрузке квестов", e);
            request.setAttribute("quests", null);
        }
        
        request.setAttribute("user", SessionUtils.getCurrentUser(request));
        
        request.getRequestDispatcher("/WEB-INF/jsp/quest-selection-simple.jsp").forward(request, response);
    }
    
    private void startNewGame(HttpServletRequest request, HttpServletResponse response, Long questId) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtils.getCurrentUser(request);
        if (currentUser == null) {
            startAnonymousGame(request, response, questId);
            return;
        }
        
        try {
            Optional<Quest> questOpt = questService.getQuestById(questId);
            if (questOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Квест не найден");
                return;
            }
            
            Quest quest = questOpt.get();
            
            if (gameService.isUserPlayingQuest(currentUser, quest)) {
                UserQuestProgress existingProgress = gameService.getUserQuestProgress(currentUser, quest);
                if (existingProgress != null && existingProgress.getId() != null) {
                    response.sendRedirect(request.getContextPath() + "/game?progressId=" + existingProgress.getId());
                    return;
                }
                logger.warn("Пользователь {} имеет прогресс без ID для квеста {}, создаем новый", 
                    currentUser.getUsername(), quest.getTitle());
            }
            
            UserQuestProgress progress = gameService.startQuest(currentUser, quest);
            
            logger.info("Пользователь {} начал игру в квест {} (ID: {})", 
                currentUser.getUsername(), quest.getTitle(), progress.getId());
            
            response.sendRedirect(request.getContextPath() + "/game?progressId=" + progress.getId());
            
        } catch (Exception e) {
            logger.error("Ошибка при начале игры", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при начале игры");
        }
    }
    
    private void startAnonymousGame(HttpServletRequest request, HttpServletResponse response, Long questId) 
            throws ServletException, IOException {
        
        try {
            Optional<Quest> questOpt = questService.getQuestById(questId);
            if (questOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Квест не найден");
                return;
            }
            
            Quest quest = questOpt.get();
            
            String playerName = request.getParameter("playerName");
            if (playerName == null || playerName.trim().isEmpty()) {
                request.setAttribute("questId", questId);
                request.getRequestDispatcher("/WEB-INF/jsp/game-anonymous.jsp").forward(request, response);
                return;
            }
            
            SessionUtils.getOrCreatePlayerStats(request, playerName);
            
            request.getSession().setAttribute("playerName", playerName);
            request.getSession().setAttribute("questId", questId);
            
            logger.info("Анонимный пользователь {} начал игру в квест {}", playerName, quest.getTitle());
            
            QuestStage firstStage = gameService.getFirstStage(quest);
            if (firstStage == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка загрузки квеста");
                return;
            }
            
            Quest questCopy = new Quest(quest.getId(), quest.getTitle(), quest.getDescription());
            
            List<String> availableOptions = firstStage.getOptions().stream()
                    .map(option -> option.getOptionKey())
                    .collect(java.util.stream.Collectors.toList());
            
            request.setAttribute("quest", questCopy);
            request.setAttribute("stage", firstStage);
            request.setAttribute("playerName", playerName);
            request.setAttribute("anonymousGame", true);
            request.setAttribute("availableOptions", availableOptions);
            
            request.getRequestDispatcher("/WEB-INF/jsp/game.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Ошибка при начале анонимной игры", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при начале игры");
        }
    }
    
    private void loadExistingGame(HttpServletRequest request, HttpServletResponse response, Long progressId) 
            throws ServletException, IOException {
        
        try {
            UserQuestProgress progress = gameService.getProgressById(progressId);
            if (progress == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Прогресс игры не найден");
                return;
            }
            
            User currentUser = SessionUtils.getCurrentUser(request);
            
            if (currentUser != null && !progress.getUser().getId().equals(currentUser.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет доступа к этой игре");
                return;
            }
            
            if (progress.getIsCompleted()) {
                response.sendRedirect(request.getContextPath() + "/result?progressId=" + progressId);
                return;
            }
            
            QuestStage currentStage = gameService.getCurrentStage(progress);
            if (currentStage == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка загрузки этапа");
                return;
            }
            
            List<String> availableOptions = gameService.getAvailableChoices(progress);
            
            request.setAttribute("progress", progress);
            request.setAttribute("quest", progress.getQuest());
            request.setAttribute("stage", currentStage);
            request.setAttribute("availableOptions", availableOptions);
            request.setAttribute("user", currentUser);
            
            request.getRequestDispatcher("/WEB-INF/jsp/game.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Ошибка при загрузке игры", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при загрузке игры");
        }
    }
    
    private void processChoice(HttpServletRequest request, HttpServletResponse response, Long progressId, String choice) 
            throws ServletException, IOException {
        
        try {
            UserQuestProgress progress = gameService.getProgressById(progressId);
            if (progress == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Прогресс игры не найден");
                return;
            }
            
            User currentUser = SessionUtils.getCurrentUser(request);
            
            if (currentUser != null && !progress.getUser().getId().equals(currentUser.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "У вас нет доступа к этой игре");
                return;
            }
            
            boolean gameCompleted = gameService.makeChoice(progress, choice);
            
            if (gameCompleted) {
                logger.info("Игра завершена для пользователя {} в квесте {}", 
                    progress.getUser().getUsername(), progress.getQuest().getTitle());
                response.sendRedirect(request.getContextPath() + "/result?progressId=" + progressId);
            } else {
                response.sendRedirect(request.getContextPath() + "/game?progressId=" + progressId);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке выбора", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при обработке выбора");
        }
    }
    
    private void processAnonymousChoice(HttpServletRequest request, HttpServletResponse response, String choice) 
            throws ServletException, IOException {
        
        try {
            String questIdStr = request.getParameter("questId");
            String currentStageId = request.getParameter("currentStageId");
            
            if (questIdStr == null || currentStageId == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Недостаточно данных для анонимной игры");
                return;
            }
            
            Long questId = Long.parseLong(questIdStr);
            Optional<Quest> questOpt = questService.getQuestById(questId);
            if (questOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Квест не найден");
                return;
            }
            
            Quest quest = questOpt.get();
            
            Optional<QuestStage> currentStageOpt = questService.getStage(quest, currentStageId);
            if (currentStageOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Текущий этап не найден");
                return;
            }
            
            QuestStage currentStage = currentStageOpt.get();
            
            var option = currentStage.getOptionByKey(choice);
            if (option == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неверный выбор");
                return;
            }
            
            String playerName = (String) request.getSession().getAttribute("playerName");
            Quest questCopy = new Quest(quest.getId(), quest.getTitle(), quest.getDescription());
            
            if (option.isEndOption()) {
                PlayerStats playerStats = SessionUtils.getOrCreatePlayerStats(request, playerName);
                
                boolean isWin = option.isWinOption();
                if (isWin) {
                    playerStats.registerWin();
                    logger.info("Анонимный игрок {} выиграл квест {}", playerName, quest.getTitle());
                } else {
                    playerStats.registerLoss();
                    logger.info("Анонимный игрок {} проиграл квест {}", playerName, quest.getTitle());
                }
                
                request.getSession().setAttribute("playerStats", playerStats);
                
                QuestStage finalStage = new QuestStage();
                finalStage.setStageId("final_" + option.getResultType().getValue().toLowerCase());
                finalStage.setTitle(isWin ? "Победа!" : "Поражение");
                finalStage.setDescription(option.getOptionText());
                finalStage.setResultType(isWin ? QuestStage.ResultType.WIN : QuestStage.ResultType.LOSE);
                
                request.getSession().setAttribute("isWin", isWin);
                request.getSession().setAttribute("finalStage", finalStage);
                request.getSession().setAttribute("lastQuest", questCopy);
                request.getSession().setAttribute("isAnonymous", true);
                
                logger.info("Анонимная игра завершена через опцию с результатом {}", option.getResultType());
                
                response.sendRedirect(request.getContextPath() + "/result");
                return;
            }
            
            String nextStageId = option.getTargetStageId();
            Optional<QuestStage> nextStageOpt = questService.getStage(quest, nextStageId);
            if (nextStageOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Следующий этап не найден");
                return;
            }
            
            QuestStage nextStage = nextStageOpt.get();
            
            List<String> availableOptions = nextStage.getOptions().stream()
                    .map(opt -> opt.getOptionKey())
                    .collect(java.util.stream.Collectors.toList());
            
            request.setAttribute("quest", questCopy);
            request.setAttribute("stage", nextStage);
            request.setAttribute("availableOptions", availableOptions);
            request.setAttribute("anonymousGame", true);
            
            if (playerName != null) {
                request.setAttribute("playerName", playerName);
            }
            
            logger.info("Анонимный игрок перешел на этап {} в квесте {}", nextStageId, quest.getTitle());
            
            if (nextStage.isEndStage()) {
                PlayerStats playerStats = SessionUtils.getOrCreatePlayerStats(request, playerName);
                
                boolean isWin = nextStage.isWinStage();
                if (isWin) {
                    playerStats.registerWin();
                    logger.info("Анонимный игрок {} выиграл квест {}", playerName, quest.getTitle());
                } else {
                    playerStats.registerLoss();
                    logger.info("Анонимный игрок {} проиграл квест {}", playerName, quest.getTitle());
                }
                
                request.getSession().setAttribute("playerStats", playerStats);
                
                request.getSession().setAttribute("isWin", isWin);
                request.getSession().setAttribute("finalStage", nextStage);
                request.getSession().setAttribute("lastQuest", questCopy);
                request.getSession().setAttribute("isAnonymous", true);
                
                response.sendRedirect(request.getContextPath() + "/result");
            } else {
                request.getRequestDispatcher("/WEB-INF/jsp/game.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке анонимного выбора", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при обработке выбора");
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("GameServlet уничтожен");
    }
}
