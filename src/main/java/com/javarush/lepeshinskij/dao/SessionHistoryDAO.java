package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.SessionHistory;
import com.javarush.lepeshinskij.entity.UserQuestProgress;

import java.util.List;

public interface SessionHistoryDAO extends BaseDAO<SessionHistory, Long> {
    
    List<SessionHistory> findBySessionOrderByStepNumber(UserQuestProgress session);
    
    List<SessionHistory> findBySessionIdOrderByStepNumber(Long sessionId);
    
    SessionHistory findLastBySession(UserQuestProgress session);
    
    long countBySession(UserQuestProgress session);
    
    void deleteBySession(UserQuestProgress session);
    
    void deleteLastNSteps(UserQuestProgress session, int count);
    
    boolean canGoBack(UserQuestProgress session);
}
