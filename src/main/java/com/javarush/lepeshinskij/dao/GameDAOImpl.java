package com.javarush.lepeshinskij.dao;

import com.javarush.lepeshinskij.entity.Game;
import com.javarush.lepeshinskij.entity.Quest;
import com.javarush.lepeshinskij.entity.User;
import org.hibernate.SessionFactory;

import java.util.List;

public class GameDAOImpl extends BaseDAOImpl<Game, Long> implements GameDAO {

    public GameDAOImpl(SessionFactory sessionFactory) {
        super(Game.class, sessionFactory);
    }

    @Override
    public List<Game> findByUser(User user) {
        return executeInTransaction(session ->
                session.createQuery("from Game g join fetch g.quest where g.user = :user order by g.finishedAt desc", Game.class)
                        .setParameter("user", user)
                        .list());
    }
    
    @Override
    public List<Game> findByQuest(Quest quest) {
        return executeInTransaction(session ->
                session.createQuery("from Game g where g.quest = :quest order by g.finishedAt desc", Game.class)
                        .setParameter("quest", quest)
                        .list());
    }
}
