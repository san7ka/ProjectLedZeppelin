package com.javarush.lepeshinskij.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "user_quest_progress", indexes = {
    @jakarta.persistence.Index(name = "idx_user_quest_progress_user", columnList = "user_id"),
    @jakarta.persistence.Index(name = "idx_user_quest_progress_quest", columnList = "quest_id"),
    @jakarta.persistence.Index(name = "idx_user_quest_progress_user_quest", columnList = "user_id, quest_id")
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "userQuestProgress")
public class UserQuestProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_user_quest_progress_user"))
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_user_quest_progress_quest"))
    private Quest quest;
    
    @NotBlank(message = "Current stage ID is required")
    @Size(max = 100, message = "Current stage ID must not exceed 100 characters")
    @Column(name = "current_stage_id", nullable = false, length = 100)
    private String currentStageId;
    
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;
    
    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public UserQuestProgress() {}
    
    public UserQuestProgress(User user, Quest quest, String currentStageId) {
        this.user = user;
        this.quest = quest;
        this.currentStageId = currentStageId;
        this.isCompleted = false;
    }
    
    public UserQuestProgress(User user, Quest quest, String currentStageId, Boolean isCompleted) {
        this.user = user;
        this.quest = quest;
        this.currentStageId = currentStageId;
        this.isCompleted = isCompleted;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Quest getQuest() {
        return quest;
    }
    
    public void setQuest(Quest quest) {
        this.quest = quest;
    }
    
    public String getCurrentStageId() {
        return currentStageId;
    }
    
    public void setCurrentStageId(String currentStageId) {
        this.currentStageId = currentStageId;
    }
    
    public Boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
        if (isCompleted && completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!isCompleted) {
            this.completedAt = null;
        }
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getUsername() {
        return user != null ? user.getUsername() : "Unknown User";
    }
    
    public String getQuestTitle() {
        return quest != null ? quest.getTitle() : "Unknown Quest";
    }
    
    public QuestStage getCurrentStage() {
        if (quest == null) {
            return null;
        }
        return quest.getStageByStageId(currentStageId);
    }
    
    public boolean isInProgress() {
        return !isCompleted;
    }
    
    public void complete() {
        setIsCompleted(true);
        setCompletedAt(LocalDateTime.now());
    }
    
    public void reset() {
        setIsCompleted(false);
        setCurrentStageId("start");
        setCompletedAt(null);
    }
    
    public void advanceToStage(String stageId) {
        setCurrentStageId(stageId);
        if (quest != null) {
            QuestStage stage = quest.getStageByStageId(stageId);
            if (stage != null && stage.isEndStage()) {
                complete();
            }
        }
    }
    
    public long getDurationInMinutes() {
        if (startedAt == null) {
            return 0;
        }
        LocalDateTime endTime = isCompleted && completedAt != null ? completedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, endTime).toMinutes();
    }
    
    public String getFormattedDuration() {
        long minutes = getDurationInMinutes();
        if (minutes < 60) {
            return minutes + " мин";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + " ч " + remainingMinutes + " мин";
        }
    }
    
    @Override
    public String toString() {
        return "UserQuestProgress{" +
                "id=" + id +
                ", username='" + getUsername() + '\'' +
                ", questTitle='" + getQuestTitle() + '\'' +
                ", currentStageId='" + currentStageId + '\'' +
                ", isCompleted=" + isCompleted +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserQuestProgress that = (UserQuestProgress) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
