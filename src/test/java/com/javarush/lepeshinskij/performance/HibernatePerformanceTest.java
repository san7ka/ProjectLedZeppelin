package com.javarush.lepeshinskij.performance;

import com.javarush.lepeshinskij.dao.QuestDAO;
import com.javarush.lepeshinskij.dao.UserDAO;
import com.javarush.lepeshinskij.dao.UserQuestProgressDAO;
import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional
public class HibernatePerformanceTest {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private QuestDAO questDAO;

    @Autowired
    private UserQuestProgressDAO userQuestProgressDAO;

    private List<User> testUsers;
    private List<Quest> testQuests;

    @BeforeEach
    void setUp() {
        testUsers = new ArrayList<>();
        testQuests = new ArrayList<>();
    }

    @Test
    void testBatchInsertUsers() {
        // Given
        int userCount = 100;
        List<User> users = new ArrayList<>();

        for (int i = 0; i < userCount; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPassword("password" + i);
            user.setRole(User.UserRole.USER);
            users.add(user);
        }

        // When
        long startTime = System.currentTimeMillis();
        for (User user : users) {
            userDAO.save(user);
        }
        long endTime = System.currentTimeMillis();

        // Then
        long executionTime = endTime - startTime;
        System.out.println("Batch insert of " + userCount + " users took: " + executionTime + "ms");
        
        assertEquals(userCount, userDAO.count());
        
        assertTrue(executionTime < 5000, "Batch insert took too long: " + executionTime + "ms");
    }

    @Test
    void testBatchInsertQuests() {
        // Given
        User author = userDAO.save(createTestUser("author", "author@example.com"));
        int questCount = 50;
        List<Quest> quests = new ArrayList<>();

        for (int i = 0; i < questCount; i++) {
            Quest quest = new Quest();
            quest.setTitle("Quest " + i);
            quest.setDescription("Description for quest " + i);
            quest.setAuthor(author);
            quests.add(quest);
        }

        // When
        long startTime = System.currentTimeMillis();
        for (Quest quest : quests) {
            questDAO.save(quest);
        }
        long endTime = System.currentTimeMillis();

        // Then
        long executionTime = endTime - startTime;
        System.out.println("Batch insert of " + questCount + " quests took: " + executionTime + "ms");
        
        assertEquals(questCount, questDAO.count());
        
        assertTrue(executionTime < 3000, "Batch insert took too long: " + executionTime + "ms");
    }

    @Test
    void testConcurrentUserCreation() throws InterruptedException {
        // Given
        int threadCount = 10;
        int usersPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // When
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < usersPerThread; j++) {
                    User user = createTestUser("user_" + threadId + "_" + j, "user_" + threadId + "_" + j + "@example.com");
                    userDAO.save(user);
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();

        // Then
        long executionTime = endTime - startTime;
        int totalUsers = threadCount * usersPerThread;
        
        System.out.println("Concurrent creation of " + totalUsers + " users took: " + executionTime + "ms");
        
        assertEquals(totalUsers, userDAO.count());
        
        assertTrue(executionTime < 10000, "Concurrent creation took too long: " + executionTime + "ms");
    }

    @Test
    void testQueryPerformance() {
        // Given
        User author = userDAO.save(createTestUser("author", "author@example.com"));
        
        for (int i = 0; i < 100; i++) {
            Quest quest = new Quest();
            quest.setTitle("Quest " + i);
            quest.setDescription("Description " + i);
            quest.setAuthor(author);
            questDAO.save(quest);
        }

        // When
        long startTime = System.currentTimeMillis();
        
        var allQuests = questDAO.findAll();
        var authorQuests = questDAO.findByAuthor(author);
        var questsByTitle = questDAO.findByTitleContaining("Quest");
        
        long endTime = System.currentTimeMillis();

        // Then
        long executionTime = endTime - startTime;
        System.out.println("Query execution took: " + executionTime + "ms");
        
        assertEquals(100, allQuests.size());
        assertEquals(100, authorQuests.size());
        assertEquals(100, questsByTitle.size());
        
        assertTrue(executionTime < 1000, "Queries took too long: " + executionTime + "ms");
    }

    @Test
    void testLazyLoadingPerformance() {
        // Given
        User author = userDAO.save(createTestUser("author", "author@example.com"));
        Quest quest = new Quest();
        quest.setTitle("Test Quest");
        quest.setDescription("Test Description");
        quest.setAuthor(author);
        quest = questDAO.save(quest);

        for (int i = 0; i < 50; i++) {
            User user = userDAO.save(createTestUser("user" + i, "user" + i + "@example.com"));
            UserQuestProgress progress = new UserQuestProgress();
            progress.setUser(user);
            progress.setQuest(quest);
            progress.setCurrentStageId("stage1");
            progress.setIsCompleted(false);
            progress.setStartedAt(LocalDateTime.now());
            userQuestProgressDAO.save(progress);
        }

        // When
        long startTime = System.currentTimeMillis();
        
        var foundQuest = questDAO.findById(quest.getId());
        assertTrue(foundQuest.isPresent());
        
        var questProgress = userQuestProgressDAO.findByQuest(quest);
        
        long endTime = System.currentTimeMillis();

        // Then
        long executionTime = endTime - startTime;
        System.out.println("Lazy loading took: " + executionTime + "ms");
        
        assertEquals(50, questProgress.size());
        
        assertTrue(executionTime < 500, "Lazy loading took too long: " + executionTime + "ms");
    }

    @Test
    void testCachePerformance() {
        // Given
        User user = userDAO.save(createTestUser("cacheuser", "cache@example.com"));

        // When
        long startTime = System.currentTimeMillis();
        
        var user1 = userDAO.findByUsername("cacheuser");
        
        var user2 = userDAO.findByUsername("cacheuser");
        
        long endTime = System.currentTimeMillis();

        // Then
        long executionTime = endTime - startTime;
        System.out.println("Cache performance test took: " + executionTime + "ms");
        
        assertTrue(user1.isPresent());
        assertTrue(user2.isPresent());
        assertEquals(user1.get().getId(), user2.get().getId());
        
        assertTrue(executionTime < 1000, "Cache performance test took too long: " + executionTime + "ms");
    }

    @Test
    void testMemoryUsage() {
        // Given
        int userCount = 1000;
        List<User> users = new ArrayList<>();

        // When
        long startTime = System.currentTimeMillis();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        for (int i = 0; i < userCount; i++) {
            User user = createTestUser("memuser" + i, "memuser" + i + "@example.com");
            users.add(userDAO.save(user));
        }
        
        long endTime = System.currentTimeMillis();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Then
        long executionTime = endTime - startTime;
        long memoryUsed = endMemory - startMemory;
        
        System.out.println("Memory usage test:");
        System.out.println("  Execution time: " + executionTime + "ms");
        System.out.println("  Memory used: " + (memoryUsed / 1024 / 1024) + "MB");
        System.out.println("  Users created: " + userCount);
        
        assertEquals(userCount, userDAO.count());
        
        assertTrue(memoryUsed < 100 * 1024 * 1024, "Memory usage too high: " + (memoryUsed / 1024 / 1024) + "MB");
    }

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(User.UserRole.USER);
        return user;
    }
}
