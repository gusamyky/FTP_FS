package ftp.gusamyky.client.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private static ConfigManager instance;
    private final Properties properties;

    private ConfigManager() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(CONFIG_FILE));
        } catch (IOException e) {
            // Set default values if config file is not found
            setDefaultValues();
        }
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void setDefaultValues() {
        properties.setProperty("cloud.retry.attempts", "3");
        properties.setProperty("cloud.retry.delay", "1000");
        properties.setProperty("cloud.connection.timeout", "5000");
        properties.setProperty("client.files.dir", "FTP_Files");
    }

    public int getCloudRetryAttempts() {
        return Integer.parseInt(properties.getProperty("cloud.retry.attempts", "3"));
    }

    public int getCloudRetryDelay() {
        return Integer.parseInt(properties.getProperty("cloud.retry.delay", "1000"));
    }

    public int getConnectionTimeout() {
        return Integer.parseInt(properties.getProperty("cloud.connection.timeout", "5000"));
    }

    public String getClientFilesDir() {
        return properties.getProperty("client.files.dir", "FTP_Files");
    }
}