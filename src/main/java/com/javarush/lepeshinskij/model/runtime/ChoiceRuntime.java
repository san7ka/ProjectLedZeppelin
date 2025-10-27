package com.javarush.lepeshinskij.model.runtime;

public class ChoiceRuntime {
    
    private final Long id;
    private final String optionKey;
    private final String optionText;
    private final String targetStageId;
    private QuestNodeRuntime targetNode;
    
    public ChoiceRuntime(Long id, String optionKey, String optionText, String targetStageId) {
        this.id = id;
        this.optionKey = optionKey;
        this.optionText = optionText;
        this.targetStageId = targetStageId;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getOptionKey() {
        return optionKey;
    }
    
    public String getOptionText() {
        return optionText;
    }
    
    public String getTargetStageId() {
        return targetStageId;
    }
    
    public QuestNodeRuntime getTargetNode() {
        return targetNode;
    }
    
    public void setTargetNode(QuestNodeRuntime targetNode) {
        this.targetNode = targetNode;
    }
    
    public boolean isTargetNodeValid() {
        return targetNode != null;
    }
    
    @Override
    public String toString() {
        return "ChoiceRuntime{" +
                "id=" + id +
                ", optionKey='" + optionKey + '\'' +
                ", optionText='" + optionText + '\'' +
                ", targetStageId='" + targetStageId + '\'' +
                ", hasTargetNode=" + (targetNode != null) +
                '}';
    }
}
