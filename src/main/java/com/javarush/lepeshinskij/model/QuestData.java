package com.javarush.lepeshinskij.model;

import java.util.Map;

public class QuestData {
    private Map<String, QuestStageData> stages;
    private Map<String, String> transitions;
    
    public QuestData() {
    }
    
    public QuestData(Map<String, QuestStageData> stages, Map<String, String> transitions) {
        this.stages = stages;
        this.transitions = transitions;
    }
    
    public Map<String, QuestStageData> getStages() {
        return stages;
    }
    
    public void setStages(Map<String, QuestStageData> stages) {
        this.stages = stages;
    }
    
    public Map<String, String> getTransitions() {
        return transitions;
    }
    
    public void setTransitions(Map<String, String> transitions) {
        this.transitions = transitions;
    }
    
    public static class QuestStageData {
        private String title;
        private String description;
        private Map<String, String> options;
        private String resultType;
        private int stageIndex;
        
        public QuestStageData() {
        }
        
        public QuestStageData(String title, String description, Map<String, String> options, String resultType, int stageIndex) {
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
        
        public String getResultType() {
            return resultType;
        }
        
        public void setResultType(String resultType) {
            this.resultType = resultType;
        }
        
        public int getStageIndex() {
            return stageIndex;
        }
        
        public void setStageIndex(int stageIndex) {
            this.stageIndex = stageIndex;
        }
    }
}
