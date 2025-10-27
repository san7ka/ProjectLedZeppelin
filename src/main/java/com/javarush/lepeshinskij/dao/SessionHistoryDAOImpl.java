package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.SessionHistory;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SessionHistoryDAOImpl extends BaseDAOImpl<SessionHistory, Long> implements SessionHistoryDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionHistoryDAOImpl.class);
    
    public SessionHistoryDAOImpl(SessionFactory sessionFactory) {
        super(SessionHistory.class, sessionFactory);
    }
    
    @Override
    public List<SessionHistory> findBySessionOrderByStepNumber(UserQuestProgress session) {
        logger.debug("Получение истории для сессии ID: {}", session.getId());
        
        return executeInTransaction(hibernateSession -> {
            Query<SessionHistory> query = hibernateSession.createQuery(
                "SELECT sh FROM SessionHistory sh " +
                "WHERE sh.session = :session " +
                "ORDER BY sh.stepNumber ASC",
                SessionHistory.class
            );
            query.setParameter("session", session);
            return query.getResultList();
        });
    }
    
    @Override
    public List<SessionHistory> findBySessionIdOrderByStepNumber(Long sessionId) {
        logger.debug("Получение истории для сессии ID: {}", sessionId);
        
        return executeInTransaction(hibernateSession -> {
            Query<SessionHistory> query = hibernateSession.createQuery(
                "SELECT sh FROM SessionHistory sh " +
                "WHERE sh.session.id = :sessionId " +
                "ORDER BY sh.stepNumber ASC",
                SessionHistory.class
            );
            query.setParameter("sessionId", sessionId);
            return query.getResultList();
        });
    }
    
    @Override
    public SessionHistory findLastBySession(UserQuestProgress session) {
        logger.debug("Получение последней записи истории для сессии ID: {}", session.getId());
        
        return executeInTransaction(hibernateSession -> {
            Query<SessionHistory> query = hibernateSession.createQuery(
                "SELECT sh FROM SessionHistory sh " +
                "WHERE sh.session = :session " +
                "ORDER BY sh.stepNumber DESC",
                SessionHistory.class
            );
            query.setParameter("session", session);
            query.setMaxResults(1);
            
            List<SessionHistory> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        });
    }
    
    @Override
    public long countBySession(UserQuestProgress session) {
        logger.debug("Подсчет шагов для сессии ID: {}", session.getId());
        
        return executeInTransaction(hibernateSession -> {
            Query<Long> query = hibernateSession.createQuery(
                "SELECT COUNT(sh) FROM SessionHistory sh WHERE sh.session = :session",
                Long.class
            );
            query.setParameter("session", session);
            return query.getSingleResult();
        });
    }
    
    @Override
    public void deleteBySession(UserQuestProgress session) {
        logger.info("Удаление всей истории для сессии ID: {}", session.getId());
        
        executeInTransaction(hibernateSession -> {
            var query = hibernateSession.createMutationQuery(
                "DELETE FROM SessionHistory sh WHERE sh.session = :session"
            );
            query.setParameter("session", session);
            int deletedCount = query.executeUpdate();
            logger.info("Удалено {} записей истории", deletedCount);
            return null;
        });
    }
    
    @Override
    public void deleteLastNSteps(UserQuestProgress session, int count) {
        logger.info("Удаление последних {} шагов для сессии ID: {}", count, session.getId());
        
        executeInTransaction(hibernateSession -> {
            Query<SessionHistory> selectQuery = hibernateSession.createQuery(
                "SELECT sh FROM SessionHistory sh " +
                "WHERE sh.session = :session " +
                "ORDER BY sh.stepNumber DESC",
                SessionHistory.class
            );
            selectQuery.setParameter("session", session);
            selectQuery.setMaxResults(count);
            
            List<SessionHistory> historyToDelete = selectQuery.getResultList();
            
            for (SessionHistory history : historyToDelete) {
                hibernateSession.remove(history);
            }
            
            logger.info("Удалено {} записей истории", historyToDelete.size());
            return null;
        });
    }
    
    @Override
    public boolean canGoBack(UserQuestProgress session) {
        long count = countBySession(session);
        return count > 1;
    }
}
