package com.javarush.lepeshinskij.listener;

import com.javarush.lepeshinskij.util.HibernateUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateContextListener implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(HibernateContextListener.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Инициализация Hibernate SessionFactory...");
        try {
            SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
            sce.getServletContext().setAttribute("sessionFactory", sessionFactory);
            logger.info("Hibernate SessionFactory успешно инициализирован");
        } catch (Exception e) {
            logger.error("Ошибка инициализации Hibernate SessionFactory", e);
            throw new RuntimeException("Не удалось инициализировать Hibernate", e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Закрытие Hibernate SessionFactory...");
        try {
            HibernateUtil.shutdown();
            logger.info("Hibernate SessionFactory успешно закрыт");
        } catch (Exception e) {
            logger.error("Ошибка при закрытии Hibernate SessionFactory", e);
        }
    }
}
