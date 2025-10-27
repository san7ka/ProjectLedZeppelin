package com.javarush.lepeshinskij;

import com.javarush.lepeshinskij.dao.QuestDAOTest;
import com.javarush.lepeshinskij.dao.QuestStageDAOTest;
import com.javarush.lepeshinskij.dao.UserDAOTest;
import com.javarush.lepeshinskij.dao.UserQuestProgressDAOTest;
import com.javarush.lepeshinskij.service.GameServiceTest;
import com.javarush.lepeshinskij.service.QuestServiceTest;
import com.javarush.lepeshinskij.service.UserServiceTest;
import com.javarush.lepeshinskij.controller.AuthControllerTest;
import com.javarush.lepeshinskij.integration.QuestGameIntegrationTest;
import com.javarush.lepeshinskij.performance.HibernatePerformanceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    // DAO Tests
    UserDAOTest.class,
    QuestDAOTest.class,
    QuestStageDAOTest.class,
    UserQuestProgressDAOTest.class,
    
    // Service Tests
    UserServiceTest.class,
    QuestServiceTest.class,
    GameServiceTest.class,
    
    // Controller Tests
    AuthControllerTest.class,
    
    // Integration Tests
    QuestGameIntegrationTest.class,
    
    // Performance Tests
    HibernatePerformanceTest.class
})
public class TestSuite {
}
