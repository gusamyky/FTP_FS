package ftp.gusamyky.server;

import ftp.gusamyky.server.config.ConfigLoader;
import ftp.gusamyky.server.config.ServerConfig;
import ftp.gusamyky.server.config.DatabaseConfig;
import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.util.DatabaseInitializer;
import ftp.gusamyky.server.network.ServerNetworkService;
import java.time.LocalDateTime;
import java.sql.DriverManager;
import java.sql.Connection;

public class ServerMain {
    private static ServerNetworkService networkService;

    public static void main(String[] args) {
        System.out.println("[" + LocalDateTime.now() + "] Server starting...");
        try {
            // Load config first to get database connection details
            ServerConfig serverConfig = ConfigLoader.loadServerConfig();
            DatabaseConfig dbConfig = ConfigLoader.loadDatabaseConfig();

            // Check if Azure MySQL is accessible
            System.out.println("[" + LocalDateTime.now() + "] Checking Azure MySQL connection...");
            checkMySQLStatus(dbConfig);

            // Initialize services
            DatabaseInitializer.initialize(dbConfig);
            ServiceFactory serviceFactory = new ServiceFactory(dbConfig, serverConfig);
            networkService = new ServerNetworkService(serverConfig, serviceFactory);

            // Start socket server
            Thread socketThread = new Thread(() -> {
                try {
                    networkService.start();
                } catch (Exception e) {
                    System.err.println("[" + LocalDateTime.now() + "] Socket server error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            socketThread.start();

            // Add shutdown hook to close resources properly
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[" + LocalDateTime.now() + "] Server shutting down, closing resources...");
                // Tu można dodać zamykanie połączeń, jeśli potrzeba
                System.out.println("[" + LocalDateTime.now() + "] Server shutdown complete");
            }));

            System.out.println("[" + LocalDateTime.now() + "] Server started successfully");
            socketThread.join();
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Checks if Azure MySQL server is accessible
     */
    private static void checkMySQLStatus(DatabaseConfig dbConfig) {
        try {
            // Try to establish a connection to Azure MySQL
            try (Connection conn = DriverManager.getConnection(
                    dbConfig.getUrlNoDb(),
                    dbConfig.getUser(),
                    dbConfig.getPassword())) {
                System.out.println("[" + LocalDateTime.now() + "] Successfully connected to Azure MySQL server");
            }
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] ERROR: Cannot connect to Azure MySQL server");
            System.err.println("[" + LocalDateTime.now() + "] Please check your Azure MySQL connection settings:");
            System.err.println("[" + LocalDateTime.now() + "] - Server URL: " + dbConfig.getUrl());
            System.err.println("[" + LocalDateTime.now() + "] - Username: " + dbConfig.getUser());
            System.err.println(
                    "[" + LocalDateTime.now() + "] - Make sure your IP is allowed in Azure MySQL firewall rules");
            System.err.println("[" + LocalDateTime.now() + "] - Verify SSL settings in connection string");
            throw new RuntimeException("Failed to connect to Azure MySQL server", e);
        }
    }
}