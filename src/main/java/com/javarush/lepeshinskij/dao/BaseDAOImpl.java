package com.javarush.lepeshinskij.dao;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class BaseDAOImpl<T, ID extends Serializable> implements BaseDAO<T, ID> {
    
    private final Class<T> entityClass;
    
    private SessionFactory sessionFactory;
    
    protected BaseDAOImpl(Class<T> entityClass, SessionFactory sessionFactory) {
        this.entityClass = entityClass;
        this.sessionFactory = sessionFactory;
    }
    
    protected Class<T> getEntityClass() {
        return entityClass;
    }
    
    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
    
    protected <R> R executeInTransaction(TransactionCallback<R> callback) {
        Transaction transaction = null;
        Session session = sessionFactory.openSession();
        try {
            transaction = session.beginTransaction();
            R result = callback.execute(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }
    
    @FunctionalInterface
    protected interface TransactionCallback<R> {
        R execute(Session session);
    }
    
    @Override
    public T save(T entity) {
        return executeInTransaction(session -> {
            session.persist(entity);
            session.flush();
            return entity;
        });
    }
    
    @Override
    public T update(T entity) {
        return executeInTransaction(session -> (T) session.merge(entity));
    }
    
    @Override
    public T saveOrUpdate(T entity) {
        return executeInTransaction(session -> (T) session.merge(entity));
    }
    
    @Override
    public Optional<T> findById(ID id) {
        return executeInTransaction(session -> {
            T entity = session.get(entityClass, id);
            return Optional.ofNullable(entity);
        });
    }
    
    @Override
    public T getById(ID id) {
        return executeInTransaction(session -> session.get(entityClass, id));
    }
    
    @Override
    public List<T> findAll() {
        return executeInTransaction(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            
            TypedQuery<T> query = session.createQuery(cq);
            return query.getResultList();
        });
    }
    
    @Override
    public List<T> findAll(int offset, int limit) {
        return executeInTransaction(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            
            TypedQuery<T> query = session.createQuery(cq);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            
            return query.getResultList();
        });
    }
    
    @Override
    public long count() {
        return executeInTransaction(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> root = cq.from(entityClass);
            cq.select(cb.count(root));
            
            TypedQuery<Long> query = session.createQuery(cq);
            return query.getSingleResult();
        });
    }
    
    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
    
    @Override
    public boolean deleteById(ID id) {
        return executeInTransaction(session -> {
            T entity = session.get(entityClass, id);
            if (entity != null) {
                session.remove(entity);
                return true;
            }
            return false;
        });
    }
    
    @Override
    public void delete(T entity) {
        executeInTransaction(session -> {
            session.remove(entity);
            return null;
        });
    }
    
    @Override
    public int deleteAll() {
        return executeInTransaction(session -> {
            String hql = "DELETE FROM " + entityClass.getSimpleName();
            return session.createMutationQuery(hql).executeUpdate();
        });
    }
    
    @Override
    public void clear() {
        executeInTransaction(session -> {
            session.clear();
            return null;
        });
    }
    
    @Override
    public void flush() {
        executeInTransaction(session -> {
            session.flush();
            return null;
        });
    }
}
