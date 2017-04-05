/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.config;

import io.cluster.util.Constants;
import java.util.List;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 *
 * @author thangpham
 */
public class ApplicationConfig {

    private Configuration config = null;
    private static ApplicationConfig _instance = null;
    public static final String SERVER_TYPE = "server", CLIENT_TYPE = "client", NORMAL_TYPE = "normal";
    public static final String DERBY_DB = "derby", POSTGRESQL_DB = "postgresql";
    private String DATABASE_TYPE = null;

    static {
        _instance = new ApplicationConfig();
    }

    public static ApplicationConfig load() {
        return _instance;
    }

    public ApplicationConfig() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(Constants.getApplicationConfigFile())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        try {
            config = builder.getConfiguration();
            DATABASE_TYPE = config.getString("cluster.database.type");
        } catch (ConfigurationException cex) {
            cex.printStackTrace();
        }
    }

    public boolean enableHttp() {
        return config.getBoolean("http.enable", Boolean.FALSE);
    }

    public String httpHost() {
        return config.getString("http.host");
    }

    public int httpPort() {
        return config.getInt("http.port");
    }

    public List<String> httpLoadedControllerPackages() {
        return config.getList(String.class, "http.endpoint.packages");
    }

    public boolean enableScheduler() {
        return config.getBoolean("scheduler.enable", Boolean.FALSE);
    }

    public int schedulerMaxThread() {
        return config.getInt("scheduler.maxthread");
    }

    public List<String> schedulerLoadedJobPackages() {
        return config.getList(String.class, "scheduler.job.packages");
    }

    public String clusterType() {
        return config.getString("cluster.type", NORMAL_TYPE);
    }

    public String clusterServerHost() {
        return config.getString("cluster.server.host");
    }

    public int clusterServerPort() {
        return config.getInt("cluster.server.port");
    }

    public String clusterClientHost() {
        return config.getString("cluster.client.host", null);
    }

    public int clusterClientPort() {
        return config.getInt("cluster.client.port", -1);
    }

    public int clusterBufferSize() {
        return config.getInt("cluster.buffer.size", 4096);
    }

    public long clusterClientPingPeriod() {
        return config.getInt("cluster.client.pingperiod", 3000);
    }

    public String clusterDbHost() {
        return config.getString("cluster.database." + DATABASE_TYPE + ".host");
    }

    public int clusterDbPort() {
        return config.getInt("cluster.database." + DATABASE_TYPE + ".port");
    }
    
    public String clusterDbUsername() {
        return config.getString("cluster.database." + DATABASE_TYPE + ".username");
    }
    
    public String clusterDbPassword() {
        return config.getString("cluster.database." + DATABASE_TYPE + ".password");
    }

    public String clusterDbName() {
        return config.getString("cluster.database.dbname");
    }

    public String getDatabaseType() {
        return DATABASE_TYPE;
    }

}
