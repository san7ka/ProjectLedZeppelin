package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Game;
import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.User;

import java.util.List;

public interface GameDAO extends BaseDAO<Game, Long> {

    List<Game> findByUser(User user);
    
    List<Game> findByQuest(Quest quest);
}
