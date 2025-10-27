package com.javarush.lepeshinskij.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "quests", indexes = {
    @jakarta.persistence.Index(name = "idx_quests_author", columnList = "author_id"),
    @jakarta.persistence.Index(name = "idx_quests_title", columnList = "title")
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "quests")
public class Quest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Quest title is required")
    @Size(max = 200, message = "Quest title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "start_stage_id", length = 100)
    private String startStageId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuestStatus status = QuestStatus.DRAFT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_quests_author"))
    private User author;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "quest", cascade = jakarta.persistence.CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @jakarta.persistence.OrderBy("stageIndex ASC")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 16)
    private List<QuestStage> stages = new ArrayList<>();
    
    @OneToMany(mappedBy = "quest", cascade = jakarta.persistence.CascadeType.ALL, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 16)
    private List<UserQuestProgress> userProgress = new ArrayList<>();
    
    public Quest() {}
    
    public Quest(String title, String description, User author) {
        this.title = title;
        this.description = description;
        this.author = author;
    }
    
    public Quest(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStartStageId() {
        return startStageId;
    }
    
    public void setStartStageId(String startStageId) {
        this.startStageId = startStageId;
    }
    
    public QuestStatus getStatus() {
        return status;
    }
    
    public void setStatus(QuestStatus status) {
        this.status = status;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public void setAuthor(User author) {
        this.author = author;
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
    
    public List<QuestStage> getStages() {
        return stages;
    }
    
    public void setStages(List<QuestStage> stages) {
        this.stages = stages;
    }
    
    public List<UserQuestProgress> getUserProgress() {
        return userProgress;
    }
    
    public void setUserProgress(List<UserQuestProgress> userProgress) {
        this.userProgress = userProgress;
    }
    
    public void addStage(QuestStage stage) {
        stages.add(stage);
        stage.setQuest(this);
    }
    
    public void removeStage(QuestStage stage) {
        stages.remove(stage);
        stage.setQuest(null);
    }
    
    public QuestStage getStageByStageId(String stageId) {
        return stages.stream()
                .filter(stage -> stageId.equals(stage.getStageId()))
                .findFirst()
                .orElse(null);
    }
    
    public QuestStage getStartStage() {
        return stages.stream()
                .filter(stage -> "start".equals(stage.getStageId()))
                .findFirst()
                .orElse(null);
    }
    
    public boolean hasStage(String stageId) {
        return stages.stream()
                .anyMatch(stage -> stageId.equals(stage.getStageId()));
    }
    
    public int getStageCount() {
        return stages.size();
    }
    
    public String getAuthorName() {
        return author != null ? author.getUsername() : "Unknown";
    }

    public boolean isPlayable() {
        return !stages.isEmpty() && getStartStage() != null && QuestStatus.PUBLISHED.equals(status);
    }
    
    public boolean isDraft() {
        return QuestStatus.DRAFT.equals(status);
    }
    
    public boolean isPublished() {
        return QuestStatus.PUBLISHED.equals(status);
    }
    
    @Override
    public String toString() {
        return "Quest{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", author=" + (author != null ? author.getUsername() : "null") +
                ", createdAt=" + createdAt +
                ", stageCount=" + getStageCount() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quest quest = (Quest) o;
        return id != null && id.equals(quest.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
