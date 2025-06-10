package ftp.gusamyky.server.config;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServerConfig {
    private static final Logger LOGGER = Logger.getLogger(ServerConfig.class.getName());
    private static final int MIN_PORT = 1024;
    private static final int MAX_PORT = 65535;
    private static final String DEFAULT_HOST = "localhost";

    private final int port;
    private final String filesDir;
    private final String host;

    public ServerConfig(int port, String filesDir, String host) {
        validatePort(port);
        validateFilesDir(filesDir);
        this.port = port;
        this.filesDir = filesDir;
        this.host = host != null && !host.trim().isEmpty() ? host : DEFAULT_HOST;
    }

    private void validatePort(int port) {
        if (port < MIN_PORT || port > MAX_PORT) {
            String msg = String.format("Port must be between %d and %d", MIN_PORT, MAX_PORT);
            LOGGER.severe(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private void validateFilesDir(String filesDir) {
        if (filesDir == null || filesDir.trim().isEmpty()) {
            String msg = "Files directory cannot be null or empty";
            LOGGER.severe(msg);
            throw new IllegalArgumentException(msg);
        }

        try {
            Files.createDirectories(Paths.get(filesDir));
        } catch (Exception e) {
            String msg = "Failed to create or access files directory: " + filesDir;
            LOGGER.log(Level.SEVERE, msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }

    public int getPort() {
        return port;
    }

    public String getFilesDir() {
        return filesDir;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "port=" + port +
                ", filesDir='" + filesDir + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}