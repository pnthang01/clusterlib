/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.dao.derby;

import io.cluster.config.ApplicationConfig;
import io.cluster.model.SuperviseModel;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 *
 * @author thangpham
 */
public class HibernateSessionManager {

    private static final Logger LOGGER = LogManager.getLogger(HibernateSessionManager.class.getName());

    private final ApplicationConfig appConfig = ApplicationConfig.load();
    private static HibernateSessionManager _instance;
    private SessionFactory sf = null;

    static {
        _instance = new HibernateSessionManager();
    }

    public static HibernateSessionManager load() {
        return _instance;
    }

    /**
     *
     * @return
     */
    public synchronized SessionFactory getSessionFactory() {
        if (null == sf) {
            Properties properties = null;
            //driver settings
            if (ApplicationConfig.DERBY_DB.equals(appConfig.getDatabaseType())) {
                properties = buildDerbyConfig();
            } else if (ApplicationConfig.POSTGRESQL_DB.equals(appConfig.getDatabaseType())) {
                properties = buildPostgreConfig();
            }
            sf = new Configuration()
                    .addProperties(properties)
                    .addAnnotatedClass(SuperviseModel.class)
                    .buildSessionFactory(
                            new StandardServiceRegistryBuilder()
                            .applySettings(properties)
                            .build());
        }
        return sf;
    }

    private Properties buildDerbyConfig() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.DerbyTenSevenDialect");
        //log settings
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.connection.driver_class", "org.apache.derby.jdbc.EmbeddedDriver");
        properties.put("hibernate.connection.url", String.format("jdbc:derby:%s;create=true", appConfig.clusterDbName()));
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.connection.pool_size", 5);
        return properties;
    }

    private Properties buildPostgreConfig() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        //log settings
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        properties.put("hibernate.connection.url", String.format("jdbc:postgresql://%s:%d/%s",
                appConfig.clusterDbHost(), appConfig.clusterDbPort(), appConfig.clusterDbName()));
        properties.put("hibernate.connection.username", appConfig.clusterDbUsername());
        properties.put("hibernate.connection.password", appConfig.clusterDbPassword());
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.connection.pool_size", 5);
        return properties;
    }
}
