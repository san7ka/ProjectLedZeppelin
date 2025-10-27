package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.User;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl extends BaseDAOImpl<User, Long> implements UserDAO {
    
    public UserDAOImpl(SessionFactory sessionFactory) {
        super(User.class, sessionFactory);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return executeInTransaction(session -> {
            String hql = "FROM User u WHERE u.username = :username";
            TypedQuery<User> query = session.createQuery(hql, User.class);
            query.setParameter("username", username);
            query.setHint("org.hibernate.cacheable", true);
            query.setHint("org.hibernate.cacheRegion", "userByUsername");
            
            List<User> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return executeInTransaction(session -> {
            String hql = "FROM User u WHERE u.email = :email";
            TypedQuery<User> query = session.createQuery(hql, User.class);
            query.setParameter("email", email);
            query.setHint("org.hibernate.cacheable", true);
            query.setHint("org.hibernate.cacheRegion", "userByEmail");
            
            List<User> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
    
    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return executeInTransaction(session -> {
            String hql = "FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail";
            TypedQuery<User> query = session.createQuery(hql, User.class);
            query.setParameter("usernameOrEmail", usernameOrEmail);
            
            List<User> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(u) FROM User u WHERE u.username = :username";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("username", username);
            
            return query.getSingleResult() > 0;
        });
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(u) FROM User u WHERE u.email = :email";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("email", email);
            
            return query.getSingleResult() > 0;
        });
    }
    
    @Override
    public List<User> findByRole(User.UserRole role) {
        return executeInTransaction(session -> {
            String hql = "FROM User u WHERE u.role = :role ORDER BY u.createdAt DESC";
            TypedQuery<User> query = session.createQuery(hql, User.class);
            query.setParameter("role", role);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<User> findAdmins() {
        return findByRole(User.UserRole.ADMIN);
    }
    
    @Override
    public List<User> findRegularUsers() {
        return findByRole(User.UserRole.USER);
    }
    
    @Override
    public List<User> findByUsernameContaining(String usernamePart) {
        return executeInTransaction(session -> {
            String hql = "FROM User u WHERE u.username LIKE :usernamePart ORDER BY u.username";
            TypedQuery<User> query = session.createQuery(hql, User.class);
            query.setParameter("usernamePart", "%" + usernamePart + "%");
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<User> findByEmailContaining(String emailPart) {
        return executeInTransaction(session -> {
            String hql = "FROM User u WHERE u.email LIKE :emailPart ORDER BY u.email";
            TypedQuery<User> query = session.createQuery(hql, User.class);
            query.setParameter("emailPart", "%" + emailPart + "%");
            
            return query.getResultList();
        });
    }
    
    @Override
    public long countByRole(User.UserRole role) {
        return executeInTransaction(session -> {
            String hql = "SELECT COUNT(u) FROM User u WHERE u.role = :role";
            TypedQuery<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("role", role);
            
            return query.getSingleResult();
        });
    }
    
    @Override
    public long countAdmins() {
        return countByRole(User.UserRole.ADMIN);
    }
    
    @Override
    public long countRegularUsers() {
        return countByRole(User.UserRole.USER);
    }
    
    @Override
    public List<User> findByCreatedAtAfter(LocalDateTime date) {
        return executeInTransaction(session -> {
            String hql = "FROM User u WHERE u.createdAt > :date ORDER BY u.createdAt DESC";
            TypedQuery<User> query = session.createQuery(hql, User.class);
            query.setParameter("date", date);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<User> findByCreatedAtBefore(LocalDateTime date) {
        return executeInTransaction(session -> {
            String hql = "FROM User u WHERE u.createdAt < :date ORDER BY u.createdAt DESC";
            TypedQuery<User> query = session.createQuery(hql, User.class);
            query.setParameter("date", date);
            
            return query.getResultList();
        });
    }
    
    @Override
    public List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return executeInTransaction(session -> {
            String hql = "FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate ORDER BY u.createdAt DESC";
            TypedQuery<User> query = session.createQuery(hql, User.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            
            return query.getResultList();
        });
    }
}
