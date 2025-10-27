package com.javarush.lepeshinskij.model.runtime;

import com.javarush.lepeshinskij.entity.QuestStage;

import java.util.ArrayList;
import java.util.List;

public class QuestNodeRuntime {
    
    private final Long id;
    private final String stageId;
    private final String title;
    private final String description;
    private final QuestStage.ResultType resultType;
    private final List<ChoiceRuntime> choices;
    
    public QuestNodeRuntime(Long id, String stageId, String title, String description, 
                           QuestStage.ResultType resultType) {
        this.id = id;
        this.stageId = stageId;
        this.title = title;
        this.description = description;
        this.resultType = resultType;
        this.choices = new ArrayList<>();
    }
    
    public Long getId() {
        return id;
    }
    
    public String getStageId() {
        return stageId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public QuestStage.ResultType getResultType() {
        return resultType;
    }
    
    public List<ChoiceRuntime> getChoices() {
        return choices;
    }
    
    public void addChoice(ChoiceRuntime choice) {
        choices.add(choice);
    }
    
    public ChoiceRuntime getChoiceByKey(String optionKey) {
        return choices.stream()
                .filter(choice -> optionKey.equals(choice.getOptionKey()))
                .findFirst()
                .orElse(null);
    }
    
    public ChoiceRuntime getChoiceById(Long choiceId) {
        return choices.stream()
                .filter(choice -> choiceId.equals(choice.getId()))
                .findFirst()
                .orElse(null);
    }
    
    public boolean hasChoices() {
        return !choices.isEmpty();
    }
    
    public boolean isStartNode() {
        return "start".equals(stageId);
    }
    
    public boolean isWinNode() {
        return QuestStage.ResultType.WIN.equals(resultType);
    }
    
    public boolean isLoseNode() {
        return QuestStage.ResultType.LOSE.equals(resultType);
    }
    
    public boolean isEndNode() {
        return isWinNode() || isLoseNode();
    }
    
    @Override
    public String toString() {
        return "QuestNodeRuntime{" +
                "id=" + id +
                ", stageId='" + stageId + '\'' +
                ", title='" + title + '\'' +
                ", resultType=" + resultType +
                ", choicesCount=" + choices.size() +
                '}';
    }
}
