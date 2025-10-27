package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.QuestStageOption;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class QuestStageDAOImpl extends BaseDAOImpl<QuestStage, Long> implements QuestStageDAO {
    
    public QuestStageDAOImpl(SessionFactory sessionFactory) {
        super(QuestStage.class, sessionFactory);
    }
    
    @Override
    public Optional<QuestStage> findByQuestIdAndStageId(Long questId, String stageId) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.quest.id = :questId AND qs.stageId = :stageId";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("questId", questId);
            query.setParameter("stageId", stageId);
            
            List<QuestStage> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
    
    @Override
    public Optional<QuestStage> findByQuestAndStageId(Quest quest, String stageId) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.quest = :quest AND qs.stageId = :stageId";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("quest", quest);
            query.setParameter("stageId", stageId);
            
            List<QuestStage> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
    
    @Override
    public List<QuestStage> findByQuest(Quest quest) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.quest = :quest ORDER BY qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("quest", quest);
            query.setHint("org.hibernate.cacheable", true);
            query.setHint("org.hibernate.cacheRegion", "questStagesByQuest");
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findByQuestId(Long questId) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.quest.id = :questId ORDER BY qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("questId", questId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findByQuestOrderByStageIndex(Quest quest) {
        return findByQuest(quest);
    }
    
    @Override
    public List<QuestStage> findByQuestIdOrderByStageIndex(Long questId) {
        return findByQuestId(questId);
    }
    
    @Override
    public Optional<QuestStage> findStartStageByQuest(Quest quest) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.quest = :quest ORDER BY qs.stageIndex ASC";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("quest", quest);
            query.setMaxResults(1);
            
            List<QuestStage> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
    
    @Override
    public Optional<QuestStage> findStartStageByQuestId(Long questId) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.quest.id = :questId ORDER BY qs.stageIndex ASC";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("questId", questId);
            query.setMaxResults(1);
            
            List<QuestStage> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
    
    @Override
    public List<QuestStage> findByResultType(QuestStage.ResultType resultType) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.resultType = :resultType ORDER BY qs.quest.title, qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("resultType", resultType);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findByQuestAndResultType(Quest quest, QuestStage.ResultType resultType) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.quest = :quest AND qs.resultType = :resultType ORDER BY qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("quest", quest);
            query.setParameter("resultType", resultType);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findByQuestIdAndResultType(Long questId, QuestStage.ResultType resultType) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.quest.id = :questId AND qs.resultType = :resultType ORDER BY qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("questId", questId);
            query.setParameter("resultType", resultType);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findWinStagesByQuest(Quest quest) {
        return findByQuestAndResultType(quest, QuestStage.ResultType.WIN);
    }
    
    @Override
    public List<QuestStage> findLoseStagesByQuest(Quest quest) {
        return findByQuestAndResultType(quest, QuestStage.ResultType.LOSE);
    }
    
    @Override
    public List<QuestStage> findWinStagesByQuestId(Long questId) {
        return findByQuestIdAndResultType(questId, QuestStage.ResultType.WIN);
    }
    
    @Override
    public List<QuestStage> findLoseStagesByQuestId(Long questId) {
        return findByQuestIdAndResultType(questId, QuestStage.ResultType.LOSE);
    }
    
    @Override
    public List<QuestStage> findByTitleContaining(String titlePart) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE LOWER(qs.title) LIKE LOWER(:titlePart) ORDER BY qs.quest.title, qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("titlePart", "%" + titlePart + "%");
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findByDescriptionContaining(String descriptionPart) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE LOWER(qs.description) LIKE LOWER(:descriptionPart) ORDER BY qs.quest.title, qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("descriptionPart", "%" + descriptionPart + "%");
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findByTitleOrDescriptionContaining(String searchTerm) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE LOWER(qs.title) LIKE LOWER(:searchTerm) OR LOWER(qs.description) LIKE LOWER(:searchTerm) ORDER BY qs.quest.title, qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findByStageIndex(Integer stageIndex) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.stageIndex = :stageIndex ORDER BY qs.quest.title";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("stageIndex", stageIndex);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findByStageIndexBetween(Integer minIndex, Integer maxIndex) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.stageIndex BETWEEN :minIndex AND :maxIndex ORDER BY qs.quest.title, qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("minIndex", minIndex);
            query.setParameter("maxIndex", maxIndex);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findByStageIndexGreaterThanEqual(Integer minIndex) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.stageIndex >= :minIndex ORDER BY qs.quest.title, qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("minIndex", minIndex);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<QuestStage> findByStageIndexLessThanEqual(Integer maxIndex) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStage qs WHERE qs.stageIndex <= :maxIndex ORDER BY qs.quest.title, qs.stageIndex";
            TypedQuery<QuestStage> query = session.createQuery(hql, QuestStage.class);
            query.setParameter("maxIndex", maxIndex);
            
            return query.getResultList();
        });
    }
    
    @Override
    public long countByQuest(Quest quest) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(qs) FROM QuestStage qs WHERE qs.quest = :quest";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("quest", quest);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countByQuestId(Long questId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(qs) FROM QuestStage qs WHERE qs.quest.id = :questId";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("questId", questId);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countByResultType(QuestStage.ResultType resultType) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(qs) FROM QuestStage qs WHERE qs.resultType = :resultType";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("resultType", resultType);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countByQuestAndResultType(Quest quest, QuestStage.ResultType resultType) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(qs) FROM QuestStage qs WHERE qs.quest = :quest AND qs.resultType = :resultType";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("quest", quest);
            query.setParameter("resultType", resultType);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public Integer getMaxStageIndexByQuest(Quest quest) {
        return executeInTransaction(session -> {
            String hql = "SELECT MAX(qs.stageIndex) FROM QuestStage qs WHERE qs.quest = :quest";
            TypedQuery<Integer> query = session.createQuery(hql, Integer.class);
            query.setParameter("quest", quest);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public Integer getMaxStageIndexByQuestId(Long questId) {
        return executeInTransaction(session -> {
            String hql = "SELECT MAX(qs.stageIndex) FROM QuestStage qs WHERE qs.quest.id = :questId";
            TypedQuery<Integer> query = session.createQuery(hql, Integer.class);
            query.setParameter("questId", questId);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public boolean existsByQuestAndStageId(Quest quest, String stageId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(qs) FROM QuestStage qs WHERE qs.quest = :quest AND qs.stageId = :stageId";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("quest", quest);
            query.setParameter("stageId", stageId);
            
            return query.getSingleResult() > 0;
        });
    }
    
    @Override
    public boolean existsByQuestIdAndStageId(Long questId, String stageId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(qs) FROM QuestStage qs WHERE qs.quest.id = :questId AND qs.stageId = :stageId";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("questId", questId);
            query.setParameter("stageId", stageId);
            
            return query.getSingleResult() > 0;
        });
    }
    
    @Override
    public List<QuestStageOption> loadStageOptions(Long stageId) {
        return executeInTransaction(session -> {
            String hql = "FROM QuestStageOption qso WHERE qso.stage.id = :stageId ORDER BY qso.optionKey";
            TypedQuery<QuestStageOption> query = session.createQuery(hql, QuestStageOption.class);
            query.setParameter("stageId", stageId);
            
            return query.getResultList();
        });
    }
    
    @Override
    public Optional<QuestStageOption> findOptionById(Long optionId) {
        return executeInTransaction(session -> {
            QuestStageOption option = session.get(QuestStageOption.class, optionId);
            return Optional.ofNullable(option);
        });
    }
}
