package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.dao.QuestDAO;
import com.javarush.lepeshinskij.dao.QuestDAOImpl;
import com.javarush.lepeshinskij.dao.QuestStageDAO;
import com.javarush.lepeshinskij.dao.QuestStageDAOImpl;
import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.QuestStageOption;
import com.javarush.lepeshinskij.entity.User;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class QuestEditorService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestEditorService.class);
    
    private final QuestDAO questDAO;
    private final QuestStageDAO questStageDAO;
    
    public QuestEditorService(SessionFactory sessionFactory) {
        this.questDAO = new QuestDAOImpl(sessionFactory);
        this.questStageDAO = new QuestStageDAOImpl(sessionFactory);
    }
    
    public QuestEditorService(QuestDAO questDAO, QuestStageDAO questStageDAO) {
        this.questDAO = questDAO;
        this.questStageDAO = questStageDAO;
    }
    
    public Quest createEmptyQuest(String title, String description, User author) {
        logger.info("Создание пустого квеста: {} автором: {}", title, author.getUsername());
        
        Quest quest = new Quest(title, description, author);
        return questDAO.save(quest);
    }
    
    public QuestStage addStandardNode(Long questId, String stageId, String title, String description) {
        logger.info("Добавление стандартного узла: {} к квесту ID: {}", stageId, questId);
        
        Quest quest = questDAO.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        
        if (quest.hasStage(stageId)) {
            throw new IllegalArgumentException("Stage with ID '" + stageId + "' already exists");
        }
        
        QuestStage stage = new QuestStage(stageId, title, description, QuestStage.ResultType.NONE);
        stage.setQuest(quest);
        stage.setStageIndex(quest.getStageCount());
        
        return questStageDAO.save(stage);
    }
    
    public QuestStage addWinNode(Long questId, String stageId, String title, String description) {
        logger.info("Добавление WIN узла: {} к квесту ID: {}", stageId, questId);
        
        Quest quest = questDAO.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        
        if (quest.hasStage(stageId)) {
            throw new IllegalArgumentException("Stage with ID '" + stageId + "' already exists");
        }
        
        QuestStage stage = new QuestStage(stageId, title, description, QuestStage.ResultType.WIN);
        stage.setQuest(quest);
        stage.setStageIndex(quest.getStageCount());
        
        return questStageDAO.save(stage);
    }
    
    public QuestStage addLoseNode(Long questId, String stageId, String title, String description) {
        logger.info("Добавление LOSE узла: {} к квесту ID: {}", stageId, questId);
        
        Quest quest = questDAO.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        
        if (quest.hasStage(stageId)) {
            throw new IllegalArgumentException("Stage with ID '" + stageId + "' already exists");
        }
        
        QuestStage stage = new QuestStage(stageId, title, description, QuestStage.ResultType.LOSE);
        stage.setQuest(quest);
        stage.setStageIndex(quest.getStageCount());
        
        return questStageDAO.save(stage);
    }
    
    public QuestStage updateNode(Long nodeId, String title, String description) {
        logger.info("Обновление узла ID: {}", nodeId);
        
        QuestStage stage = questStageDAO.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + nodeId));
        
        stage.setTitle(title);
        stage.setDescription(description);
        stage.setUpdatedAt(LocalDateTime.now());
        
        return questStageDAO.update(stage);
    }
    
    public void deleteNode(Long nodeId) {
        logger.info("Удаление узла ID: {}", nodeId);
        
        QuestStage stage = questStageDAO.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + nodeId));
        
        Quest quest = stage.getQuest();
        
        if (stage.isStartStage()) {
            throw new IllegalStateException("Cannot delete start stage");
        }
        
        for (QuestStage otherStage : quest.getStages()) {
            if (otherStage.getId().equals(nodeId)) {
                continue;
            }
            
            for (QuestStageOption option : otherStage.getOptions()) {
                if (stage.getStageId().equals(option.getTargetStageId())) {
                    throw new IllegalStateException("Cannot delete stage that is referenced by other stages");
                }
            }
        }
        
        questStageDAO.deleteById(nodeId);
    }
    
    public QuestStageOption addChoice(Long fromNodeId, String optionKey, String optionText, String targetStageId) {
        logger.info("Добавление выбора: {} -> {} для узла ID: {}", optionKey, targetStageId, fromNodeId);
        
        QuestStage fromStage = questStageDAO.findById(fromNodeId)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + fromNodeId));
        
        Quest quest = fromStage.getQuest();
        if (!quest.hasStage(targetStageId)) {
            throw new IllegalArgumentException("Target stage not found: " + targetStageId);
        }
        
        if (fromStage.hasOption(optionKey)) {
            throw new IllegalArgumentException("Option with key '" + optionKey + "' already exists");
        }
        
        QuestStageOption option = new QuestStageOption(fromStage, optionKey, optionText, targetStageId);
        fromStage.addOption(option);
        
        questStageDAO.update(fromStage);
        
        return option;
    }
    
    public QuestStageOption updateChoice(Long choiceId, String optionText, String targetStageId) {
        logger.info("Обновление выбора ID: {}", choiceId);
        
        QuestStageOption option = questStageDAO.findOptionById(choiceId)
                .orElseThrow(() -> new IllegalArgumentException("Option not found: " + choiceId));
        
        Quest quest = option.getStage().getQuest();
        if (!quest.hasStage(targetStageId)) {
            throw new IllegalArgumentException("Target stage not found: " + targetStageId);
        }
        
        option.setOptionText(optionText);
        option.setTargetStageId(targetStageId);
        
        questStageDAO.update(option.getStage());
        
        return option;
    }
    
    public void deleteChoice(Long choiceId) {
        logger.info("Удаление выбора ID: {}", choiceId);
        
        QuestStageOption option = questStageDAO.findOptionById(choiceId)
                .orElseThrow(() -> new IllegalArgumentException("Option not found: " + choiceId));
        
        QuestStage stage = option.getStage();
        stage.removeOption(option);
        
        questStageDAO.update(stage);
    }
    
    public QuestStageOption linkNodes(Long fromNodeId, Long toNodeId, String choiceText) {
        logger.info("Связывание узлов: {} -> {}", fromNodeId, toNodeId);
        
        QuestStage fromStage = questStageDAO.findById(fromNodeId)
                .orElseThrow(() -> new IllegalArgumentException("From stage not found: " + fromNodeId));
        
        QuestStage toStage = questStageDAO.findById(toNodeId)
                .orElseThrow(() -> new IllegalArgumentException("To stage not found: " + toNodeId));
        
        if (!fromStage.getQuest().getId().equals(toStage.getQuest().getId())) {
            throw new IllegalArgumentException("Stages belong to different quests");
        }
        
        String optionKey = "choice_" + fromStage.getOptions().size();
        
        return addChoice(fromNodeId, optionKey, choiceText, toStage.getStageId());
    }
    
    public void setStartNode(Long questId, Long nodeId) {
        logger.info("Установка стартового узла {} для квеста ID: {}", nodeId, questId);
        
        Quest quest = questDAO.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        
        QuestStage stage = questStageDAO.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + nodeId));
        
        if (!stage.getQuest().getId().equals(questId)) {
            throw new IllegalArgumentException("Stage does not belong to this quest");
        }
        
        quest.setStartStageId(stage.getStageId());
        questDAO.update(quest);
    }
    
    public List<QuestStage> getQuestNodes(Long questId) {
        Quest quest = questDAO.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        
        return questStageDAO.findByQuest(quest);
    }
}
