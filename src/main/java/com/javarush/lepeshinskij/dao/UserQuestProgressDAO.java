package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserQuestProgressDAO extends BaseDAO<UserQuestProgress, Long> {
    
    Optional<UserQuestProgress> findByUserAndQuest(User user, Quest quest);
    
    Optional<UserQuestProgress> findByUserIdAndQuestId(Long userId, Long questId);
    
    List<UserQuestProgress> findByUser(User user);
    
    List<UserQuestProgress> findByUserId(Long userId);
    
    List<UserQuestProgress> findByQuest(Quest quest);
    
    List<UserQuestProgress> findByQuestId(Long questId);
    
    List<UserQuestProgress> findActiveByUser(User user);
    
    List<UserQuestProgress> findActiveByUserId(Long userId);
    
    List<UserQuestProgress> findCompletedByUser(User user);
    
    List<UserQuestProgress> findCompletedByUserId(Long userId);
    
    List<UserQuestProgress> findActiveByQuest(Quest quest);
    
    List<UserQuestProgress> findActiveByQuestId(Long questId);
    
    List<UserQuestProgress> findCompletedByQuest(Quest quest);
    
    List<UserQuestProgress> findCompletedByQuestId(Long questId);
    
    List<UserQuestProgress> findByStartedAtAfter(LocalDateTime date);
    
    List<UserQuestProgress> findByStartedAtBefore(LocalDateTime date);
    
    List<UserQuestProgress> findByStartedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<UserQuestProgress> findByCompletedAtAfter(LocalDateTime date);
    
    List<UserQuestProgress> findByCompletedAtBefore(LocalDateTime date);
    
    List<UserQuestProgress> findByCompletedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<UserQuestProgress> findByCurrentStageId(String currentStageId);
    
    List<UserQuestProgress> findByQuestAndCurrentStageId(Quest quest, String currentStageId);
    
    List<UserQuestProgress> findByQuestIdAndCurrentStageId(Long questId, String currentStageId);
    
    long countActiveByUser(User user);
    
    long countActiveByUserId(Long userId);
    
    long countCompletedByUser(User user);
    
    long countCompletedByUserId(Long userId);
    
    long countActiveByQuest(Quest quest);
    
    long countActiveByQuestId(Long questId);
    
    long countCompletedByQuest(Quest quest);
    
    long countCompletedByQuestId(Long questId);
    
    Object[] getProgressStatisticsByUser(User user);
    
    Object[] getProgressStatisticsByUserId(Long userId);
    
    Object[] getProgressStatisticsByQuest(Quest quest);
    
    Object[] getProgressStatisticsByQuestId(Long questId);
    
    boolean isUserPlayingQuest(User user, Quest quest);
    
    boolean isUserPlayingQuestById(Long userId, Long questId);
    
    boolean hasUserCompletedQuest(User user, Quest quest);
    
}
