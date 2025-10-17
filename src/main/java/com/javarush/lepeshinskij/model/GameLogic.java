package com.javarush.lepeshinskij.model;

import com.javarush.lepeshinskij.service.QuestService;
import java.util.logging.Logger;

public class GameLogic {
    private static final Logger logger = Logger.getLogger(GameLogic.class.getName());
    private final Quest quest;
    private String currentStageId;

    public GameLogic() {
        this.quest = QuestService.getInstance().getQuest();
        this.currentStageId = Quest.START;
    }

    public QuestStage getCurrentStage() {
        return quest.getStage(currentStageId);
    }

    public boolean processChoice(String choice) {
        logger.info("Processing choice: " + choice + " from stage: " + currentStageId);
        
        String transitionKey = currentStageId + "_next_" + choice;
        logger.info("Looking for transition key: " + transitionKey);
        String nextStageId = quest.getNextStageId(transitionKey);

        if (nextStageId != null) {
            logger.info("Found transition to stage: " + nextStageId);
            currentStageId = nextStageId;
            
            QuestStage nextStage = quest.getStage(nextStageId);
            if (nextStage != null) {
                QuestStage.ResultType resultType = nextStage.getResultType();
                
                if (resultType == QuestStage.ResultType.WIN) {
                    currentStageId = Quest.WIN;
                } else if (resultType == QuestStage.ResultType.LOSE) {
                    currentStageId = Quest.LOSE;
                }
            }
        } else {
            logger.info("Transition not found, checking for direct stage: " + choice);
            QuestStage directStage = quest.getStage(choice);
            if (directStage != null) {
                logger.info("Found direct stage: " + choice);
                currentStageId = choice;
            } else {
                logger.warning("No stage found for choice: " + choice);
            }
        }

        return isGameOver();
    }

    public boolean isGameOver() {
        return Quest.WIN.equals(currentStageId) || Quest.LOSE.equals(currentStageId);
    }

    public boolean isWin() {
        return Quest.WIN.equals(currentStageId);
    }

    public void reset() {
        this.currentStageId = Quest.START;
    }

    public String getCurrentStageId() {
        return currentStageId;
    }
}