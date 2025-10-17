package com.javarush.lepeshinskij.model;

import java.util.Map;

public class QuestStage {
    public enum ResultType {
        NONE,
        WIN,
        LOSE
    }
    
    private String title;
    private String description;
    private Map<String, String> options;
    private ResultType resultType;
    private int stageIndex;

    public QuestStage(String title, String description, Map<String, String> options) {
        this(title, description, options, ResultType.NONE, 0);
    }
    
    public QuestStage(String title, String description, Map<String, String> options, ResultType resultType) {
        this(title, description, options, resultType, 0);
    }
    
    public QuestStage(String title, String description, Map<String, String> options, ResultType resultType, int stageIndex) {
        this.title = title;
        this.description = description;
        this.options = options;
        this.resultType = resultType;
        this.stageIndex = stageIndex;
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

    public Map<String, String> getOptions() {
        return options;
    }
    
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
    
    public ResultType getResultType() {
        return resultType != null ? resultType : ResultType.NONE;
    }
    
    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }
    
    public int getStageIndex() {
        return stageIndex;
    }
    
    public void setStageIndex(int stageIndex) {
        this.stageIndex = stageIndex;
    }
    
    public int getOptionIndex(String optionId) {
        int index = 1;
        for (String key : options.keySet()) {
            if (key.equals(optionId)) {
                return index;
            }
            index++;
        }
        return 0;
    }
    
    public String getFormattedIndex(String optionId) {
        int optionIndex = getOptionIndex(optionId);
        if (optionIndex > 0) {
            return stageIndex + "." + optionIndex;
        }
        return String.valueOf(stageIndex);
    }
}