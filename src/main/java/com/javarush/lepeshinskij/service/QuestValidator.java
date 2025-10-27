package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.model.runtime.QuestGraph;
import com.javarush.lepeshinskij.model.runtime.QuestNodeRuntime;
import com.javarush.lepeshinskij.model.runtime.ChoiceRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class QuestValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestValidator.class);
    
    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult() {
            this.isValid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }
        
        private ValidationResult(boolean isValid, List<String> errors, List<String> warnings) {
            this.isValid = isValid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public static ValidationResult createInvalid(List<String> errors, List<String> warnings) {
            return new ValidationResult(false, errors, warnings);
        }
        
        public static ValidationResult createValid(List<String> warnings) {
            return new ValidationResult(true, Collections.emptyList(), warnings);
        }
    }
    
    public ValidationResult validateQuest(Quest quest) {
        logger.info("Валидация квеста: {} (ID: {})", quest.getTitle(), quest.getId());
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (quest.getStages().isEmpty()) {
            errors.add("Квест не содержит узлов");
            return ValidationResult.createInvalid(errors, warnings);
        }
        
        QuestGraph graph;
        try {
            graph = QuestGraph.buildFromQuest(quest);
        } catch (Exception e) {
            errors.add("Ошибка построения графа: " + e.getMessage());
            return ValidationResult.createInvalid(errors, warnings);
        }
        
        if (!graph.hasStartNode()) {
            errors.add("Не установлен стартовый узел");
        }
        
        if (graph.getWinNodes().isEmpty()) {
            errors.add("Квест не содержит узлов победы (WIN)");
        }
        
        for (QuestNodeRuntime node : graph.getAllNodes()) {
            validateNode(node, graph, errors, warnings);
        }
        
        if (graph.hasStartNode()) {
            checkReachability(graph, errors, warnings);
        }
        
        if (graph.hasStartNode() && !graph.getWinNodes().isEmpty()) {
            checkWinPaths(graph, errors, warnings);
        }
        
        boolean isValid = errors.isEmpty();
        
        logger.info("Валидация завершена: valid={}, errors={}, warnings={}", 
                   isValid, errors.size(), warnings.size());
        
        return isValid ? ValidationResult.createValid(warnings) : 
                        ValidationResult.createInvalid(errors, warnings);
    }
    
    private void validateNode(QuestNodeRuntime node, QuestGraph graph, 
                             List<String> errors, List<String> warnings) {
        
        if (node.isEndNode() && node.hasChoices()) {
            warnings.add("Конечный узел '" + node.getStageId() + "' имеет выборы (они не будут использованы)");
        }
        
        if (!node.isEndNode() && !node.hasChoices()) {
            errors.add("Узел '" + node.getStageId() + "' не является конечным, но не имеет выборов");
        }
        
        for (ChoiceRuntime choice : node.getChoices()) {
            if (!choice.isTargetNodeValid()) {
                errors.add("Выбор '" + choice.getOptionKey() + "' в узле '" + node.getStageId() + 
                          "' ссылается на несуществующий узел: " + choice.getTargetStageId());
            }
        }
    }
    
    private void checkReachability(QuestGraph graph, List<String> errors, List<String> warnings) {
        Set<String> reachableNodes = new HashSet<>();
        Queue<QuestNodeRuntime> queue = new LinkedList<>();
        
        QuestNodeRuntime startNode = graph.getStartNode();
        queue.add(startNode);
        reachableNodes.add(startNode.getStageId());
        
        while (!queue.isEmpty()) {
            QuestNodeRuntime current = queue.poll();
            
            for (ChoiceRuntime choice : current.getChoices()) {
                if (choice.isTargetNodeValid()) {
                    QuestNodeRuntime targetNode = choice.getTargetNode();
                    if (!reachableNodes.contains(targetNode.getStageId())) {
                        reachableNodes.add(targetNode.getStageId());
                        queue.add(targetNode);
                    }
                }
            }
        }
        
        for (QuestNodeRuntime node : graph.getAllNodes()) {
            if (!reachableNodes.contains(node.getStageId())) {
                warnings.add("Узел '" + node.getStageId() + "' недостижим из стартового узла");
            }
        }
    }
    
    private void checkWinPaths(QuestGraph graph, List<String> errors, List<String> warnings) {
        QuestNodeRuntime startNode = graph.getStartNode();
        List<QuestNodeRuntime> winNodes = graph.getWinNodes();
        
        boolean hasPathToWin = false;
        
        for (QuestNodeRuntime winNode : winNodes) {
            if (hasPath(graph, startNode, winNode)) {
                hasPathToWin = true;
                break;
            }
        }
        
        if (!hasPathToWin) {
            errors.add("Не существует пути от стартового узла ни к одному WIN узлу");
        }
    }
    
    private boolean hasPath(QuestGraph graph, QuestNodeRuntime from, QuestNodeRuntime to) {
        if (from.getStageId().equals(to.getStageId())) {
            return true;
        }
        
        Set<String> visited = new HashSet<>();
        Queue<QuestNodeRuntime> queue = new LinkedList<>();
        
        queue.add(from);
        visited.add(from.getStageId());
        
        while (!queue.isEmpty()) {
            QuestNodeRuntime current = queue.poll();
            
            for (ChoiceRuntime choice : current.getChoices()) {
                if (choice.isTargetNodeValid()) {
                    QuestNodeRuntime targetNode = choice.getTargetNode();
                    
                    if (targetNode.getStageId().equals(to.getStageId())) {
                        return true;
                    }
                    
                    if (!visited.contains(targetNode.getStageId())) {
                        visited.add(targetNode.getStageId());
                        queue.add(targetNode);
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean canPublish(Quest quest) {
        ValidationResult result = validateQuest(quest);
        return result.isValid();
    }
    
    public void validateAndPublish(Quest quest) {
        ValidationResult result = validateQuest(quest);
        
        if (!result.isValid()) {
            StringBuilder sb = new StringBuilder("Квест не прошел валидацию:\n");
            for (String error : result.getErrors()) {
                sb.append("- ").append(error).append("\n");
            }
            throw new IllegalStateException(sb.toString());
        }
        
        if (result.hasWarnings()) {
            logger.warn("Квест '{}' имеет предупреждения:", quest.getTitle());
            for (String warning : result.getWarnings()) {
                logger.warn("  - {}", warning);
            }
        }
    }
}
