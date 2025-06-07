package common.db;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

public class DatabaseConfig {
    private String url;
    private String user;
    private String password;
    private String dbName;
    private int maxConnections = 10;
    private int connectionTimeout = 30000; // 30 seconds

    // Default values in case config file is missing
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/ftp_fs";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    public DatabaseConfig() throws IOException {
        Properties prop = new Properties();
        boolean configLoaded = false;

        // Try to load from classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                prop.load(input);
                configLoaded = true;
                System.out.println("Loaded database configuration from classpath");
            }
        } catch (IOException e) {
            System.err.println("Could not load config.properties from classpath: " + e.getMessage());
        }

        // If classpath loading failed, try alternate locations
        if (!configLoaded) {
            // Try current directory
            File configFile = new File("config.properties");
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    prop.load(fis);
                    configLoaded = true;
                    System.out.println("Loaded database configuration from current directory");
                } catch (IOException e) {
                    System.err.println("Error reading config.properties from file system: " + e.getMessage());
                }
            }
        }

        // Load configuration with fallbacks to defaults if needed
        url = getPropertyWithDefault(prop, "db.url", DEFAULT_URL);
        user = getPropertyWithDefault(prop, "db.user", DEFAULT_USER);
        password = getPropertyWithDefault(prop, "db.password", DEFAULT_PASSWORD);

        // Try to parse optional configuration values
        try {
            String maxConnStr = prop.getProperty("db.max_connections");
            if (maxConnStr != null && !maxConnStr.trim().isEmpty()) {
                maxConnections = Integer.parseInt(maxConnStr);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid max connections value, using default: " + maxConnections);
        }

        try {
            String timeoutStr = prop.getProperty("db.connection_timeout");
            if (timeoutStr != null && !timeoutStr.trim().isEmpty()) {
                connectionTimeout = Integer.parseInt(timeoutStr);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid connection timeout value, using default: " + connectionTimeout);
        }

        // Parse database name from URL
        int lastSlashIndex = url.lastIndexOf("/");
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            dbName = url.substring(lastSlashIndex + 1);

            // Remove any parameters from the database name
            int paramIndex = dbName.indexOf("?");
            if (paramIndex != -1) {
                dbName = dbName.substring(0, paramIndex);
            }
        } else {
            dbName = "ftp_fs"; // Default database name if URL is malformed
            System.err.println("WARNING: Could not parse database name from URL, using default: " + dbName);
        }

        // Validate configuration
        if (!url.startsWith("jdbc:mysql://")) {
            System.err.println("WARNING: Database URL does not appear to be a valid MySQL JDBC URL: " + url);
        }

        System.out.println("Database configuration initialized with URL: " + url + ", user: " + user + ", database: " + dbName);
    }

    private String getPropertyWithDefault(Properties prop, String key, String defaultValue) {
        String value = prop.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            System.out.println("Property '" + key + "' not found or empty, using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPassword() { return password; }
    public String getDbName() { return dbName; }
    public int getMaxConnections() { return maxConnections; }
    public int getConnectionTimeout() { return connectionTimeout; }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
               "url='" + url + "', " +
               "user='" + user + "', " +
               "dbName='" + dbName + "', " +
               "maxConnections=" + maxConnections + ", " +
               "connectionTimeout=" + connectionTimeout +
               '}';
    }
} 