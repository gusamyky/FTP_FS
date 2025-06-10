package ftp.gusamyky.server;

import ftp.gusamyky.server.config.ConfigLoader;
import ftp.gusamyky.server.config.ServerConfig;
import ftp.gusamyky.server.config.DatabaseConfig;
import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.util.DatabaseInitializer;
import ftp.gusamyky.server.network.ServerNetworkService;
import ftp.gusamyky.server.common.exception.DatabaseException;
import ftp.gusamyky.server.common.exception.ServerException;

import java.sql.DriverManager;
import java.sql.Connection;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ServerMain {
    private static final Logger LOGGER = Logger.getLogger(ServerMain.class.getName());
    private static ServerNetworkService networkService;

    public static void main(String[] args) {
        LOGGER.info("Server starting...");
        try {
            // Load configurations
            ServerConfig serverConfig = ConfigLoader.loadServerConfig();
            DatabaseConfig dbConfig = ConfigLoader.loadDatabaseConfig();

            // Verify database connection
            LOGGER.info("Verifying database connection...");
            verifyDatabaseConnection(dbConfig);

            // Initialize services
            DatabaseInitializer.initialize(dbConfig);
            ServiceFactory serviceFactory = new ServiceFactory(dbConfig, serverConfig);
            networkService = new ServerNetworkService(serverConfig, serviceFactory);

            // Start network service
            startNetworkService();

            // Add shutdown hook
            addShutdownHook();

            LOGGER.info("Server started successfully");
        } catch (DatabaseException e) {
            LOGGER.log(Level.SEVERE, "Database error: " + e.getMessage(), e);
            System.exit(1);
        } catch (ServerException e) {
            LOGGER.log(Level.SEVERE, "Server error: " + e.getMessage(), e);
            System.exit(1);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void startNetworkService() {
        Thread socketThread = new Thread(() -> {
            try {
                networkService.start();
            } catch (ServerException e) {
                LOGGER.log(Level.SEVERE, "Network service error: " + e.getMessage(), e);
            }
        });
        socketThread.setName("NetworkService");
        socketThread.start();
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Server shutting down, closing resources...");
            if (networkService != null) {
                try {
                    networkService.stop();
                } catch (ServerException e) {
                    LOGGER.log(Level.WARNING, "Error during network service shutdown: " + e.getMessage(), e);
                }
            }
            LOGGER.info("Server shutdown complete");
        }));
    }

    private static void verifyDatabaseConnection(DatabaseConfig dbConfig) {
        try (Connection conn = DriverManager.getConnection(
                dbConfig.getUrlNoDb(),
                dbConfig.getUser(),
                dbConfig.getPassword())) {
            LOGGER.info("Successfully connected to database server");
        } catch (Exception e) {
            String errorMsg = String.format("Failed to connect to database server. URL: %s, User: %s",
                    dbConfig.getUrl(), dbConfig.getUser());
            LOGGER.severe(errorMsg);
            LOGGER.severe("Please check your database connection settings:");
            LOGGER.severe("- Verify network connectivity and firewall rules");
            LOGGER.severe("- Check SSL settings if enabled");
            throw new DatabaseException(errorMsg, e);
        }
    }
}