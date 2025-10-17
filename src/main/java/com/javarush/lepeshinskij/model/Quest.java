package com.javarush.lepeshinskij.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Quest {
    private static final Logger logger = Logger.getLogger(Quest.class.getName());
    
    public static final String START = "start";
    public static final String WIN = "win";
    public static final String LOSE = "lose";

    private final Map<String, QuestStage> stages = new LinkedHashMap<>();
    private final Map<String, String> transitions = new HashMap<>();
    
    private static final String QUEST_DATA_FILE = System.getProperty("user.home") + "/.quest_data_v2.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final boolean enablePersistence;

    public Quest() {
        this(true);
    }
    
    public Quest(boolean enablePersistence) {
        this.enablePersistence = enablePersistence && !isTestEnvironment();
        if (this.enablePersistence && loadFromFile()) {
            // Quest loaded from file
        } else {
            initializeDefaultQuest();
            reindexStages();
            if (this.enablePersistence) {
                saveToFile();
            }
        }
    }
    
    private boolean isTestEnvironment() {
        // Check if we're running in a test environment
        try {
            Class.forName("org.junit.jupiter.api.Test");
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if (element.getClassName().endsWith("Test")) {
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {
            // JUnit not on classpath, not a test
        }
        return false;
    }

    public QuestStage getStage(String stageId) {
        return stages.get(stageId);
    }

    public Map<String, QuestStage> getStages() {
        return new LinkedHashMap<>(stages);
    }

    public String getStartStageId() {
        return START;
    }

    public void reorderStages(List<String> stageOrder) {
        if (stageOrder == null || stageOrder.isEmpty()) {
            return;
        }
        
        Map<String, QuestStage> newStages = new LinkedHashMap<>();
        
        for (String stageId : stageOrder) {
            if (stages.containsKey(stageId)) {
                newStages.put(stageId, stages.get(stageId));
            }
        }
        
        for (Map.Entry<String, QuestStage> entry : stages.entrySet()) {
            if (!newStages.containsKey(entry.getKey())) {
                newStages.put(entry.getKey(), entry.getValue());
            }
        }
        
        stages.clear();
        stages.putAll(newStages);
        
        reindexStages();
        saveToFile();
    }

    public void addStage(String stageId, QuestStage stage) {
        logger.info("Adding stage: " + stageId + " with options: " + stage.getOptions());
        stages.put(stageId, stage);
        updateTransitionsForStage(stageId, stage);
        reindexStages();
        logger.info("Total stages now: " + stages.size() + ", transitions: " + transitions.size());
        saveToFile();
    }

    public void updateStage(String stageId, QuestStage stage) {
        if (stages.containsKey(stageId)) {
            QuestStage existingStage = stages.get(stageId);
            stage.setStageIndex(existingStage.getStageIndex());
            
            transitions.keySet().removeIf(key -> key.startsWith(stageId + "_next_"));
            
            stages.put(stageId, stage);
            
            updateTransitionsForStage(stageId, stage);
            saveToFile();
        }
    }
    
    public void addChildToParent(String parentId, String childId, String optionText) {
        QuestStage parent = stages.get(parentId);
        if (parent != null) {
            parent.getOptions().put(optionText, childId);
            String transitionKey = parentId + "_next_" + optionText;
            transitions.put(transitionKey, childId);
            saveToFile();
        }
    }

    public String getNextStageId(String choice) {
        return transitions.get(choice);
    }

    public Map<String, String> getParentsOf(String childStageId) {
        Map<String, String> parents = new HashMap<>();
        for (Map.Entry<String, QuestStage> entry : stages.entrySet()) {
            String parentId = entry.getKey();
            QuestStage stage = entry.getValue();
            for (Map.Entry<String, String> option : stage.getOptions().entrySet()) {
                if (option.getValue().equals(childStageId)) {
                    parents.put(parentId, option.getKey());
                }
            }
        }
        return parents;
    }

    public void removeChildFromParent(String parentId, String childId) {
        QuestStage parent = stages.get(parentId);
        if (parent != null) {
            parent.getOptions().values().removeIf(childId::equals);
            transitions.entrySet().removeIf(entry -> 
                entry.getKey().startsWith(parentId + "_next_") && entry.getValue().equals(childId)
            );
            saveToFile();
        }
    }

    public void deleteStage(String stageId) {
        if (stages.containsKey(stageId)) {
            stages.remove(stageId);
            transitions.entrySet().removeIf(entry -> 
                entry.getKey().startsWith(stageId + "_next_") || entry.getValue().equals(stageId)
            );
            for (QuestStage stage : stages.values()) {
                stage.getOptions().values().removeIf(stageId::equals);
            }
            reindexStages();
            saveToFile();
        }
    }

    public void setStartStage(String stageId) {
        if (stages.containsKey(stageId)) {
            logger.info("Setting start stage to: " + stageId);
        }
    }

    private void updateTransitionsForStage(String stageId, QuestStage stage) {
        for (Map.Entry<String, String> option : stage.getOptions().entrySet()) {
            String transitionKey = stageId + "_next_" + option.getKey();
            transitions.put(transitionKey, option.getValue());
        }
    }

    private void reindexStages() {
        int index = 0;
        for (Map.Entry<String, QuestStage> entry : stages.entrySet()) {
            entry.getValue().setStageIndex(++index);
        }
    }

    private void initializeDefaultQuest() {
        stages.clear();
        transitions.clear();

        Map<String, String> startOptions = new HashMap<>();
        startOptions.put("left", "Пойти налево");
        startOptions.put("right", "Пойти направо");
        stages.put(START, new QuestStage("Начало игры", "Вы стоите на развилке. Куда пойдете?", startOptions));
        transitions.put("start_next_left", "left");
        transitions.put("start_next_right", "right");

        QuestStage leftPath = new QuestStage(
                "Левая тропинка",
                "Вы пошли по левой тропинке и встретили медведя. Что будете делать?",
                Map.of(
                        "fight", "Сразиться с медведем",
                        "run", "Убежать"
                )
        );
        stages.put("left", leftPath);
        transitions.put("left_next_fight", "fight");
        transitions.put("left_next_run", "run");

        QuestStage runFromBear = new QuestStage(
                "Убежать от медведя",
                "Вы попытались убежать, но медведь оказался быстрее.",
                Map.of(),
                QuestStage.ResultType.LOSE
        );
        stages.put("run", runFromBear);
        QuestStage fightBear = new QuestStage(
                "Схватка с медведем",
                "Вы решили сразиться с медведем и неожиданно напугали его своей храбростью. Медведь убежал, оставив после себя банку с мёдом.",
                Map.of(
                        "take_honey", "Взять мёд",
                        "leave_honey", "Оставить мёд"
                )
        );
        stages.put("fight", fightBear);
        transitions.put("fight_next_take_honey", "take_honey");
        transitions.put("fight_next_leave_honey", "leave_honey");

        // Этапы после решения взять мёд (WIN)
        QuestStage takeHoney = new QuestStage(
                "Взять мёд",
                "Вы взяли мёд и продолжили путь. Через некоторое время вы вышли к деревне и продали мёд за хорошую цену. Победа!",
                Map.of(),
                QuestStage.ResultType.WIN
        );
        stages.put("take_honey", takeHoney);

        // Этапы после решения оставить мёд (LOSE)
        QuestStage leaveHoney = new QuestStage(
                "Оставить мёд",
                "Вы оставили мёд и продолжили путь. Вскоре вы заблудились в лесу без еды и воды.",
                Map.of(),
                QuestStage.ResultType.LOSE
        );
        stages.put("leave_honey", leaveHoney);

        QuestStage rightPath = new QuestStage(
                "Правая тропинка",
                "Вы пошли по правой тропинке и вышли к реке. Что будете делать?",
                Map.of(
                        "swim", "Переплыть реку",
                        "bridge", "Поискать мост"
                )
        );
        stages.put("right", rightPath);
        transitions.put("right_next_swim", "swim");
        transitions.put("right_next_bridge", "bridge");

        QuestStage swimAcross = new QuestStage(
                "Переплыть реку",
                "Вы попытались переплыть реку, но течение оказалось слишком сильным. Вас унесло вниз по течению.",
                Map.of(),
                QuestStage.ResultType.LOSE
        );
        stages.put("swim", swimAcross);

        QuestStage findBridge = new QuestStage(
                "Поиск моста",
                "Вы пошли вдоль реки и вскоре нашли старый мост. Перейдя по нему, вы обнаружили сундук с сокровищами. Победа!",
                Map.of(),
                QuestStage.ResultType.WIN
        );
        stages.put("bridge", findBridge);

        QuestStage win = new QuestStage(
                "Победа!",
                "Поздравляем! Вы успешно прошли квест!",
                Map.of(
                        START, "Начать заново"
                )
        );
        stages.put(WIN, win);

        QuestStage lose = new QuestStage(
                "Поражение",
                "К сожалению, ваше приключение закончилось неудачей.",
                Map.of(
                        START, "Начать заново"
                )
        );
        stages.put(LOSE, lose);
    }
    
    private void saveToFile() {
        if (!enablePersistence) {
            logger.fine("Persistence disabled, skipping save");
            return;
        }
        
        try {
            Map<String, QuestData.QuestStageData> stageDataMap = new LinkedHashMap<>();
            for (Map.Entry<String, QuestStage> entry : stages.entrySet()) {
                QuestStage stage = entry.getValue();
                stageDataMap.put(entry.getKey(), new QuestData.QuestStageData(
                    stage.getTitle(),
                    stage.getDescription(),
                    stage.getOptions(),
                    stage.getResultType().name(),
                    stage.getStageIndex()
                ));
            }
            
            QuestData questData = new QuestData(stageDataMap, new HashMap<>(transitions));
            File file = new File(QUEST_DATA_FILE);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, questData);
            logger.info("Quest data saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save quest data", e);
        }
    }
    
    private boolean loadFromFile() {
        try {
            File file = new File(QUEST_DATA_FILE);
            if (!file.exists()) {
                logger.info("Quest data file not found: " + file.getAbsolutePath());
                return false;
            }
            
            QuestData questData = objectMapper.readValue(file, QuestData.class);
            
            stages.clear();
            transitions.clear();
            
            for (Map.Entry<String, QuestData.QuestStageData> entry : questData.getStages().entrySet()) {
                QuestData.QuestStageData stageData = entry.getValue();
                QuestStage.ResultType resultType = QuestStage.ResultType.NONE;
                try {
                    resultType = QuestStage.ResultType.valueOf(stageData.getResultType());
                } catch (Exception e) {
                    resultType = QuestStage.ResultType.NONE;
                }
                
                QuestStage stage = new QuestStage(
                    stageData.getTitle(),
                    stageData.getDescription(),
                    stageData.getOptions(),
                    resultType,
                    stageData.getStageIndex()
                );
                stages.put(entry.getKey(), stage);
            }
            
            if (questData.getTransitions() != null) {
                transitions.putAll(questData.getTransitions());
            }
            
            reindexStages();
            
            logger.info("Quest data loaded from: " + file.getAbsolutePath() + " with " + stages.size() + " stages");
            return true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load quest data", e);
            return false;
        }
    }
}