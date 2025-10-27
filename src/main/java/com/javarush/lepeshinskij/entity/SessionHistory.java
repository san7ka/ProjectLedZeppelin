package com.javarush.lepeshinskij.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_history", indexes = {
    @Index(name = "idx_session_history_session", columnList = "session_id"),
    @Index(name = "idx_session_history_node", columnList = "node_id"),
    @Index(name = "idx_session_history_visited", columnList = "visited_at")
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "sessionHistory")
public class SessionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_session_history_session"))
    private UserQuestProgress session;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_session_history_node"))
    private QuestStage node;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "choice_id",
                foreignKey = @ForeignKey(name = "fk_session_history_choice"))
    private QuestStageOption choice;
    
    @CreationTimestamp
    @Column(name = "visited_at", nullable = false, updatable = false)
    private LocalDateTime visitedAt;
    
    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;
    
    public SessionHistory() {}
    
    public SessionHistory(UserQuestProgress session, QuestStage node, Integer stepNumber) {
        this.session = session;
        this.node = node;
        this.stepNumber = stepNumber;
    }
    
    public SessionHistory(UserQuestProgress session, QuestStage node, 
                         QuestStageOption choice, Integer stepNumber) {
        this.session = session;
        this.node = node;
        this.choice = choice;
        this.stepNumber = stepNumber;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public UserQuestProgress getSession() {
        return session;
    }
    
    public void setSession(UserQuestProgress session) {
        this.session = session;
    }
    
    public QuestStage getNode() {
        return node;
    }
    
    public void setNode(QuestStage node) {
        this.node = node;
    }
    
    public QuestStageOption getChoice() {
        return choice;
    }
    
    public void setChoice(QuestStageOption choice) {
        this.choice = choice;
    }
    
    public LocalDateTime getVisitedAt() {
        return visitedAt;
    }
    
    public void setVisitedAt(LocalDateTime visitedAt) {
        this.visitedAt = visitedAt;
    }
    
    public Integer getStepNumber() {
        return stepNumber;
    }
    
    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }
    
    public String getNodeStageId() {
        return node != null ? node.getStageId() : null;
    }
    
    public String getChoiceText() {
        return choice != null ? choice.getOptionText() : null;
    }
    
    public String getChoiceTargetStageId() {
        return choice != null ? choice.getTargetStageId() : null;
    }
    
    public boolean hasChoice() {
        return choice != null;
    }
    
    @Override
    public String toString() {
        return "SessionHistory{" +
                "id=" + id +
                ", nodeStageId='" + getNodeStageId() + '\'' +
                ", choiceText='" + getChoiceText() + '\'' +
                ", stepNumber=" + stepNumber +
                ", visitedAt=" + visitedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionHistory that = (SessionHistory) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
