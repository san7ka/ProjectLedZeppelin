package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.QuestStageOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class QuestValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestValidationService.class);
    
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorsAsString() {
            return String.join("; ", errors);
        }
    }
    
    public ValidationResult validateQuest(Quest quest) {
        List<String> errors = new ArrayList<>();
        
        if (quest.getStages().isEmpty()) {
            errors.add("Квест не содержит этапов");
            return new ValidationResult(false, errors);
        }
        
        QuestStage startStage = quest.getStartStage();
        if (startStage == null) {
            errors.add("Квест не имеет стартового этапа (stage с ID 'start')");
        }
        
        boolean hasWinOption = quest.getStages().stream()
            .flatMap(stage -> stage.getOptions().stream())
            .anyMatch(QuestStageOption::isWinOption);
        if (!hasWinOption) {
            errors.add("Квест должен иметь хотя бы один ответ с результатом WIN");
        }
        
        boolean hasLoseOption = quest.getStages().stream()
            .flatMap(stage -> stage.getOptions().stream())
            .anyMatch(QuestStageOption::isLoseOption);
        if (!hasLoseOption) {
            errors.add("Квест должен иметь хотя бы один ответ с результатом LOSE");
        }
        
        Map<String, QuestStage> stageMap = quest.getStages().stream()
            .collect(Collectors.toMap(QuestStage::getStageId, s -> s));
        
        for (QuestStage stage : quest.getStages()) {
            int optionCount = stage.getOptions().size();
            if (optionCount != 2) {
                errors.add(String.format(
                    "Этап '%s' должен иметь ровно 2 ответа, а имеет %d",
                    stage.getTitle(), optionCount
                ));
            }
            
            for (QuestStageOption option : stage.getOptions()) {
                if (option.getResultType() == QuestStageOption.ResultType.NONE) {
                    String targetStageId = option.getTargetStageId();
                    
                    if (targetStageId == null || targetStageId.trim().isEmpty()) {
                        errors.add(String.format(
                            "Этап '%s', опция '%s' имеет результат 'Переход к этапу', но не указан целевой этап",
                            stage.getTitle(), option.getOptionText()
                        ));
                        continue;
                    }
                    
                    if (!stageMap.containsKey(targetStageId)) {
                        errors.add(String.format(
                            "Этап '%s', опция '%s' указывает на несуществующий этап '%s'",
                            stage.getTitle(), option.getOptionText(), targetStageId
                        ));
                    }
                }
                else if (option.getTargetStageId() != null && !option.getTargetStageId().isEmpty()) {
                    errors.add(String.format(
                        "Этап '%s', опция '%s' имеет результат %s, но указан целевой этап (должен быть пустым)",
                        stage.getTitle(), option.getOptionText(), option.getResultType()
                    ));
                }
            }
        }
        
        if (startStage != null) {
            Set<String> reachableStages = findReachableStages(startStage, stageMap);
            for (QuestStage stage : quest.getStages()) {
                if (!reachableStages.contains(stage.getStageId())) {
                    errors.add(String.format(
                        "Этап '%s' недостижим от стартового этапа",
                        stage.getTitle()
                    ));
                }
            }
        }
        
        if (startStage != null && hasWinOption && hasLoseOption) {
            Set<String> reachableStages = findReachableStages(startStage, stageMap);
            
            boolean canReachWin = quest.getStages().stream()
                .filter(s -> reachableStages.contains(s.getStageId()))
                .flatMap(s -> s.getOptions().stream())
                .anyMatch(QuestStageOption::isWinOption);
            
            boolean canReachLose = quest.getStages().stream()
                .filter(s -> reachableStages.contains(s.getStageId()))
                .flatMap(s -> s.getOptions().stream())
                .anyMatch(QuestStageOption::isLoseOption);
            
            if (!canReachWin) {
                errors.add("Невозможно достичь победного ответа (WIN) от стартового этапа");
            }
            
            if (!canReachLose) {
                errors.add("Невозможно достичь проигрышного ответа (LOSE) от стартового этапа");
            }
        }
        
        boolean isValid = errors.isEmpty();
        if (isValid) {
            logger.info("Квест '{}' прошел валидацию успешно", quest.getTitle());
        } else {
            logger.warn("Квест '{}' не прошел валидацию. Ошибки: {}", 
                quest.getTitle(), String.join("; ", errors));
        }
        
        return new ValidationResult(isValid, errors);
    }
    
    private Set<String> findReachableStages(QuestStage startStage, Map<String, QuestStage> stageMap) {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        
        queue.add(startStage.getStageId());
        reachable.add(startStage.getStageId());
        
        while (!queue.isEmpty()) {
            String currentStageId = queue.poll();
            QuestStage currentStage = stageMap.get(currentStageId);
            
            if (currentStage != null) {
                for (QuestStageOption option : currentStage.getOptions()) {
                    if (option.getResultType() == QuestStageOption.ResultType.NONE) {
                        String targetStageId = option.getTargetStageId();
                        if (targetStageId != null && !targetStageId.isEmpty() && 
                            !reachable.contains(targetStageId) && stageMap.containsKey(targetStageId)) {
                            reachable.add(targetStageId);
                            queue.add(targetStageId);
                        }
                    }
                }
            }
        }
        
        return reachable;
    }
    
    public boolean canPublish(Quest quest) {
        return validateQuest(quest).isValid();
    }
    
    public String getQuestStatusInfo(Quest quest) {
        long stageCount = quest.getStages().size();
        long winOptions = quest.getStages().stream()
            .flatMap(stage -> stage.getOptions().stream())
            .filter(QuestStageOption::isWinOption)
            .count();
        long loseOptions = quest.getStages().stream()
            .flatMap(stage -> stage.getOptions().stream())
            .filter(QuestStageOption::isLoseOption)
            .count();
        
        return String.format(
            "Этапов: %d, WIN ответов: %d, LOSE ответов: %d",
            stageCount, winOptions, loseOptions
        );
    }
}
