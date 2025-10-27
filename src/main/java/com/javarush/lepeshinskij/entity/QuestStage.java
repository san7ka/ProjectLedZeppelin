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
import java.util.Map;

@Entity
@jakarta.persistence.Table(name = "quest_stages", indexes = {
    @jakarta.persistence.Index(name = "idx_quest_stages_quest", columnList = "quest_id"),
    @jakarta.persistence.Index(name = "idx_quest_stages_stage_id", columnList = "stage_id"),
    @jakarta.persistence.Index(name = "idx_quest_stages_quest_stage", columnList = "quest_id, stage_id")
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "questStages")
public class QuestStage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_quest_stages_quest"))
    private Quest quest;
    
    @NotBlank(message = "Stage ID is required")
    @Size(max = 100, message = "Stage ID must not exceed 100 characters")
    @Column(name = "stage_id", nullable = false, length = 100)
    private String stageId;
    
    @NotBlank(message = "Stage title is required")
    @Size(max = 200, message = "Stage title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false, length = 20)
    private ResultType resultType = ResultType.NONE;
    
    @Column(name = "stage_index", nullable = false)
    private Integer stageIndex = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "stage", cascade = jakarta.persistence.CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @jakarta.persistence.OrderBy("optionKey ASC")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 16)
    private List<QuestStageOption> options = new ArrayList<>();
    
    @Transient
    private Map<String, String> optionsMap = new java.util.LinkedHashMap<>();
    
    public QuestStage() {}
    
    public QuestStage(String stageId, String title, String description) {
        this.stageId = stageId;
        this.title = title;
        this.description = description;
        this.resultType = ResultType.NONE;
    }
    
    public QuestStage(String stageId, String title, String description, ResultType resultType) {
        this.stageId = stageId;
        this.title = title;
        this.description = description;
        this.resultType = resultType;
    }
    
    public QuestStage(String stageId, String title, String description, ResultType resultType, Integer stageIndex) {
        this.stageId = stageId;
        this.title = title;
        this.description = description;
        this.resultType = resultType;
        this.stageIndex = stageIndex;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Quest getQuest() {
        return quest;
    }
    
    public void setQuest(Quest quest) {
        this.quest = quest;
    }
    
    public String getStageId() {
        return stageId;
    }
    
    public void setStageId(String stageId) {
        this.stageId = stageId;
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
    
    public ResultType getResultType() {
        return resultType;
    }
    
    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }
    
    public Integer getStageIndex() {
        return stageIndex;
    }
    
    public void setStageIndex(Integer stageIndex) {
        this.stageIndex = stageIndex;
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
    
    public List<QuestStageOption> getOptions() {
        return options;
    }
    
    public void setOptions(List<QuestStageOption> options) {
        this.options = options;
    }
    
    public Map<String, String> getOptionsMap() {
        return optionsMap;
    }
    
    public void setOptionsMap(Map<String, String> optionsMap) {
        this.optionsMap = optionsMap;
    }
    
    public void addOption(QuestStageOption option) {
        options.add(option);
        option.setStage(this);
    }
    
    public void removeOption(QuestStageOption option) {
        options.remove(option);
        option.setStage(null);
    }
    
    public QuestStageOption getOptionByKey(String optionKey) {
        return options.stream()
                .filter(option -> optionKey.equals(option.getOptionKey()))
                .findFirst()
                .orElse(null);
    }
    
    public boolean hasOption(String optionKey) {
        return options.stream()
                .anyMatch(option -> optionKey.equals(option.getOptionKey()));
    }
    
    public int getOptionCount() {
        return options.size();
    }
    
    public boolean isStartStage() {
        return "start".equals(stageId);
    }
    
    public boolean isWinStage() {
        return ResultType.WIN.equals(resultType);
    }
    
    public boolean isLoseStage() {
        return ResultType.LOSE.equals(resultType);
    }
    
    public boolean isEndStage() {
        return isWinStage() || isLoseStage();
    }
    
    public String getQuestTitle() {
        return quest != null ? quest.getTitle() : "Unknown Quest";
    }
    
    @Override
    public String toString() {
        return "QuestStage{" +
                "id=" + id +
                ", stageId='" + stageId + '\'' +
                ", title='" + title + '\'' +
                ", resultType=" + resultType +
                ", stageIndex=" + stageIndex +
                ", optionCount=" + getOptionCount() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestStage that = (QuestStage) o;
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
