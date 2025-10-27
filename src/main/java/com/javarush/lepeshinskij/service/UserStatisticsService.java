package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.dao.UserStatisticsDAO;
import com.javarush.lepeshinskij.dao.UserStatisticsDAOImpl;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import com.javarush.lepeshinskij.entity.UserStatistics;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UserStatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserStatisticsService.class);
    
    private UserStatisticsDAO userStatisticsDAO;
    
    public UserStatisticsService(SessionFactory sessionFactory) {
        this.userStatisticsDAO = new UserStatisticsDAOImpl(sessionFactory);
    }
    
    public UserStatisticsService(UserStatisticsDAO userStatisticsDAO) {
        this.userStatisticsDAO = userStatisticsDAO;
    }
    
    public UserStatistics getUserStatistics(User user) {
        return userStatisticsDAO.getOrCreate(user);
    }
    
    public void recordGameStart(User user) {
        try {
            UserStatistics statistics = getUserStatistics(user);
            statistics.recordGameStart();
            userStatisticsDAO.update(statistics);
            logger.info("Зарегистрировано начало игры для пользователя с ID: {}", user.getId());
        } catch (Exception e) {
            logger.error("Ошибка при регистрации начала игры для пользователя с ID: {}", user.getId(), e);
        }
    }
    
    public void recordGameWin(User user, Long playtimeMinutes) {
        try {
            UserStatistics statistics = getUserStatistics(user);
            statistics.recordWin(playtimeMinutes);
            userStatisticsDAO.update(statistics);
            logger.info("Зарегистрирована победа для пользователя с ID: {} за {} минут", user.getId(), playtimeMinutes);
        } catch (Exception e) {
            logger.error("Ошибка при регистрации победы для пользователя с ID: {}", user.getId(), e);
        }
    }
    
    public void recordGameLoss(User user, Long playtimeMinutes) {
        try {
            UserStatistics statistics = getUserStatistics(user);
            statistics.recordLoss(playtimeMinutes);
            userStatisticsDAO.update(statistics);
            logger.info("Зарегистрировано поражение для пользователя с ID: {} за {} минут", user.getId(), playtimeMinutes);
        } catch (Exception e) {
            logger.error("Ошибка при регистрации поражения для пользователя с ID: {}", user.getId(), e);
        }
    }
    
    public void updateStatisticsFromProgress(UserQuestProgress progress) {
        if (progress == null || progress.getUser() == null) {
            return;
        }
        
        User user = progress.getUser();
        
        try {
            UserStatistics statistics = getUserStatistics(user);
            
            if (progress.getStartedAt() != null && progress.getCreatedAt().isEqual(progress.getStartedAt())) {
                statistics.recordGameStart();
            }
            
            if (progress.getIsCompleted() && progress.getCompletedAt() != null) {
                Long playtimeMinutes = progress.getDurationInMinutes();
                
                boolean isWin = false;
                if (progress.getCurrentStage() != null) {
                    isWin = progress.getCurrentStage().isWinStage();
                }
                
                if (isWin) {
                    statistics.recordWin(playtimeMinutes);
                } else {
                    statistics.recordLoss(playtimeMinutes);
                }
            }
            
            userStatisticsDAO.update(statistics);
            logger.info("Обновлена статистика для пользователя с ID: {} на основе прогресса игры", user.getId());
            
        } catch (Exception e) {
            logger.error("Ошибка при обновлении статистики для пользователя с ID: {}", user.getId(), e);
        }
    }
    
    public void recalculateStatistics(User user, GameService gameService) {
        try {
            UserStatistics statistics = getUserStatistics(user);
            
            var allGames = gameService.getAllUserGames(user);
            
            statistics.setTotalGames(allGames.size());
            statistics.setCompletedGames(0);
            statistics.setWonGames(0);
            statistics.setLostGames(0);
            statistics.setTotalPlaytimeMinutes(0L);
            statistics.setBestTimeMinutes(null);
            
            for (UserQuestProgress game : allGames) {
                if (game.getIsCompleted()) {
                    statistics.incrementCompletedGames();
                    
                    Long playtimeMinutes = game.getDurationInMinutes();
                    statistics.addPlaytime(playtimeMinutes);
                    
                    boolean isWin = false;
                    if (game.getCurrentStage() != null) {
                        isWin = game.getCurrentStage().isWinStage();
                    }
                    
                    if (isWin) {
                        statistics.incrementWonGames();
                    } else {
                        statistics.incrementLostGames();
                    }
                }
            }
            
            userStatisticsDAO.update(statistics);
            logger.info("Пересчитана статистика для пользователя с ID: {}", user.getId());
            
        } catch (Exception e) {
            logger.error("Ошибка при пересчете статистики для пользователя с ID: {}", user.getId(), e);
        }
    }
}
