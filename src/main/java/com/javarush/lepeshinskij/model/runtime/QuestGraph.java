package com.javarush.lepeshinskij.model.runtime;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.QuestStageOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class QuestGraph {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestGraph.class);
    
    private final Long questId;
    private final String questTitle;
    private final Map<String, QuestNodeRuntime> nodes;
    private QuestNodeRuntime startNode;
    
    public QuestGraph(Long questId, String questTitle) {
        this.questId = questId;
        this.questTitle = questTitle;
        this.nodes = new HashMap<>();
    }
    
    public static QuestGraph buildFromQuest(Quest quest) {
        logger.debug("Построение графа для квеста: {} (ID: {})", quest.getTitle(), quest.getId());
        
        QuestGraph graph = new QuestGraph(quest.getId(), quest.getTitle());
        
        for (QuestStage stage : quest.getStages()) {
            QuestNodeRuntime node = new QuestNodeRuntime(
                stage.getId(),
                stage.getStageId(),
                stage.getTitle(),
                stage.getDescription(),
                stage.getResultType()
            );
            
            graph.addNode(node);
            
            if (stage.isStartStage() || 
                (quest.getStartStageId() != null && quest.getStartStageId().equals(stage.getStageId()))) {
                graph.startNode = node;
            }
        }
        
        for (QuestStage stage : quest.getStages()) {
            QuestNodeRuntime sourceNode = graph.getNode(stage.getStageId());
            if (sourceNode == null) {
                logger.warn("Узел не найден: {}", stage.getStageId());
                continue;
            }
            
            for (QuestStageOption option : stage.getOptions()) {
                ChoiceRuntime choice = new ChoiceRuntime(
                    option.getId(),
                    option.getOptionKey(),
                    option.getOptionText(),
                    option.getTargetStageId()
                );
                
                QuestNodeRuntime targetNode = graph.getNode(option.getTargetStageId());
                if (targetNode != null) {
                    choice.setTargetNode(targetNode);
                } else {
                    logger.warn("Целевой узел не найден: {} для выбора: {}", 
                               option.getTargetStageId(), option.getOptionKey());
                }
                
                sourceNode.addChoice(choice);
            }
        }
        
        logger.info("Граф построен: {} узлов, стартовый узел: {}", 
                   graph.nodes.size(), 
                   graph.startNode != null ? graph.startNode.getStageId() : "не установлен");
        
        return graph;
    }
    
    public Long getQuestId() {
        return questId;
    }
    
    public String getQuestTitle() {
        return questTitle;
    }
    
    public QuestNodeRuntime getStartNode() {
        return startNode;
    }
    
    public QuestNodeRuntime getNode(String stageId) {
        return nodes.get(stageId);
    }
    
    public Collection<QuestNodeRuntime> getAllNodes() {
        return nodes.values();
    }
    
    public int getNodeCount() {
        return nodes.size();
    }
    
    private void addNode(QuestNodeRuntime node) {
        nodes.put(node.getStageId(), node);
    }
    
    public boolean hasNode(String stageId) {
        return nodes.containsKey(stageId);
    }
    
    public boolean hasStartNode() {
        return startNode != null;
    }
    
    public List<QuestNodeRuntime> getWinNodes() {
        return nodes.values().stream()
                .filter(QuestNodeRuntime::isWinNode)
                .toList();
    }
    
    public List<QuestNodeRuntime> getLoseNodes() {
        return nodes.values().stream()
                .filter(QuestNodeRuntime::isLoseNode)
                .toList();
    }
    
    public List<QuestNodeRuntime> getEndNodes() {
        return nodes.values().stream()
                .filter(QuestNodeRuntime::isEndNode)
                .toList();
    }
    
    @Override
    public String toString() {
        return "QuestGraph{" +
                "questId=" + questId +
                ", questTitle='" + questTitle + '\'' +
                ", nodesCount=" + nodes.size() +
                ", hasStartNode=" + (startNode != null) +
                '}';
    }
}
