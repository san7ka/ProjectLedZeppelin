package com.javarush.lepeshinskij.integration;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.QuestStageOption;
import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserQuestProgress;
import com.javarush.lepeshinskij.service.GameService;
import com.javarush.lepeshinskij.service.QuestService;
import com.javarush.lepeshinskij.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional
public class QuestGameIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private QuestService questService;

    @Autowired
    private GameService gameService;

    private User testUser;
    private Quest testQuest;
    private QuestStage startStage;
    private QuestStage winStage;
    private QuestStage loseStage;

    @BeforeEach
    void setUp() {
        testUser = userService.createUser("testuser", "test@example.com", "password123");
        assertNotNull(testUser);
        assertNotNull(testUser.getId());

        testQuest = questService.createQuest("Integration Test Quest", "A quest for integration testing", testUser);
        assertNotNull(testQuest);
        assertNotNull(testQuest.getId());

        startStage = questService.addStage(testQuest, "start", "Start Stage", "You are at the beginning", QuestStage.ResultType.NONE);
        winStage = questService.addStage(testQuest, "win", "Win Stage", "You win!", QuestStage.ResultType.WIN);
        loseStage = questService.addStage(testQuest, "lose", "Lose Stage", "You lose!", QuestStage.ResultType.LOSE);
        // questService.createQuestStageOption(startStage, "lose_choice", "Choose to lose", "lose");
    }

    @Test
    void testCompleteQuestFlow() {
        UserQuestProgress progress = gameService.startQuest(testUser, testQuest);
        assertNotNull(progress);
        assertNotNull(progress.getId());
        assertEquals(testUser.getId(), progress.getUser().getId());
        assertEquals(testQuest.getId(), progress.getQuest().getId());
        assertEquals("start", progress.getCurrentStageId());
        assertFalse(progress.getIsCompleted());
        assertNotNull(progress.getStartedAt());

        assertTrue(gameService.isUserPlayingQuest(testUser, testQuest));

        QuestStage currentStage = gameService.getCurrentStage(progress);
        assertNotNull(currentStage);
        assertEquals("start", currentStage.getStageId());

        List<String> availableChoices = gameService.getAvailableChoices(progress);
        assertNotNull(availableChoices);
        assertTrue(availableChoices.contains("win_choice"));
        assertTrue(availableChoices.contains("lose_choice"));

        boolean continues = gameService.makeChoice(progress, "win_choice");
        assertFalse(continues);
        assertEquals("win", progress.getCurrentStageId());
        assertTrue(progress.getIsCompleted());
        assertNotNull(progress.getCompletedAt());

        assertTrue(progress.getIsCompleted());
    }

    @Test
    void testLoseQuestFlow() {
        UserQuestProgress progress = gameService.startQuest(testUser, testQuest);
        assertNotNull(progress);

        boolean continues = gameService.makeChoice(progress, "lose_choice");
        assertFalse(continues);
        assertTrue(progress.getIsCompleted());
        assertNotNull(progress.getCompletedAt());
    }

    @Test
    void testUserCannotStartQuestTwice() {
        UserQuestProgress progress1 = gameService.startQuest(testUser, testQuest);
        assertNotNull(progress1);

        assertThrows(IllegalStateException.class, () -> {
            gameService.startQuest(testUser, testQuest);
        });
    }

    @Test
    void testGetUserQuestProgress() {
        UserQuestProgress progress = gameService.startQuest(testUser, testQuest);

        Optional<UserQuestProgress> foundProgress = gameService.getProgress(testUser, testQuest);
        assertTrue(foundProgress.isPresent());
        assertEquals(progress.getId(), foundProgress.get().getId());

        List<UserQuestProgress> userQuests = gameService.getUserQuestProgress(testUser.getId());
        assertFalse(userQuests.isEmpty());
        assertTrue(userQuests.stream().anyMatch(p -> p.getId().equals(progress.getId())));
    }

    @Test
    void testQuestStatistics() {
        UserQuestProgress progress = gameService.startQuest(testUser, testQuest);
        gameService.makeChoice(progress, "win_choice");

        var questStats = gameService.getQuestStatistics(testQuest);
        assertNotNull(questStats);

        var userStats = gameService.getUserGameStatistics(testUser);
        assertNotNull(userStats);
    }

    @Test
    void testMultipleUsersPlaySameQuest() {
        User user2 = userService.createUser("user2", "user2@example.com", "password123");
        assertNotNull(user2);

        UserQuestProgress progress1 = gameService.startQuest(testUser, testQuest);
        UserQuestProgress progress2 = gameService.startQuest(user2, testQuest);

        assertNotNull(progress1);
        assertNotNull(progress2);
        assertNotEquals(progress1.getId(), progress2.getId());

        assertTrue(gameService.isUserPlayingQuest(testUser, testQuest));
        assertTrue(gameService.isUserPlayingQuest(user2, testQuest));

        gameService.makeChoice(progress1, "win_choice");
        gameService.makeChoice(progress2, "lose_choice");

        Optional<UserQuestProgress> finalProgress1 = gameService.getProgress(testUser, testQuest);
        Optional<UserQuestProgress> finalProgress2 = gameService.getProgress(user2, testQuest);

        assertTrue(finalProgress1.isPresent());
        assertTrue(finalProgress2.isPresent());
        assertTrue(finalProgress1.get().getIsCompleted());
        assertTrue(finalProgress2.get().getIsCompleted());
        assertEquals("win", finalProgress1.get().getCurrentStageId());
        assertEquals("lose", finalProgress2.get().getCurrentStageId());
    }

    @Test
    void testQuestWithMultipleStages() {
        Quest multiStageQuest = questService.createQuest("Multi Stage Quest", "A quest with multiple stages", testUser);

        QuestStage stage1 = questService.addStage(multiStageQuest, "stage1", "Stage 1", "First stage", QuestStage.ResultType.NONE);
        QuestStage stage2 = questService.addStage(multiStageQuest, "stage2", "Stage 2", "Second stage", QuestStage.ResultType.NONE);
        QuestStage finalStage = questService.addStage(multiStageQuest, "final", "Final Stage", "Final stage", QuestStage.ResultType.WIN);
        UserQuestProgress progress = gameService.startQuest(testUser, multiStageQuest);
        assertEquals("stage1", progress.getCurrentStageId());

        boolean continues = gameService.makeChoice(progress, "to_stage2");
        assertTrue(continues);
        assertEquals("stage2", progress.getCurrentStageId());
        assertFalse(progress.getIsCompleted());

        continues = gameService.makeChoice(progress, "to_final");
        assertFalse(continues);
        assertEquals("final", progress.getCurrentStageId());
        assertTrue(progress.getIsCompleted());
    }

    @Test
    void testQuestValidation() {
        Quest invalidQuest = new Quest();
        invalidQuest.setTitle("");
        invalidQuest.setDescription("Valid description");
        invalidQuest.setAuthor(testUser);
    }

    @Test
    void testUserValidation() {
        User invalidUser = new User();
        invalidUser.setUsername("ab");
        invalidUser.setEmail("invalid-email");
        invalidUser.setPassword("123");
    }

    @Test
    void testQuestStageValidation() {
        QuestStage invalidStage = new QuestStage();
        invalidStage.setStageId("");
        invalidStage.setTitle("Valid title");
        invalidStage.setDescription("Valid description");
        invalidStage.setQuest(testQuest);
        invalidStage.setStageIndex(1);
        invalidStage.setResultType(QuestStage.ResultType.NONE);
    }
}
