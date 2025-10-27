package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.QuestStage;
import com.javarush.lepeshinskij.entity.QuestStageOption;
import java.util.List;
import java.util.Optional;

public interface QuestStageDAO extends BaseDAO<QuestStage, Long> {
    
    Optional<QuestStage> findByQuestIdAndStageId(Long questId, String stageId);
    
    Optional<QuestStage> findByQuestAndStageId(Quest quest, String stageId);
    
    List<QuestStage> findByQuest(Quest quest);
    
    List<QuestStage> findByQuestId(Long questId);
    
    List<QuestStage> findByQuestOrderByStageIndex(Quest quest);
    
    List<QuestStage> findByQuestIdOrderByStageIndex(Long questId);
    
    Optional<QuestStage> findStartStageByQuest(Quest quest);
    
    Optional<QuestStage> findStartStageByQuestId(Long questId);
    
    List<QuestStage> findByResultType(QuestStage.ResultType resultType);
    
    List<QuestStage> findByQuestAndResultType(Quest quest, QuestStage.ResultType resultType);
    
    List<QuestStage> findByQuestIdAndResultType(Long questId, QuestStage.ResultType resultType);
    
    List<QuestStage> findWinStagesByQuest(Quest quest);
    
    List<QuestStage> findLoseStagesByQuest(Quest quest);
    
    List<QuestStage> findWinStagesByQuestId(Long questId);
    
    List<QuestStage> findLoseStagesByQuestId(Long questId);
    
    List<QuestStage> findByTitleContaining(String titlePart);
    
    List<QuestStage> findByDescriptionContaining(String descriptionPart);
    
    List<QuestStage> findByTitleOrDescriptionContaining(String searchTerm);
    
    List<QuestStage> findByStageIndex(Integer stageIndex);
    
    List<QuestStage> findByStageIndexBetween(Integer minIndex, Integer maxIndex);
    
    List<QuestStage> findByStageIndexGreaterThanEqual(Integer minIndex);
    
    List<QuestStage> findByStageIndexLessThanEqual(Integer maxIndex);
    
    long countByQuest(Quest quest);
    
    long countByQuestId(Long questId);
    
    long countByResultType(QuestStage.ResultType resultType);
    
    long countByQuestAndResultType(Quest quest, QuestStage.ResultType resultType);
    
    Integer getMaxStageIndexByQuest(Quest quest);
    
    Integer getMaxStageIndexByQuestId(Long questId);
    
    boolean existsByQuestAndStageId(Quest quest, String stageId);
    
    boolean existsByQuestIdAndStageId(Long questId, String stageId);
    
    List<QuestStageOption> loadStageOptions(Long stageId);
    
    Optional<QuestStageOption> findOptionById(Long optionId);
}
