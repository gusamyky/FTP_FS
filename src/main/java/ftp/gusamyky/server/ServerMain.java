package ftp.gusamyky.server;

import ftp.gusamyky.server.config.ConfigLoader;
import ftp.gusamyky.server.config.ServerConfig;
import ftp.gusamyky.server.config.DatabaseConfig;
import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.util.DatabaseInitializer;
import ftp.gusamyky.server.network.ServerNetworkService;
import java.time.LocalDateTime;

public class ServerMain {
    private static ServerNetworkService networkService;

    public static void main(String[] args) {
        System.out.println("[" + LocalDateTime.now() + "] Server starting...");
        try {
            // Check if MySQL is running before attempting to connect
            System.out.println("[" + LocalDateTime.now() + "] Checking if MySQL server is running...");
            checkMySQLStatus();

            // Load config and initialize services
            ServerConfig serverConfig = ConfigLoader.loadServerConfig();
            DatabaseConfig dbConfig = ConfigLoader.loadDatabaseConfig();
            DatabaseInitializer.initialize(dbConfig);
            ServiceFactory serviceFactory = new ServiceFactory(dbConfig);
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
     * Checks if MySQL server is running and accessible
     */
    private static void checkMySQLStatus() {
        String host = "localhost";
        int port = 3306;
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 1000);
            System.out.println("[" + LocalDateTime.now() + "] MySQL server is running at " + host + ":" + port);
        } catch (java.net.ConnectException e) {
            System.err.println(
                    "[" + LocalDateTime.now() + "] ERROR: MySQL server is not running at " + host + ":" + port);
            System.err
                    .println("[" + LocalDateTime.now() + "] Please start MySQL server before running this application");
            System.err.println("[" + LocalDateTime.now() + "] Starting instructions:" +
                    "\n  - Linux: sudo service mysql start" +
                    "\n  - macOS: mysql.server start" +
                    "\n  - Windows: Start MySQL service from Services app");
            throw new RuntimeException("MySQL server is not running at " + host + ":" + port);
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] Error checking MySQL status: " + e.getMessage());
            throw new RuntimeException("Failed to check MySQL status", e);
        }
    }
}