package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.User;
import java.util.List;

public interface QuestDAO extends BaseDAO<Quest, Long> {
    
    List<Quest> findByAuthor(User author);
    
    List<Quest> findByAuthorId(Long authorId);
    
    List<Quest> findByAuthorUsername(String authorUsername);
    
    List<Quest> findByTitleContaining(String titlePart);

    List<Quest> findByDescriptionContaining(String descriptionPart);
    
    List<Quest> findByTitleOrDescriptionContaining(String searchTerm);
    
    List<Quest> findByCreatedAtAfter(java.time.LocalDateTime date);
    
    List<Quest> findByCreatedAtBefore(java.time.LocalDateTime date);
    
    List<Quest> findByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    List<Quest> findByUpdatedAtAfter(java.time.LocalDateTime date);
    
    List<Quest> findAllOrderByCreatedAtDesc();
    
    List<Quest> findAllOrderByUpdatedAtDesc();
    
    List<Quest> findAllOrderByTitle();
    
    List<Quest> findAllOrderByAuthorUsername();
    
    List<Quest> findAllOrderByCreatedAtDesc(int offset, int limit);
    
    List<Quest> findAllOrderByUpdatedAtDesc(int offset, int limit);
    
    long countByAuthor(User author);
    
    long countByAuthorId(Long authorId);
    
    long countByAuthorUsername(String authorUsername);
    
    List<Quest> findByMinStageCount(int minStages);
    
    List<Quest> findByMaxStageCount(int maxStages);
    
    List<Quest> findByStageCountBetween(int minStages, int maxStages);
    
    Object[] getQuestStatistics();
    
    List<Quest> findAllWithAuthor();
    
    List<Quest> findAllWithAuthorAndStages();
}
