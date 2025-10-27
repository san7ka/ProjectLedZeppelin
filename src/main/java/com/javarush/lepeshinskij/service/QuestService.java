package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.dao.GameDAO;
import com.javarush.lepeshinskij.dao.GameDAOImpl;
import com.javarush.lepeshinskij.dao.QuestDAO;
import com.javarush.lepeshinskij.dao.QuestDAOImpl;
import com.javarush.lepeshinskij.dao.QuestStageDAO;
import com.javarush.lepeshinskij.dao.QuestStageDAOImpl;
import com.javarush.lepeshinskij.entity.Game;
import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.QuestStageOption;
import com.javarush.lepeshinskij.entity.QuestStatus;
import com.javarush.lepeshinskij.entity.User;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class QuestService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestService.class);
    
    private QuestDAO questDAO;
    private QuestStageDAO questStageDAO;
    private GameDAO gameDAO;
    private QuestValidator questValidator;
    
    public QuestService(SessionFactory sessionFactory) {
        this.questDAO = new QuestDAOImpl(sessionFactory);
        this.questStageDAO = new QuestStageDAOImpl(sessionFactory);
        this.gameDAO = new GameDAOImpl(sessionFactory);
        this.questValidator = new QuestValidator();
    }
    
    public QuestService(QuestDAO questDAO, QuestStageDAO questStageDAO) {
        this.questDAO = questDAO;
        this.questStageDAO = questStageDAO;
        this.gameDAO = null; // For testing purposes
        this.questValidator = new QuestValidator();
    }
    
    public Quest createQuest(String title, String description, User author) {
        logger.info("Создание нового квеста: {} автором: {}", title, author.getUsername());
        
        Quest quest = new Quest(title, description, author);
        quest.setCreatedAt(LocalDateTime.now());
        quest.setUpdatedAt(LocalDateTime.now());
        
        Quest savedQuest = questDAO.save(quest);
        
        logger.info("Квест успешно создан: {} (ID: {})", title, savedQuest.getId());
        return savedQuest;
    }
    
    public Optional<Quest> getQuestById(Long questId) {
        return questDAO.findById(questId);
    }
    
    public Quest getQuestByIdOrNull(Long questId) {
        return questDAO.findById(questId).orElse(null);
    }
    
    public List<Quest> getAllQuests() {
        return questDAO.findAllWithAuthor();
    }
    
    public List<Quest> getAllQuestsWithDetails() {
        return questDAO.findAllWithAuthorAndStages();
    }
    
    public List<Quest> getQuestsByAuthor(User author) {
        if (author == null || author.getId() == null) {
            return java.util.Collections.emptyList();
        }
        return questDAO.findByAuthorId(author.getId());
    }
    
    public List<Quest> getQuestsByAuthorId(Long authorId) {
        return questDAO.findByAuthorId(authorId);
    }
    
    public List<Quest> searchQuestsByTitle(String title) {
        return questDAO.findByTitleContaining(title);
    }
    
    public Quest updateQuest(Quest quest) {
        logger.info("Обновление квеста: {} (ID: {})", quest.getTitle(), quest.getId());
        
        quest.setUpdatedAt(LocalDateTime.now());
        Quest updatedQuest = questDAO.update(quest);
        
        logger.info("Квест успешно обновлен: {}", quest.getTitle());
        return updatedQuest;
    }
    
    public void deleteQuest(Long questId) {
        logger.info("Удаление квеста с ID: {}", questId);
        
        Quest quest = questDAO.findById(questId).orElse(null);
        if (quest == null) {
            logger.warn("Квест с ID {} не найден", questId);
            return;
        }
        
        if (gameDAO != null) {
            List<Game> games = gameDAO.findByQuest(quest);
            logger.info("Найдено {} игр(ы) для удаления перед удалением квеста ID: {}", games.size(), questId);
            for (Game game : games) {
                gameDAO.delete(game);
            }
            logger.info("Удалено {} игр(ы) для квеста ID: {}", games.size(), questId);
        }
        
        questDAO.deleteById(questId);
        logger.info("Квест с ID {} успешно удален", questId);
    }
    
    public Optional<QuestStage> getStartStage(Quest quest) {
        return questStageDAO.findStartStageByQuest(quest);
    }
    
    public Optional<QuestStage> getStage(Quest quest, String stageId) {
        Optional<QuestStage> stageOpt = questStageDAO.findByQuestAndStageId(quest, stageId);
        if (stageOpt.isPresent()) {
            QuestStage stage = stageOpt.get();
            List<QuestStageOption> options = questStageDAO.loadStageOptions(stage.getId());
            stage.setOptions(options);
        }
        return stageOpt;
    }
    
    public List<QuestStage> getQuestStages(Quest quest) {
        return questStageDAO.findByQuest(quest);
    }
    
    public List<QuestStage> getQuestStages(Long questId) {
        Quest quest = questDAO.findById(questId).orElse(null);
        if (quest == null) {
            return new java.util.ArrayList<>();
        }
        return questStageDAO.findByQuest(quest);
    }
    
    public QuestStage createStage(Quest quest, String stageId, String title, String description, 
                                 String resultType, int stageIndex) {
        logger.info("Создание этапа квеста: {} для квеста: {}", stageId, quest.getTitle());
        
        QuestStage stage = new QuestStage();
        stage.setQuest(quest);
        stage.setStageId(stageId);
        stage.setTitle(title);
        stage.setDescription(description);
        stage.setResultType(QuestStage.ResultType.valueOf(resultType));
        stage.setStageIndex(stageIndex);
        stage.setCreatedAt(LocalDateTime.now());
        stage.setUpdatedAt(LocalDateTime.now());
        
        QuestStage savedStage = questStageDAO.save(stage);
        
        logger.info("Этап квеста успешно создан: {} (ID: {})", stageId, savedStage.getId());
        return savedStage;
    }
    
    public QuestStage updateStage(QuestStage stage) {
        logger.info("Обновление этапа квеста: {} (ID: {})", stage.getStageId(), stage.getId());
        
        stage.setUpdatedAt(LocalDateTime.now());
        QuestStage updatedStage = questStageDAO.update(stage);
        
        logger.info("Этап квеста успешно обновлен: {}", stage.getStageId());
        return updatedStage;
    }
    
    public void deleteStage(Long stageId) {
        logger.info("Удаление этапа квеста с ID: {}", stageId);
        
        questStageDAO.deleteById(stageId);
        logger.info("Этап квеста с ID {} успешно удален", stageId);
    }
    
    public long getQuestCount() {
        return questDAO.count();
    }
    
    public long getQuestCountByAuthor(User author) {
        return questDAO.countByAuthor(author);
    }
    
    public long getQuestCountByAuthorId(Long authorId) {
        return questDAO.countByAuthorId(authorId);
    }
    
    public QuestStage getFirstStage(Quest quest) {
        Optional<QuestStage> stage = questStageDAO.findStartStageByQuest(quest);
        if (stage.isPresent()) {
            QuestStage result = stage.get();
            logger.info("Найден первый этап: {} с ID: {}", result.getStageId(), result.getId());
            List<QuestStageOption> options = questStageDAO.loadStageOptions(result.getId());
            logger.info("Загружено {} опций для этапа {}", options.size(), result.getStageId());
            result.setOptions(options);
            return result;
        }
        logger.warn("Первый этап для квеста {} не найден", quest.getTitle());
        return null;
    }
    
    public QuestValidator.ValidationResult validateQuest(Quest quest) {
        logger.info("Валидация квеста: {} (ID: {})", quest.getTitle(), quest.getId());
        return questValidator.validateQuest(quest);
    }
    
    public QuestValidator.ValidationResult validateQuestById(Long questId) {
        Quest quest = questDAO.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        return validateQuest(quest);
    }
    
    public boolean canPublishQuest(Quest quest) {
        return questValidator.canPublish(quest);
    }
    
    public void validateAndPublish(Long questId) {
        logger.info("Валидация и публикация квеста ID: {}", questId);
        
        Quest quest = questDAO.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        
        questValidator.validateAndPublish(quest);
        
        quest.setStatus(QuestStatus.PUBLISHED);
        quest.setUpdatedAt(LocalDateTime.now());
        questDAO.update(quest);
        
        logger.info("Квест успешно опубликован: {} (ID: {})", quest.getTitle(), questId);
    }
    
    public void unpublishQuest(Long questId) {
        logger.info("Снятие с публикации квеста ID: {}", questId);
        
        Quest quest = questDAO.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        
        quest.setStatus(QuestStatus.DRAFT);
        quest.setUpdatedAt(LocalDateTime.now());
        questDAO.update(quest);
        
        logger.info("Квест снят с публикации: {} (ID: {})", quest.getTitle(), questId);
    }
}