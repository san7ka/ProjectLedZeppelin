package com.javarush.lepeshinskij.model.runtime;

import com.javarush.lepeshinskij.entity.UserQuestProgress;

import java.time.LocalDateTime;
import java.util.Deque;
import java.util.LinkedList;

public class ActiveSession {
    
    private final Long sessionId;
    private final Long userId;
    private final Long questId;
    private final LocalDateTime startedAt;
    
    private String currentStageId;
    private boolean isCompleted;
    private LocalDateTime lastActivityAt;
    
    private final Deque<String> visitedStageIds;
    
    private int currentStepNumber;
    
    public ActiveSession(UserQuestProgress progress) {
        this.sessionId = progress.getId();
        this.userId = progress.getUser().getId();
        this.questId = progress.getQuest().getId();
        this.currentStageId = progress.getCurrentStageId();
        this.isCompleted = progress.getIsCompleted();
        this.startedAt = progress.getStartedAt();
        this.lastActivityAt = LocalDateTime.now();
        this.visitedStageIds = new LinkedList<>();
        this.currentStepNumber = 0;
    }
    
    public ActiveSession(Long sessionId, Long userId, Long questId, String startStageId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.questId = questId;
        this.currentStageId = startStageId;
        this.isCompleted = false;
        this.startedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.visitedStageIds = new LinkedList<>();
        this.currentStepNumber = 0;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public Long getQuestId() {
        return questId;
    }
    
    public String getCurrentStageId() {
        return currentStageId;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }
    
    public int getCurrentStepNumber() {
        return currentStepNumber;
    }
    
    public void moveToNode(String stageId) {
        visitedStageIds.push(currentStageId);
        currentStageId = stageId;
        currentStepNumber++;
        lastActivityAt = LocalDateTime.now();
    }
    
    public boolean goBack() {
        if (visitedStageIds.isEmpty()) {
            return false;
        }
        currentStageId = visitedStageIds.pop();
        currentStepNumber++;
        lastActivityAt = LocalDateTime.now();
        return true;
    }
    
    public void complete() {
        isCompleted = true;
        lastActivityAt = LocalDateTime.now();
    }
    
    public boolean canGoBack() {
        return !visitedStageIds.isEmpty();
    }
    
    public Deque<String> getVisitedStageIds() {
        return new LinkedList<>(visitedStageIds);
    }
    
    public void restoreHistory(Deque<String> history) {
        visitedStageIds.clear();
        visitedStageIds.addAll(history);
    }
    
    @Override
    public String toString() {
        return "ActiveSession{" +
                "sessionId=" + sessionId +
                ", userId=" + userId +
                ", questId=" + questId +
                ", currentStageId='" + currentStageId + '\'' +
                ", isCompleted=" + isCompleted +
                ", stepNumber=" + currentStepNumber +
                ", historyDepth=" + visitedStageIds.size() +
                '}';
    }
}
