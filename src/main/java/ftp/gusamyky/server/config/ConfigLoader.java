package ftp.gusamyky.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final String CONFIG_FILE = "config.properties";

    public static ServerConfig loadServerConfig() {
        Properties prop = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                prop.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int port = Integer.parseInt(
                System.getenv().getOrDefault("SERVER_PORT", prop.getProperty("server.port", "2121")));
        String filesDir = System.getenv().getOrDefault("SERVER_FILES_DIR",
                prop.getProperty("server.filesDir", "server_files"));
        return new ServerConfig(port, filesDir);
    }

    public static DatabaseConfig loadDatabaseConfig() {
        Properties prop = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                prop.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url = System.getenv().getOrDefault("DB_URL",
                prop.getProperty("db.url"));
        String user = System.getenv().getOrDefault("DB_USER", prop.getProperty("db.user", "root"));
        String password = System.getenv().getOrDefault("DB_PASSWORD", prop.getProperty("db.password", ""));
        String filesDirectory = System.getenv().getOrDefault("DB_FILES_DIRECTORY",
                prop.getProperty("db.files_directory", "server_files"));
        String urlNoDb = System.getenv().getOrDefault("DB_URL_NO_DB",
                prop.getProperty("db.urlNoDb"));
        return new DatabaseConfig(url, user, password, filesDirectory, urlNoDb);
    }
}