package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO extends BaseDAO<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findAdmins();
    
    List<User> findRegularUsers();
    
    List<User> findByUsernameContaining(String usernamePart);
    
    List<User> findByEmailContaining(String emailPart);
    
    long countByRole(User.UserRole role);
    
    long countAdmins();
    
    long countRegularUsers();
    
    List<User> findByCreatedAtAfter(java.time.LocalDateTime date);
    
    List<User> findByCreatedAtBefore(java.time.LocalDateTime date);
    
    List<User> findByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
