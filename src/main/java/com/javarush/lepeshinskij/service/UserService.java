package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.dao.UserDAO;
import com.javarush.lepeshinskij.dao.UserDAOImpl;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.util.PasswordEncoder;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private UserDAO userDAO;
    private PasswordEncoder passwordEncoder;
    
    public UserService(SessionFactory sessionFactory) {
        this.userDAO = new UserDAOImpl(sessionFactory);
        this.passwordEncoder = new PasswordEncoder();
    }
    
    public UserService(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }
    
    public User createUser(String username, String email, String password) {
        logger.info("Создание нового пользователя: {}", username);
        
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь с именем '" + username + "' уже существует");
        }
        
        if (userDAO.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Пользователь с email '" + email + "' уже существует");
        }
        
        User user = new User(username, email, passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userDAO.save(user);
        logger.info("Пользователь успешно создан: {} (ID: {})", username, savedUser.getId());
        
        return savedUser;
    }
    
    public User createUser(User user) {
        logger.info("Создание нового пользователя: {}", user.getUsername());
        
        if (userDAO.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с именем " + user.getUsername() + " уже существует");
        }
        
        if (userDAO.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с email " + user.getEmail() + " уже существует");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        User savedUser = userDAO.save(user);
        logger.info("Пользователь успешно создан: {} (ID: {})", user.getUsername(), savedUser.getId());
        
        return savedUser;
    }
    
    public Optional<User> getUserById(Long userId) {
        return userDAO.findById(userId);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userDAO.findByEmail(email);
    }
    
    public Optional<User> authenticate(String usernameOrEmail, String password) {
        logger.info("Попытка аутентификации пользователя: {}", usernameOrEmail);
        
        Optional<User> userOpt = userDAO.findByUsernameOrEmail(usernameOrEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                logger.info("Пользователь успешно аутентифицирован: {}", user.getUsername());
                return Optional.of(user);
            } else {
                logger.warn("Неверный пароль для пользователя: {}", usernameOrEmail);
            }
        } else {
            logger.warn("Пользователь не найден: {}", usernameOrEmail);
        }
        
        return Optional.empty();
    }
    
    public User updateUser(User user) {
        logger.info("Обновление пользователя: {} (ID: {})", user.getUsername(), user.getId());
        
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userDAO.update(user);
        logger.info("Пользователь успешно обновлен: {}", user.getUsername());
        
        return updatedUser;
    }
    
    public void changePassword(User user, String newPassword) {
        logger.info("Изменение пароля для пользователя: {}", user.getUsername());
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userDAO.update(user);
        
        logger.info("Пароль успешно изменен для пользователя: {}", user.getUsername());
    }
    
    public boolean checkPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    public void deleteUser(Long userId) {
        logger.info("Удаление пользователя с ID: {}", userId);
        
        userDAO.deleteById(userId);
        logger.info("Пользователь с ID {} успешно удален", userId);
    }
    
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    
    public List<User> getAdmins() {
        return userDAO.findByRole(User.UserRole.ADMIN);
    }
    
    public List<User> getUsers() {
        return userDAO.findByRole(User.UserRole.USER);
    }
    
    public void changeUserRole(Long userId, String newRole) {
        logger.info("Изменение роли пользователя с ID {} на {}", userId, newRole);
        
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + userId + " не найден"));
        
        user.setRole(User.UserRole.valueOf(newRole));
        user.setUpdatedAt(LocalDateTime.now());
        userDAO.update(user);
        
        logger.info("Роль пользователя {} успешно изменена на {}", user.getUsername(), newRole);
    }
    
    public Object[] getUserStatistics() {
        long totalUsers = userDAO.count();
        long adminCount = userDAO.countByRole(User.UserRole.ADMIN);
        long userCount = userDAO.countByRole(User.UserRole.USER);
        
        return new Object[]{totalUsers, adminCount, userCount};
    }
}