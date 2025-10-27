package com.javarush.lepeshinskij.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "quest_stage_options", indexes = {
    @jakarta.persistence.Index(name = "idx_quest_stage_options_stage", columnList = "stage_id"),
    @jakarta.persistence.Index(name = "idx_quest_stage_options_key", columnList = "option_key")
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "questStageOptions")
public class QuestStageOption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_quest_stage_options_stage"))
    private QuestStage stage;
    
    @NotBlank(message = "Option key is required")
    @Size(max = 100, message = "Option key must not exceed 100 characters")
    @Column(name = "option_key", nullable = false, length = 100)
    private String optionKey;
    
    @NotBlank(message = "Option text is required")
    @Size(max = 500, message = "Option text must not exceed 500 characters")
    @Column(name = "option_text", nullable = false, length = 500)
    private String optionText;
    
    @Size(max = 100, message = "Target stage ID must not exceed 100 characters")
    @Column(name = "target_stage_id", nullable = true, length = 100)
    private String targetStageId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false, length = 20)
    private ResultType resultType = ResultType.NONE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public QuestStageOption() {}
    
    public QuestStageOption(String optionKey, String optionText, String targetStageId) {
        this.optionKey = optionKey;
        this.optionText = optionText;
        this.targetStageId = targetStageId;
        this.resultType = ResultType.NONE;
    }
    
    public QuestStageOption(QuestStage stage, String optionKey, String optionText, String targetStageId) {
        this.stage = stage;
        this.optionKey = optionKey;
        this.optionText = optionText;
        this.targetStageId = targetStageId;
        this.resultType = ResultType.NONE;
    }
    
    public QuestStageOption(QuestStage stage, String optionKey, String optionText, String targetStageId, ResultType resultType) {
        this.stage = stage;
        this.optionKey = optionKey;
        this.optionText = optionText;
        this.targetStageId = targetStageId;
        this.resultType = resultType;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public QuestStage getStage() {
        return stage;
    }
    
    public void setStage(QuestStage stage) {
        this.stage = stage;
    }
    
    public String getOptionKey() {
        return optionKey;
    }
    
    public void setOptionKey(String optionKey) {
        this.optionKey = optionKey;
    }
    
    public String getOptionText() {
        return optionText;
    }
    
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
    
    public String getTargetStageId() {
        return targetStageId;
    }
    
    public void setTargetStageId(String targetStageId) {
        this.targetStageId = targetStageId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public ResultType getResultType() {
        return resultType;
    }
    
    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }
    
    public String getStageId() {
        return stage != null ? stage.getStageId() : null;
    }
    
    public String getQuestTitle() {
        return stage != null && stage.getQuest() != null ? stage.getQuest().getTitle() : "Unknown Quest";
    }
    
    public String getStageTitle() {
        return stage != null ? stage.getTitle() : "Unknown Stage";
    }
    
    public boolean isTargetStageValid() {
        if (resultType == ResultType.WIN || resultType == ResultType.LOSE) {
            return true;
        }
        if (stage == null || stage.getQuest() == null) {
            return false;
        }
        return targetStageId != null && !targetStageId.isEmpty() && stage.getQuest().hasStage(targetStageId);
    }
    
    public QuestStage getTargetStage() {
        if (resultType == ResultType.WIN || resultType == ResultType.LOSE) {
            return null;
        }
        if (stage == null || stage.getQuest() == null) {
            return null;
        }
        return stage.getQuest().getStageByStageId(targetStageId);
    }
    
    public boolean isWinOption() {
        return ResultType.WIN.equals(resultType);
    }
    
    public boolean isLoseOption() {
        return ResultType.LOSE.equals(resultType);
    }
    
    public boolean isEndOption() {
        return isWinOption() || isLoseOption();
    }
    
    @Override
    public String toString() {
        return "QuestStageOption{" +
                "id=" + id +
                ", optionKey='" + optionKey + '\'' +
                ", optionText='" + optionText + '\'' +
                ", targetStageId='" + targetStageId + '\'' +
                ", resultType=" + resultType +
                ", stageId=" + getStageId() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestStageOption that = (QuestStageOption) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    public enum ResultType {
        NONE("NONE"),
        WIN("WIN"),
        LOSE("LOSE");
        
        private final String value;
        
        ResultType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
}
