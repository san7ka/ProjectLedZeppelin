package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserStatistics;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class UserStatisticsDAOImpl implements UserStatisticsDAO {

    private final SessionFactory sessionFactory;

    public UserStatisticsDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    
    private static final Logger logger = LoggerFactory.getLogger(UserStatisticsDAOImpl.class);
    
    @Override
    public Optional<UserStatistics> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            UserStatistics statistics = session.get(UserStatistics.class, id);
            return Optional.ofNullable(statistics);
        } catch (Exception e) {
            logger.error("Ошибка при поиске статистики по ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<UserStatistics> findByUser(User user) {
        if (user == null || user.getId() == null) {
            return Optional.empty();
        }
        try (Session session = sessionFactory.openSession()) {
            Query<UserStatistics> query = session.createQuery(
                "FROM UserStatistics WHERE user.id = :userId", UserStatistics.class);
            query.setParameter("userId", user.getId());
            UserStatistics statistics = query.uniqueResult();
            return Optional.ofNullable(statistics);
        } catch (Exception e) {
            logger.error("Ошибка при поиске статистики для пользователя с ID: {}", user.getId(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public UserStatistics save(UserStatistics statistics) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(statistics);
            transaction.commit();
            logger.info("Статистика сохранена для пользователя с ID: {}", 
                statistics.getUser().getId());
            return statistics;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при сохранении статистики", e);
            throw e;
        }
    }
    
    @Override
    public UserStatistics update(UserStatistics statistics) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            UserStatistics merged = session.merge(statistics);
            transaction.commit();
            logger.info("Статистика обновлена для пользователя с ID: {}", 
                statistics.getUser().getId());
            return merged;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при обновлении статистики", e);
            throw e;
        }
    }
    
    @Override
    public void deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            UserStatistics statistics = session.get(UserStatistics.class, id);
            if (statistics != null) {
                session.remove(statistics);
                logger.info("Статистика удалена для пользователя с ID: {}", 
                    statistics.getUser().getId());
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при удалении статистики с ID: {}", id, e);
            throw e;
        }
    }
    
    @Override
    public UserStatistics getOrCreate(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Пользователь не может быть null или иметь null ID");
        }
        
        Optional<UserStatistics> existingStats = findByUser(user);
        if (existingStats.isPresent()) {
            return existingStats.get();
        }
        
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            
            User managedUser = session.get(User.class, user.getId());
            if (managedUser == null) {
                throw new IllegalArgumentException("Пользователь с ID " + user.getId() + " не найден");
            }
            
            UserStatistics newStats = new UserStatistics(managedUser);
            session.persist(newStats);
            transaction.commit();
            
            logger.info("Статистика создана для пользователя: {}", managedUser.getUsername());
            return newStats;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при создании статистики для пользователя с ID: {}", user.getId(), e);
            throw e;
        }
    }
}
