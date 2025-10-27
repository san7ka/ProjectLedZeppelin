package com.javarush.lepeshinskij.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = buildSessionFactory();
            logger.info("Hibernate SessionFactory initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing Hibernate SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private static SessionFactory buildSessionFactory() {
        try {
            String configPath = determineConfigPath();
            
            Configuration configuration = new Configuration().configure(configPath);

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    private static String determineConfigPath() {
        try {
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress("postgres", 5432), 1000);
            socket.close();
            logger.info("Using Docker configuration (hibernate.cfg.xml)");
            return "hibernate.cfg.xml";
        } catch (Exception e) {
            logger.info("Using local configuration (hibernate-local.cfg.xml)");
            return "hibernate-local.cfg.xml";
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            logger.info("Closing Hibernate SessionFactory");
            sessionFactory.close();
        }
    }
}

