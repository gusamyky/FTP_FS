package ftp.gusamyky.server.config;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_PORT = "2121";
    private static final String DEFAULT_FILES_DIR = "server_files";
    private static final String DEFAULT_HOST = "0.0.0.0";

    public static ServerConfig loadServerConfig() {
        Properties props = new Properties();

        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                LOGGER.warning("Configuration file not found, using default values");
                return createDefaultConfig();
            }

            props.load(input);
            return createConfigFromProperties(props);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load configuration", e);
            return createDefaultConfig();
        }
    }

    private static ServerConfig createConfigFromProperties(Properties props) {
        try {
            int port = Integer.parseInt(props.getProperty("server.port", DEFAULT_PORT));
            String filesDir = props.getProperty("server.filesDir", DEFAULT_FILES_DIR);
            String host = props.getProperty("server.host", DEFAULT_HOST);

            LOGGER.info("Loaded configuration - Port: " + port + ", FilesDir: " + filesDir + ", Host: " + host);
            return new ServerConfig(port, filesDir, host);
        } catch (NumberFormatException e) {
            LOGGER.severe("Invalid port number in configuration");
            throw new IllegalStateException("Invalid port number in configuration", e);
        }
    }

    private static ServerConfig createDefaultConfig() {
        LOGGER.info("Creating default configuration");
        return new ServerConfig(
                Integer.parseInt(DEFAULT_PORT),
                DEFAULT_FILES_DIR,
                DEFAULT_HOST);
    }

    public static DatabaseConfig loadDatabaseConfig() {
        Properties prop = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                prop.load(input);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load database configuration", e);
        }
        String url = prop.getProperty("db.url", "jdbc:mysql://localhost:3306/ftp_fs");
        String user = prop.getProperty("db.user", "root");
        String password = prop.getProperty("db.password", "");
        String filesDirectory = prop.getProperty("db.files_directory", "server_files");
        String urlNoDb = prop.getProperty("db.urlNoDb", "jdbc:mysql://localhost:3306/");
        return new DatabaseConfig(url, user, password, filesDirectory, urlNoDb);
    }
}