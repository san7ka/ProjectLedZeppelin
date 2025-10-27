package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.entity.UserStatistics;

import java.util.Optional;

public interface UserStatisticsDAO {
    
    Optional<UserStatistics> findById(Long id);
    
    Optional<UserStatistics> findByUser(User user);
    
    UserStatistics save(UserStatistics statistics);
    
    UserStatistics update(UserStatistics statistics);
    
    void deleteById(Long id);
    
    UserStatistics getOrCreate(User user);
}
