package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.User;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class QuestDAOImpl extends BaseDAOImpl<Quest, Long> implements QuestDAO {
    
    public QuestDAOImpl(SessionFactory sessionFactory) {
        super(Quest.class, sessionFactory);
    }
    
    @Override
    public Optional<Quest> findById(Long id) {
        return executeInTransaction(session -> {
            String hql1 = "SELECT DISTINCT q FROM Quest q " +
                         "LEFT JOIN FETCH q.author " +
                         "LEFT JOIN FETCH q.stages " +
                         "WHERE q.id = :id";
            TypedQuery<Quest> query1 = session.createQuery(hql1, Quest.class);
            query1.setParameter("id", id);
            
            List<Quest> results = query1.getResultList();
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            Quest quest = results.get(0);
            
            if (!quest.getStages().isEmpty()) {
                String hql2 = "SELECT DISTINCT s FROM QuestStage s " +
                             "LEFT JOIN FETCH s.options o " +
                             "WHERE s.id IN :stageIds";
                TypedQuery<com.javarush.lepeshinskij.entity.QuestStage> query2 = 
                    session.createQuery(hql2, com.javarush.lepeshinskij.entity.QuestStage.class);
                
                List<Long> stageIds = quest.getStages().stream()
                    .map(com.javarush.lepeshinskij.entity.QuestStage::getId)
                    .collect(java.util.stream.Collectors.toList());
                
                query2.setParameter("stageIds", stageIds);
                query2.getResultList();
            }
            
            return Optional.of(quest);
        });
    }
    
    @Override
    public List<Quest> findByAuthor(User author) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE q.author = :author ORDER BY q.createdAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("author", author);
            query.setHint("org.hibernate.cacheable", true);
            query.setHint("org.hibernate.cacheRegion", "questsByAuthor");
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findByAuthorId(Long authorId) {
        return executeInTransaction(session -> {
            String hql1 = "SELECT DISTINCT q FROM Quest q " +
                         "LEFT JOIN FETCH q.author " +
                         "LEFT JOIN FETCH q.stages " +
                         "WHERE q.author.id = :authorId ORDER BY q.createdAt DESC";
            TypedQuery<Quest> query1 = session.createQuery(hql1, Quest.class);
            query1.setParameter("authorId", authorId);
            List<Quest> quests = query1.getResultList();
            
            if (!quests.isEmpty()) {
                String hql2 = "SELECT DISTINCT s FROM QuestStage s " +
                             "LEFT JOIN FETCH s.options o " +
                             "WHERE s.quest.author.id = :authorId";
                TypedQuery<com.javarush.lepeshinskij.entity.QuestStage> query2 = 
                    session.createQuery(hql2, com.javarush.lepeshinskij.entity.QuestStage.class);
                query2.setParameter("authorId", authorId);
                query2.getResultList();
            }
            
            return quests;
        });
    }
    
    @Override
    public List<Quest> findByAuthorUsername(String authorUsername) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE q.author.username = :authorUsername ORDER BY q.createdAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("authorUsername", authorUsername);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findByTitleContaining(String titlePart) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE LOWER(q.title) LIKE LOWER(:titlePart) ORDER BY q.title";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("titlePart", "%" + titlePart + "%");
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findByDescriptionContaining(String descriptionPart) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE LOWER(q.description) LIKE LOWER(:descriptionPart) ORDER BY q.title";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("descriptionPart", "%" + descriptionPart + "%");
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findByTitleOrDescriptionContaining(String searchTerm) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE LOWER(q.title) LIKE LOWER(:searchTerm) OR LOWER(q.description) LIKE LOWER(:searchTerm) ORDER BY q.title";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findByCreatedAtAfter(LocalDateTime date) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE q.createdAt > :date ORDER BY q.createdAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("date", date);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findByCreatedAtBefore(LocalDateTime date) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE q.createdAt < :date ORDER BY q.createdAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("date", date);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE q.createdAt BETWEEN :startDate AND :endDate ORDER BY q.createdAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findByUpdatedAtAfter(LocalDateTime date) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE q.updatedAt > :date ORDER BY q.updatedAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("date", date);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findAllOrderByCreatedAtDesc() {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q ORDER BY q.createdAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findAllOrderByUpdatedAtDesc() {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q ORDER BY q.updatedAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findAllOrderByTitle() {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q ORDER BY q.title";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findAllOrderByAuthorUsername() {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q ORDER BY q.author.username, q.title";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findAllOrderByCreatedAtDesc(int offset, int limit) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q ORDER BY q.createdAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findAllOrderByUpdatedAtDesc(int offset, int limit) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q ORDER BY q.updatedAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            return query.getResultList();
        });
    }
    
    @Override
    public long countByAuthor(User author) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(q) FROM Quest q WHERE q.author = :author";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("author", author);
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countByAuthorId(Long authorId) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(q) FROM Quest q WHERE q.author.id = :authorId";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("authorId", authorId);
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countByAuthorUsername(String authorUsername) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(q) FROM Quest q WHERE q.author.username = :authorUsername";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("authorUsername", authorUsername);
            return query.getSingleResult();
        });
    }
    
    @Override
    public List<Quest> findByMinStageCount(int minStages) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE SIZE(q.stages) >= :minStages ORDER BY SIZE(q.stages) DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("minStages", minStages);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findByMaxStageCount(int maxStages) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE SIZE(q.stages) <= :maxStages ORDER BY SIZE(q.stages) DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("maxStages", maxStages);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Quest> findByStageCountBetween(int minStages, int maxStages) {
        return executeInTransaction(session -> {
            String hql = "FROM Quest q WHERE SIZE(q.stages) BETWEEN :minStages AND :maxStages ORDER BY SIZE(q.stages) DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            query.setParameter("minStages", minStages);
            query.setParameter("maxStages", maxStages);
            return query.getResultList();
        });
    }
    
    @Override
    public Object[] getQuestStatistics() {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(q), COUNT(CASE WHEN SIZE(q.stages) > 0 THEN 1 END), AVG(SIZE(q.stages)) FROM Quest q";
            TypedQuery<Object[]> query = session.createQuery(hql, Object[].class);
            return query.getSingleResult();
        });
    }
    
    @Override
    public List<Quest> findAllWithAuthor() {
        return executeInTransaction(session -> {
            String hql1 = "SELECT DISTINCT q FROM Quest q " +
                         "LEFT JOIN FETCH q.author " +
                         "LEFT JOIN FETCH q.stages " +
                         "ORDER BY q.createdAt DESC";
            TypedQuery<Quest> query1 = session.createQuery(hql1, Quest.class);
            List<Quest> quests = query1.getResultList();
            
            if (!quests.isEmpty()) {
                String hql2 = "SELECT DISTINCT s FROM QuestStage s " +
                             "LEFT JOIN FETCH s.options o " +
                             "WHERE s.quest IN :quests";
                TypedQuery<com.javarush.lepeshinskij.entity.QuestStage> query2 = 
                    session.createQuery(hql2, com.javarush.lepeshinskij.entity.QuestStage.class);
                query2.setParameter("quests", quests);
                query2.getResultList();
            }
            
            return quests;
        });
    }
    
    @Override
    public List<Quest> findAllWithAuthorAndStages() {
        return executeInTransaction(session -> {
            String hql = "SELECT DISTINCT q FROM Quest q " +
                        "LEFT JOIN FETCH q.author " +
                        "LEFT JOIN FETCH q.stages " +
                        "ORDER BY q.createdAt DESC";
            TypedQuery<Quest> query = session.createQuery(hql, Quest.class);
            return query.getResultList();
        });
    }
}