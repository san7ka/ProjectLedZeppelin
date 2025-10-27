package com.javarush.lepeshinskij.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
@WebListener
public class LiquibaseContextListener implements ServletContextListener {

    private static final String CHANGELOG_FILE = "db/changelog/db.changelog-master.xml";

    @Override
    @SuppressWarnings("deprecation")
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Запуск Liquibase миграций...");
        
        try {
            String url = System.getenv().getOrDefault("SPRING_DATASOURCE_URL", 
                    "jdbc:postgresql://localhost:5432/game");
            String username = System.getenv().getOrDefault("SPRING_DATASOURCE_USERNAME", "postgres");
            String password = System.getenv().getOrDefault("SPRING_DATASOURCE_PASSWORD", "postgres");
            
            log.info("Подключение к БД: {}", url);
            
            Class.forName("org.postgresql.Driver");
            
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                
                Database database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
                
                try (Liquibase liquibase = new Liquibase(
                        CHANGELOG_FILE,
                        new ClassLoaderResourceAccessor(),
                        database)) {
                    
                    liquibase.update(new Contexts(), new LabelExpression());
                    
                    log.info("Liquibase миграции успешно выполнены");
                }
            }
            
        } catch (Exception e) {
            log.error("Ошибка при выполнении Liquibase миграций", e);
            throw new RuntimeException("Не удалось выполнить миграции БД", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Liquibase Context Listener остановлен");
    }
}
