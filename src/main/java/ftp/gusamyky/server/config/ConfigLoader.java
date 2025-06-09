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
        int port = Integer.parseInt(prop.getProperty("server.port", "2121"));
        String filesDir = prop.getProperty("server.filesDir", "server_files");
        String host = prop.getProperty("server.host", "0.0.0.0");
        return new ServerConfig(port, filesDir, host);
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
        String url = prop.getProperty("db.url", "jdbc:mysql://localhost:3306/ftp_fs");
        String user = prop.getProperty("db.user", "root");
        String password = prop.getProperty("db.password", "");
        String filesDirectory = prop.getProperty("db.files_directory", "server_files");
        String urlNoDb = prop.getProperty("db.urlNoDb", "jdbc:mysql://localhost:3306/");
        return new DatabaseConfig(url, user, password, filesDirectory, urlNoDb);
    }
}