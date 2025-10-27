package com.javarush.lepeshinskij.service;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.QuestStageOption;
import com.javarush.lepeshinskij.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuestValidator Tests")
class QuestValidatorTest {
    
    private QuestValidator validator;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        validator = new QuestValidator();
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
    }
    
    @Test
    @DisplayName("Валидация квеста без узлов должна вернуть ошибку")
    void testValidateEmptyQuest() {
        Quest quest = new Quest("Empty Quest", "No stages", testUser);
        
        QuestValidator.ValidationResult result = validator.validateQuest(quest);
        
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("не содержит узлов")));
    }
    
    @Test
    @DisplayName("Валидация квеста без стартового узла должна вернуть ошибку")
    void testValidateQuestWithoutStartNode() {
        Quest quest = createBasicQuest();
        
        QuestStage stage1 = new QuestStage("stage1", "Stage 1", "Description", QuestStage.ResultType.NONE);
        quest.addStage(stage1);
        
        QuestValidator.ValidationResult result = validator.validateQuest(quest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("стартовый узел")));
    }
    
    @Test
    @DisplayName("Валидация квеста без WIN узлов должна вернуть ошибку")
    void testValidateQuestWithoutWinNodes() {
        Quest quest = createBasicQuest();
        
        QuestStage start = new QuestStage("start", "Start", "Start stage", QuestStage.ResultType.NONE);
        QuestStage lose = new QuestStage("lose", "Lose", "Lose stage", QuestStage.ResultType.LOSE);
        
        quest.addStage(start);
        quest.addStage(lose);
        quest.setStartStageId("start");
        
        QuestValidator.ValidationResult result = validator.validateQuest(quest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("WIN")));
    }
    
    @Test
    @DisplayName("Валидация узла без выборов должна вернуть ошибку")
    void testValidateNodeWithoutChoices() {
        Quest quest = createBasicQuest();
        
        QuestStage start = new QuestStage("start", "Start", "Start stage", QuestStage.ResultType.NONE);
        QuestStage win = new QuestStage("win", "Win", "Win stage", QuestStage.ResultType.WIN);
        
        quest.addStage(start);
        quest.addStage(win);
        quest.setStartStageId("start");
        
        QuestValidator.ValidationResult result = validator.validateQuest(quest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("не имеет выборов")));
    }
    
    @Test
    @DisplayName("Валидация корректного простого квеста должна пройти успешно")
    void testValidateCorrectSimpleQuest() {
        Quest quest = createValidSimpleQuest();
        
        QuestValidator.ValidationResult result = validator.validateQuest(quest);
        
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }
    
    @Test
    @DisplayName("Валидация квеста с недостижимым узлом должна вернуть предупреждение")
    void testValidateQuestWithUnreachableNode() {
        Quest quest = createBasicQuest();
        
        QuestStage start = new QuestStage("start", "Start", "Start", QuestStage.ResultType.NONE);
        QuestStage win = new QuestStage("win", "Win", "Win", QuestStage.ResultType.WIN);
        QuestStage unreachable = new QuestStage("unreachable", "Unreachable", "Cannot reach", QuestStage.ResultType.NONE);
        
        quest.addStage(start);
        quest.addStage(win);
        quest.addStage(unreachable);
        quest.setStartStageId("start");
        
        QuestStageOption option = new QuestStageOption(start, "toWin", "Go to win", "win");
        start.addOption(option);
        
        QuestValidator.ValidationResult result = validator.validateQuest(quest);
        
        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("недостижим")));
    }
    
    @Test
    @DisplayName("Валидация квеста без пути к WIN должна вернуть ошибку")
    void testValidateQuestWithoutPathToWin() {
        Quest quest = createBasicQuest();
        
        QuestStage start = new QuestStage("start", "Start", "Start", QuestStage.ResultType.NONE);
        QuestStage lose = new QuestStage("lose", "Lose", "Lose", QuestStage.ResultType.LOSE);
        QuestStage win = new QuestStage("win", "Win", "Win", QuestStage.ResultType.WIN);
        
        quest.addStage(start);
        quest.addStage(lose);
        quest.addStage(win);
        quest.setStartStageId("start");
        
        QuestStageOption option = new QuestStageOption(start, "toLose", "Go to lose", "lose");
        start.addOption(option);
        
        QuestValidator.ValidationResult result = validator.validateQuest(quest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("пути") && error.contains("WIN")));
    }
    
    @Test
    @DisplayName("Валидация выбора с несуществующим целевым узлом должна вернуть ошибку")
    void testValidateChoiceWithInvalidTarget() {
        Quest quest = createBasicQuest();
        
        QuestStage start = new QuestStage("start", "Start", "Start", QuestStage.ResultType.NONE);
        QuestStage win = new QuestStage("win", "Win", "Win", QuestStage.ResultType.WIN);
        
        quest.addStage(start);
        quest.addStage(win);
        quest.setStartStageId("start");
        
        QuestStageOption invalidOption = new QuestStageOption(start, "invalid", "Invalid choice", "nonexistent");
        start.addOption(invalidOption);
        
        QuestValidator.ValidationResult result = validator.validateQuest(quest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("несуществующий узел")));
    }
    
    @Test
    @DisplayName("Проверка canPublish должна вернуть true для корректного квеста")
    void testCanPublishValidQuest() {
        Quest quest = createValidSimpleQuest();
        
        boolean canPublish = validator.canPublish(quest);
        
        assertTrue(canPublish);
    }
    
    @Test
    @DisplayName("Проверка canPublish должна вернуть false для некорректного квеста")
    void testCanPublishInvalidQuest() {
        Quest quest = new Quest("Invalid Quest", "No stages", testUser);
        
        boolean canPublish = validator.canPublish(quest);
        
        assertFalse(canPublish);
    }
    
    @Test
    @DisplayName("validateAndPublish должен выбросить исключение для некорректного квеста")
    void testValidateAndPublishInvalidQuest() {
        Quest quest = new Quest("Invalid Quest", "No stages", testUser);
        
        assertThrows(IllegalStateException.class, () -> {
            validator.validateAndPublish(quest);
        });
    }
    
    @Test
    @DisplayName("validateAndPublish не должен выбросить исключение для корректного квеста")
    void testValidateAndPublishValidQuest() {
        Quest quest = createValidSimpleQuest();
        
        assertDoesNotThrow(() -> {
            validator.validateAndPublish(quest);
        });
    }
    
    @Test
    @DisplayName("Конечный узел с выборами должен вернуть предупреждение")
    void testEndNodeWithChoices() {
        Quest quest = createBasicQuest();
        
        QuestStage start = new QuestStage("start", "Start", "Start", QuestStage.ResultType.NONE);
        QuestStage win = new QuestStage("win", "Win", "Win", QuestStage.ResultType.WIN);
        
        quest.addStage(start);
        quest.addStage(win);
        quest.setStartStageId("start");
        
        QuestStageOption toWin = new QuestStageOption(start, "toWin", "Go to win", "win");
        start.addOption(toWin);
        
        QuestStageOption unusedChoice = new QuestStageOption(win, "unused", "Unused", "start");
        win.addOption(unusedChoice);
        
        QuestValidator.ValidationResult result = validator.validateQuest(quest);
        
        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("конечный узел") && warning.contains("выборы")));
    }
    
    // Helper methods
    
    private Quest createBasicQuest() {
        return new Quest("Test Quest", "Test Description", testUser);
    }
    
    private Quest createValidSimpleQuest() {
        Quest quest = createBasicQuest();
        
        QuestStage start = new QuestStage("start", "Start", "You wake up", QuestStage.ResultType.NONE);
        QuestStage room = new QuestStage("room", "Room", "Two doors ahead", QuestStage.ResultType.NONE);
        QuestStage win = new QuestStage("win", "Victory", "You escaped!", QuestStage.ResultType.WIN);
        QuestStage lose = new QuestStage("lose", "Defeat", "Game over", QuestStage.ResultType.LOSE);
        
        quest.addStage(start);
        quest.addStage(room);
        quest.addStage(win);
        quest.addStage(lose);
        quest.setStartStageId("start");
        
        QuestStageOption startToRoom = new QuestStageOption(start, "forward", "Go forward", "room");
        start.addOption(startToRoom);
        
        QuestStageOption roomToWin = new QuestStageOption(room, "left", "Left door", "win");
        QuestStageOption roomToLose = new QuestStageOption(room, "right", "Right door", "lose");
        room.addOption(roomToWin);
        room.addOption(roomToLose);
        
        return quest;
    }
}
