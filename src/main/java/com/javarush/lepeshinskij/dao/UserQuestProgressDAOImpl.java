package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserQuestProgressDAOImpl extends BaseDAOImpl<UserQuestProgress, Long> implements UserQuestProgressDAO {
    
    public UserQuestProgressDAOImpl(SessionFactory sessionFactory) {
        super(UserQuestProgress.class, sessionFactory);
    }
    
    @Override
    public Optional<UserQuestProgress> findById(Long id) {
        return executeInTransaction(session -> {
            // Сначала загружаем прогресс с квестом, автором и этапами
            String hql1 = "FROM UserQuestProgress uqp " +
                         "LEFT JOIN FETCH uqp.quest q " +
                         "LEFT JOIN FETCH q.author " +
                         "LEFT JOIN FETCH q.stages " +
                         "LEFT JOIN FETCH uqp.user " +
                         "WHERE uqp.id = :id";
            TypedQuery<UserQuestProgress> query1 = session.createQuery(hql1, UserQuestProgress.class);
            query1.setParameter("id", id);
            
            List<UserQuestProgress> results = query1.getResultList();
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            UserQuestProgress progress = results.get(0);
            
            // Затем загружаем опции для этапов
            if (progress.getQuest() != null && !progress.getQuest().getStages().isEmpty()) {
                String hql2 = "SELECT DISTINCT s FROM QuestStage s " +
                             "LEFT JOIN FETCH s.options " +
                             "WHERE s.quest.id = :questId";
                TypedQuery<com.javarush.lepeshinskij.entity.QuestStage> query2 = 
                    session.createQuery(hql2, com.javarush.lepeshinskij.entity.QuestStage.class);
                query2.setParameter("questId", progress.getQuest().getId());
                query2.getResultList();
            }
            
            return Optional.of(progress);
        });
    }
    
    @Override
    public Optional<UserQuestProgress> findByUserAndQuest(User user, Quest quest) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.user = :user AND uqp.quest = :quest";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("user", user);
            query.setParameter("quest", quest);
            
            List<UserQuestProgress> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
    
    @Override
    public Optional<UserQuestProgress> findByUserIdAndQuestId(Long userId, Long questId) {
        return executeInTransaction(session -> {
            // Сначала загружаем прогресс с квестом, автором и этапами
            String hql1 = "FROM UserQuestProgress uqp " +
                         "LEFT JOIN FETCH uqp.quest q " +
                         "LEFT JOIN FETCH q.author " +
                         "LEFT JOIN FETCH q.stages " +
                         "LEFT JOIN FETCH uqp.user " +
                         "WHERE uqp.user.id = :userId AND uqp.quest.id = :questId";
            TypedQuery<UserQuestProgress> query1 = session.createQuery(hql1, UserQuestProgress.class);
            query1.setParameter("userId", userId);
            query1.setParameter("questId", questId);
            
            List<UserQuestProgress> results = query1.getResultList();
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            UserQuestProgress progress = results.get(0);
            
            // Затем загружаем опции для этапов
            if (progress.getQuest() != null && !progress.getQuest().getStages().isEmpty()) {
                String hql2 = "SELECT DISTINCT s FROM QuestStage s " +
                             "LEFT JOIN FETCH s.options " +
                             "WHERE s.quest.id = :questId";
                TypedQuery<com.javarush.lepeshinskij.entity.QuestStage> query2 = 
                    session.createQuery(hql2, com.javarush.lepeshinskij.entity.QuestStage.class);
                query2.setParameter("questId", questId);
                query2.getResultList();
            }
            
            return Optional.of(progress);
        });
    }
    
    @Override
    public List<UserQuestProgress> findByUser(User user) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.user = :user ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("user", user);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByUserId(Long userId) {
        return executeInTransaction(session -> {
            String hql = "SELECT DISTINCT uqp FROM UserQuestProgress uqp " +
                        "LEFT JOIN FETCH uqp.quest q " +
                        "LEFT JOIN FETCH q.author " +
                        "WHERE uqp.user.id = :userId ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("userId", userId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByQuest(Quest quest) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.quest = :quest ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("quest", quest);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByQuestId(Long questId) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.quest.id = :questId ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("questId", questId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findActiveByUser(User user) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.user = :user AND uqp.isCompleted = false ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("user", user);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findActiveByUserId(Long userId) {
        return executeInTransaction(session -> {
            String hql = "SELECT DISTINCT uqp FROM UserQuestProgress uqp " +
                        "LEFT JOIN FETCH uqp.quest q " +
                        "LEFT JOIN FETCH q.author " +
                        "WHERE uqp.user.id = :userId AND uqp.isCompleted = false ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("userId", userId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findCompletedByUser(User user) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.user = :user AND uqp.isCompleted = true ORDER BY uqp.completedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("user", user);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findCompletedByUserId(Long userId) {
        return executeInTransaction(session -> {
            String hql = "SELECT DISTINCT uqp FROM UserQuestProgress uqp " +
                        "LEFT JOIN FETCH uqp.quest q " +
                        "LEFT JOIN FETCH q.author " +
                        "WHERE uqp.user.id = :userId AND uqp.isCompleted = true ORDER BY uqp.completedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("userId", userId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findActiveByQuest(Quest quest) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.quest = :quest AND uqp.isCompleted = false ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("quest", quest);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findActiveByQuestId(Long questId) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.quest.id = :questId AND uqp.isCompleted = false ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("questId", questId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findCompletedByQuest(Quest quest) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.quest = :quest AND uqp.isCompleted = true ORDER BY uqp.completedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("quest", quest);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findCompletedByQuestId(Long questId) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.quest.id = :questId AND uqp.isCompleted = true ORDER BY uqp.completedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("questId", questId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByStartedAtAfter(LocalDateTime date) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.startedAt > :date ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("date", date);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByStartedAtBefore(LocalDateTime date) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.startedAt < :date ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("date", date);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByStartedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.startedAt BETWEEN :startDate AND :endDate ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByCompletedAtAfter(LocalDateTime date) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.completedAt > :date ORDER BY uqp.completedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("date", date);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByCompletedAtBefore(LocalDateTime date) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.completedAt < :date ORDER BY uqp.completedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("date", date);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByCompletedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.completedAt BETWEEN :startDate AND :endDate ORDER BY uqp.completedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByCurrentStageId(String currentStageId) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.currentStageId = :currentStageId ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("currentStageId", currentStageId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByQuestAndCurrentStageId(Quest quest, String currentStageId) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.quest = :quest AND uqp.currentStageId = :currentStageId ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("quest", quest);
            query.setParameter("currentStageId", currentStageId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<UserQuestProgress> findByQuestIdAndCurrentStageId(Long questId, String currentStageId) {
        return executeInTransaction(session -> {
            String hql = "FROM UserQuestProgress uqp WHERE uqp.quest.id = :questId AND uqp.currentStageId = :currentStageId ORDER BY uqp.startedAt DESC";
            TypedQuery<UserQuestProgress> query = session.createQuery(hql, UserQuestProgress.class);
            query.setParameter("questId", questId);
            query.setParameter("currentStageId", currentStageId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public long countActiveByUser(User user) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.user = :user AND uqp.isCompleted = false";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("user", user);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countActiveByUserId(Long userId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.user.id = :userId AND uqp.isCompleted = false";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", userId);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countCompletedByUser(User user) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.user = :user AND uqp.isCompleted = true";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("user", user);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countCompletedByUserId(Long userId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.user.id = :userId AND uqp.isCompleted = true";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", userId);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countActiveByQuest(Quest quest) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.quest = :quest AND uqp.isCompleted = false";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("quest", quest);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countActiveByQuestId(Long questId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.quest.id = :questId AND uqp.isCompleted = false";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("questId", questId);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countCompletedByQuest(Quest quest) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.quest = :quest AND uqp.isCompleted = true";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("quest", quest);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countCompletedByQuestId(Long questId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.quest.id = :questId AND uqp.isCompleted = true";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("questId", questId);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public Object[] getProgressStatisticsByUser(User user) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp), COUNT(CASE WHEN uqp.isCompleted = false THEN 1 END), COUNT(CASE WHEN uqp.isCompleted = true THEN 1 END) FROM UserQuestProgress uqp WHERE uqp.user = :user";
            TypedQuery<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("user", user);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public Object[] getProgressStatisticsByUserId(Long userId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp), COUNT(CASE WHEN uqp.isCompleted = false THEN 1 END), COUNT(CASE WHEN uqp.isCompleted = true THEN 1 END) FROM UserQuestProgress uqp WHERE uqp.user.id = :userId";
            TypedQuery<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("userId", userId);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public Object[] getProgressStatisticsByQuest(Quest quest) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp), COUNT(CASE WHEN uqp.isCompleted = false THEN 1 END), COUNT(CASE WHEN uqp.isCompleted = true THEN 1 END) FROM UserQuestProgress uqp WHERE uqp.quest = :quest";
            TypedQuery<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("quest", quest);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public Object[] getProgressStatisticsByQuestId(Long questId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp), COUNT(CASE WHEN uqp.isCompleted = false THEN 1 END), COUNT(CASE WHEN uqp.isCompleted = true THEN 1 END) FROM UserQuestProgress uqp WHERE uqp.quest.id = :questId";
            TypedQuery<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("questId", questId);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public boolean isUserPlayingQuest(User user, Quest quest) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.user = :user AND uqp.quest = :quest AND uqp.isCompleted = false";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("user", user);
            query.setParameter("quest", quest);
            
            return query.getSingleResult() > 0;
        });
    }
    
    @Override
    public boolean isUserPlayingQuestById(Long userId, Long questId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.user.id = :userId AND uqp.quest.id = :questId AND uqp.isCompleted = false";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", userId);
            query.setParameter("questId", questId);
            
            return query.getSingleResult() > 0;
        });
    }
    
    @Override
    public boolean hasUserCompletedQuest(User user, Quest quest) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.user = :user AND uqp.quest = :quest AND uqp.isCompleted = true";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("user", user);
            query.setParameter("quest", quest);
            
            return query.getSingleResult() > 0;
        });
    }
    
    public boolean hasUserCompletedQuestById(Long userId, Long questId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(uqp) FROM UserQuestProgress uqp WHERE uqp.user.id = :userId AND uqp.quest.id = :questId AND uqp.isCompleted = true";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", userId);
            query.setParameter("questId", questId);
            
            return query.getSingleResult() > 0;
        });
    }
    
}
