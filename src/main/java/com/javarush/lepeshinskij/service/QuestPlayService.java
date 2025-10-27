package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.dao.*;
import com.javarush.lepeshinskij.entity.*;
import com.javarush.lepeshinskij.model.runtime.*;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

public class QuestPlayService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestPlayService.class);
    
    private final QuestDAO questDAO;
    private final UserQuestProgressDAO progressDAO;
    private final SessionHistoryDAO historyDAO;
    
    private final Map<Long, QuestGraph> questGraphCache = new HashMap<>();
    
    public QuestPlayService(SessionFactory sessionFactory) {
        this.questDAO = new QuestDAOImpl(sessionFactory);
        this.progressDAO = new UserQuestProgressDAOImpl(sessionFactory);
        this.historyDAO = new SessionHistoryDAOImpl(sessionFactory);
    }
    
    public QuestPlayService(QuestDAO questDAO, UserQuestProgressDAO progressDAO, SessionHistoryDAO historyDAO) {
        this.questDAO = questDAO;
        this.progressDAO = progressDAO;
        this.historyDAO = historyDAO;
    }
    
    public ActiveSession startQuest(Long questId, Long userId) {
        logger.info("Начало прохождения квеста ID: {} пользователем ID: {}", questId, userId);
        
        Quest quest = questDAO.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        
        if (!quest.isPublished()) {
            throw new IllegalStateException("Cannot start unpublished quest");
        }
        
        QuestGraph graph = getOrBuildQuestGraph(quest);
        
        if (!graph.hasStartNode()) {
            throw new IllegalStateException("Quest has no start node");
        }
        
        String startStageId = graph.getStartNode().getStageId();
        
        User user = new User();
        user.setId(userId);
        
        UserQuestProgress progress = new UserQuestProgress(user, quest, startStageId);
        progress = progressDAO.save(progress);
        
        QuestStage startStage = quest.getStageByStageId(startStageId);
        SessionHistory firstHistory = new SessionHistory(progress, startStage, 0);
        historyDAO.save(firstHistory);
        
        ActiveSession session = new ActiveSession(progress);
        
        logger.info("Квест начат: session ID: {}, стартовый узел: {}", progress.getId(), startStageId);
        
        return session;
    }
    
    public ActiveSession loadSession(Long sessionId) {
        logger.debug("Загрузка сессии ID: {}", sessionId);
        
        UserQuestProgress progress = progressDAO.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        
        if (progress.getIsCompleted()) {
            throw new IllegalStateException("Session is already completed");
        }
        
        ActiveSession session = new ActiveSession(progress);
        
        List<SessionHistory> history = historyDAO.findBySessionOrderByStepNumber(progress);
        Deque<String> visitedNodes = new LinkedList<>();
        
        for (int i = 0; i < history.size() - 1; i++) {
            visitedNodes.add(history.get(i).getNodeStageId());
        }
        
        session.restoreHistory(visitedNodes);
        
        return session;
    }
    
    public SessionState getCurrentState(Long sessionId) {
        logger.debug("Получение состояния сессии ID: {}", sessionId);
        
        UserQuestProgress progress = progressDAO.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        
        Quest quest = progress.getQuest();
        QuestGraph graph = getOrBuildQuestGraph(quest);
        
        QuestNodeRuntime currentNode = graph.getNode(progress.getCurrentStageId());
        if (currentNode == null) {
            throw new IllegalStateException("Current node not found in graph: " + progress.getCurrentStageId());
        }
        
        boolean canGoBack = historyDAO.canGoBack(progress);
        
        return new SessionState(
            sessionId,
            progress.getCurrentStageId(),
            currentNode.getTitle(),
            currentNode.getDescription(),
            currentNode.getResultType(),
            currentNode.getChoices(),
            progress.getIsCompleted(),
            canGoBack
        );
    }
    
    public SessionState makeChoice(Long sessionId, Long choiceId) {
        logger.info("Выбор ID: {} в сессии ID: {}", choiceId, sessionId);
        
        UserQuestProgress progress = progressDAO.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        
        if (progress.getIsCompleted()) {
            throw new IllegalStateException("Session is already completed");
        }
        
        Quest quest = progress.getQuest();
        QuestGraph graph = getOrBuildQuestGraph(quest);
        
        QuestNodeRuntime currentNode = graph.getNode(progress.getCurrentStageId());
        if (currentNode == null) {
            throw new IllegalStateException("Current node not found");
        }
        
        ChoiceRuntime choice = currentNode.getChoiceById(choiceId);
        if (choice == null) {
            throw new IllegalArgumentException("Choice not found: " + choiceId);
        }
        
        if (!choice.isTargetNodeValid()) {
            throw new IllegalStateException("Choice has invalid target node");
        }
        
        String targetStageId = choice.getTargetStageId();
        QuestNodeRuntime targetNode = choice.getTargetNode();
        
        progress.setCurrentStageId(targetStageId);
        
        if (targetNode.isEndNode()) {
            progress.setIsCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }
        
        progress = progressDAO.update(progress);
        
        QuestStage currentStage = quest.getStageByStageId(currentNode.getStageId());
        QuestStage targetStage = quest.getStageByStageId(targetStageId);
        QuestStageOption option = currentStage.getOptionByKey(choice.getOptionKey());
        
        int nextStepNumber = (int) historyDAO.countBySession(progress);
        SessionHistory historyEntry = new SessionHistory(progress, targetStage, option, nextStepNumber);
        historyDAO.save(historyEntry);
        
        logger.info("Переход: {} -> {} в сессии ID: {}", currentNode.getStageId(), targetStageId, sessionId);
        
        return getCurrentState(sessionId);
    }
    
    public SessionState goBack(Long sessionId) {
        logger.info("Откат в сессии ID: {}", sessionId);
        
        UserQuestProgress progress = progressDAO.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        
        if (progress.getIsCompleted()) {
            throw new IllegalStateException("Cannot go back in completed session");
        }
        
        if (!historyDAO.canGoBack(progress)) {
            throw new IllegalStateException("Cannot go back: no previous steps");
        }
        
        List<SessionHistory> history = historyDAO.findBySessionOrderByStepNumber(progress);
        
        if (history.size() < 2) {
            throw new IllegalStateException("Not enough history to go back");
        }
        
        historyDAO.deleteLastNSteps(progress, 1);
        
        SessionHistory previousStep = history.get(history.size() - 2);
        
        progress.setCurrentStageId(previousStep.getNodeStageId());
        progress.setIsCompleted(false);
        progress.setCompletedAt(null);
        progressDAO.update(progress);
        
        logger.info("Откат выполнен: текущий узел: {}", previousStep.getNodeStageId());
        
        return getCurrentState(sessionId);
    }
    
    public List<SessionHistory> getHistory(Long sessionId) {
        UserQuestProgress progress = progressDAO.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        
        return historyDAO.findBySessionOrderByStepNumber(progress);
    }
    
    public boolean canGoBack(Long sessionId) {
        UserQuestProgress progress = progressDAO.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        
        return historyDAO.canGoBack(progress);
    }
    
    public void abandonSession(Long sessionId) {
        logger.info("Отказ от прохождения сессии ID: {}", sessionId);
        
        progressDAO.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        
        progressDAO.deleteById(sessionId);
    }
    
    private QuestGraph getOrBuildQuestGraph(Quest quest) {
        Long questId = quest.getId();
        
        if (!questGraphCache.containsKey(questId)) {
            logger.debug("Построение графа для квеста ID: {}", questId);
            QuestGraph graph = QuestGraph.buildFromQuest(quest);
            questGraphCache.put(questId, graph);
        }
        
        return questGraphCache.get(questId);
    }
    
    public void clearGraphCache() {
        questGraphCache.clear();
    }
    
    public static class SessionState {
        private final Long sessionId;
        private final String currentStageId;
        private final String title;
        private final String description;
        private final QuestStage.ResultType resultType;
        private final List<ChoiceRuntime> choices;
        private final boolean isCompleted;
        private final boolean canGoBack;
        
        public SessionState(Long sessionId, String currentStageId, String title, String description,
                          QuestStage.ResultType resultType, List<ChoiceRuntime> choices,
                          boolean isCompleted, boolean canGoBack) {
            this.sessionId = sessionId;
            this.currentStageId = currentStageId;
            this.title = title;
            this.description = description;
            this.resultType = resultType;
            this.choices = choices;
            this.isCompleted = isCompleted;
            this.canGoBack = canGoBack;
        }
        
        public Long getSessionId() { return sessionId; }
        public String getCurrentStageId() { return currentStageId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public QuestStage.ResultType getResultType() { return resultType; }
        public List<ChoiceRuntime> getChoices() { return choices; }
        public boolean isCompleted() { return isCompleted; }
        public boolean canGoBack() { return canGoBack; }
        public boolean isWin() { return QuestStage.ResultType.WIN.equals(resultType); }
        public boolean isLose() { return QuestStage.ResultType.LOSE.equals(resultType); }
        public boolean isEndNode() { return isWin() || isLose(); }
    }
}
