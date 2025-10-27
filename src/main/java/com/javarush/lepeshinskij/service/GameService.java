package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.dao.*;
import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.Game;
import com.javarush.lepeshinskij.entity.GameResult;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class GameService {
    
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    
    private final UserQuestProgressDAO userQuestProgressDAO;
    private final QuestService questService;
    private final UserStatisticsService statisticsService;
    private final GameDAO gameDAO;
    
    public GameService(SessionFactory sessionFactory) {
        this.userQuestProgressDAO = new UserQuestProgressDAOImpl(sessionFactory);
        this.questService = new QuestService(sessionFactory);
        this.statisticsService = new UserStatisticsService(sessionFactory);
        this.gameDAO = new GameDAOImpl(sessionFactory);
    }
    
    public GameService(UserQuestProgressDAO userQuestProgressDAO, QuestService questService, UserStatisticsService statisticsService, GameDAO gameDAO) {
        this.userQuestProgressDAO = userQuestProgressDAO;
        this.questService = questService;
        this.statisticsService = statisticsService;
        this.gameDAO = gameDAO;
    }
    
    public UserQuestProgress startQuest(User user, Quest quest) {
        logger.info("Начало игры пользователя {} в квест: {}", user.getUsername(), quest.getTitle());
        
        Optional<UserQuestProgress> existingProgress = userQuestProgressDAO.findByUserAndQuest(user, quest);
        if (existingProgress.isPresent()) {
            UserQuestProgress progress = existingProgress.get();
            if (!progress.getIsCompleted() && progress.getId() != null) {
                logger.info("Пользователь {} уже играет в квест: {}", user.getUsername(), quest.getTitle());
                return progress;
            }

            if (progress.getId() == null) {
                logger.warn("Найден прогресс без ID для пользователя {} в квесте {}, будет пересоздан", 
                    user.getUsername(), quest.getTitle());
            }
        }
        
        Optional<QuestStage> startStageOpt = questService.getStartStage(quest);
        if (startStageOpt.isEmpty()) {
            throw new IllegalStateException("Квест не имеет стартового этапа");
        }
        
        QuestStage startStage = startStageOpt.get();
        
        UserQuestProgress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            progress.reset();
            progress.setCurrentStageId(startStage.getStageId());
            progress = userQuestProgressDAO.update(progress);
        } else {
            progress = new UserQuestProgress(user, quest, startStage.getStageId());
            progress = userQuestProgressDAO.save(progress);
        }
        
        logger.info("Игра начата: пользователь {} в квесте {} на этапе {}", 
            user.getUsername(), quest.getTitle(), startStage.getStageId());
        
        statisticsService.recordGameStart(user);
        
        return progress;
    }
    
    public UserQuestProgress startQuest(User user, Long questId) {
        Quest quest = questService.getQuestByIdOrNull(questId);
        if (quest == null) {
            throw new IllegalArgumentException("Квест с ID " + questId + " не найден");
        }
        
        return startQuest(user, quest);
    }
    
    public QuestStage getCurrentStage(UserQuestProgress progress) {
        Quest quest = progress.getQuest();
        String currentStageId = progress.getCurrentStageId();
        
        return questService.getStage(quest, currentStageId).orElse(null);
    }
    
    public boolean makeMove(UserQuestProgress progress, String choice) {
        logger.info("Пользователь {} делает выбор '{}' в квесте {}", 
            progress.getUsername(), choice, progress.getQuestTitle());
        
        QuestStage currentStage = getCurrentStage(progress);
        if (currentStage == null) {
            logger.error("Текущий этап не найден для прогресса: {}", progress.getId());
            return false;
        }
        
        if (!currentStage.hasOption(choice)) {
            logger.warn("Неверный выбор '{}' для этапа {}", choice, currentStage.getStageId());
            return false;
        }
        
        var option = currentStage.getOptionByKey(choice);
        if (option == null) {
            logger.error("Опция '{}' не найдена для этапа {}", choice, currentStage.getStageId());
            return false;
        }
        
        if (option.isEndOption()) {
            boolean isWin = option.isWinOption();
            progress.complete();
            userQuestProgressDAO.update(progress);
            
            logger.info("Игра завершена для пользователя {} в квесте {} через опцию: {}", 
                progress.getUsername(), progress.getQuestTitle(), 
                isWin ? "ПОБЕДА" : "ПОРАЖЕНИЕ");
            
            Long playtimeMinutes = progress.getDurationInMinutes();
            if (isWin) {
                statisticsService.recordGameWin(progress.getUser(), playtimeMinutes);
                gameDAO.save(new Game(progress.getUser(), progress.getQuest(), GameResult.WIN));
            } else {
                statisticsService.recordGameLoss(progress.getUser(), playtimeMinutes);
                gameDAO.save(new Game(progress.getUser(), progress.getQuest(), GameResult.LOSS));
            }
            
            return false;
        }
        
        String nextStageId = option.getTargetStageId();
        
        Optional<QuestStage> nextStageOpt = questService.getStage(progress.getQuest(), nextStageId);
        if (nextStageOpt.isEmpty()) {
            logger.error("Следующий этап '{}' не найден", nextStageId);
            return false;
        }
        
        QuestStage nextStage = nextStageOpt.get();
        
        progress.advanceToStage(nextStageId);
        userQuestProgressDAO.update(progress);
        
        logger.info("Пользователь {} перешел на этап {} в квесте {}", 
            progress.getUsername(), nextStageId, progress.getQuestTitle());
        
        if (nextStage.isEndStage()) {
            if (nextStage.isWinStage()) {
                progress.complete();
            } else if (nextStage.isLoseStage()) {
                progress.complete();
            }
            userQuestProgressDAO.update(progress);
            
            logger.info("Игра завершена для пользователя {} в квесте {}: {}", 
                progress.getUsername(), progress.getQuestTitle(), 
                nextStage.isWinStage() ? "ПОБЕДА" : "ПОРАЖЕНИЕ");
            
            Long playtimeMinutes = progress.getDurationInMinutes();
            if (nextStage.isWinStage()) {
                statisticsService.recordGameWin(progress.getUser(), playtimeMinutes);
                gameDAO.save(new Game(progress.getUser(), progress.getQuest(), GameResult.WIN));
            } else {
                statisticsService.recordGameLoss(progress.getUser(), playtimeMinutes);
                gameDAO.save(new Game(progress.getUser(), progress.getQuest(), GameResult.LOSS));
            }
            
            return false;
        }
        
        return true;
    }
    
    public boolean isUserPlayingQuest(User user, Quest quest) {
        if (user == null || user.getId() == null || quest == null || quest.getId() == null) {
            return false;
        }
        return userQuestProgressDAO.isUserPlayingQuestById(user.getId(), quest.getId());
    }
    
    public UserQuestProgress getUserQuestProgress(User user, Quest quest) {
        if (user == null || user.getId() == null || quest == null || quest.getId() == null) {
            return null;
        }
        return userQuestProgressDAO.findByUserIdAndQuestId(user.getId(), quest.getId()).orElse(null);
    }
    
    public List<UserQuestProgress> getActiveGames(User user) {
        if (user == null || user.getId() == null) {
            return java.util.Collections.emptyList();
        }
        return userQuestProgressDAO.findActiveByUserId(user.getId());
    }
    
    public List<UserQuestProgress> getCompletedGames(User user) {
        if (user == null || user.getId() == null) {
            return java.util.Collections.emptyList();
        }
        return userQuestProgressDAO.findCompletedByUserId(user.getId());
    }
    
    public List<UserQuestProgress> getAllUserGames(User user) {
        if (user == null || user.getId() == null) {
            return java.util.Collections.emptyList();
        }
        return userQuestProgressDAO.findByUserId(user.getId());
    }

    public List<Game> getGameHistory(User user) {
        if (user == null) {
            return java.util.Collections.emptyList();
        }
        return gameDAO.findByUser(user);
    }
    
    public void resetGameProgress(Long progressId) {
        logger.info("Сброс прогресса игры с ID: {}", progressId);
        
        UserQuestProgress progress = userQuestProgressDAO.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("Прогресс с ID " + progressId + " не найден"));
        
        Quest quest = progress.getQuest();
        Optional<QuestStage> startStageOpt = questService.getStartStage(quest);
        if (startStageOpt.isPresent()) {
            progress.reset();
            progress.setCurrentStageId(startStageOpt.get().getStageId());
            userQuestProgressDAO.update(progress);
            
            logger.info("Прогресс игры сброшен для пользователя {} в квесте {}", 
                progress.getUsername(), quest.getTitle());
        } else {
            throw new IllegalStateException("Квест не имеет стартового этапа");
        }
    }
    
    public void deleteGameProgress(Long progressId) {
        logger.info("Удаление прогресса игры с ID: {}", progressId);
        
        userQuestProgressDAO.deleteById(progressId);
        logger.info("Прогресс игры с ID {} успешно удален", progressId);
    }
    
    public UserQuestProgress getProgressById(Long progressId) {
        return userQuestProgressDAO.findById(progressId).orElse(null);
    }
    
    public List<String> getAvailableChoices(UserQuestProgress progress) {
        QuestStage currentStage = getCurrentStage(progress);
        if (currentStage == null) {
            return List.of();
        }
        
        return currentStage.getOptions().stream()
                .map(option -> option.getOptionKey())
                .collect(java.util.stream.Collectors.toList());
    }
    
    public boolean makeChoice(UserQuestProgress progress, String choice) {
        boolean continueGame = makeMove(progress, choice);
        return !continueGame;
    }
    
    public QuestStage getFirstStage(Quest quest) {
        return questService.getFirstStage(quest);
    }
}