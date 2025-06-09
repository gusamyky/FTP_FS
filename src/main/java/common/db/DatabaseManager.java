package common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private DatabaseConfig config;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_SECONDS = 5;

    private DatabaseManager() {
        try {
            initializeDatabase();
            // Schedule a connection validation every 10 minutes
            scheduler.scheduleAtFixedRate(this::validateConnection, 10, 10, TimeUnit.MINUTES);
        } catch (IOException | SQLException e) {
            System.err.println("[" + LocalDateTime.now() + "] Failed to initialize database: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void initializeDatabase() throws IOException, SQLException {
        config = new DatabaseConfig();
        int retries = 0;
        boolean connected = false;

        while (!connected && retries < MAX_RETRIES) {
            try {
                // First connect without specifying a database to create it if needed
                String urlNoDb = config.getUrl().substring(0, config.getUrl().lastIndexOf("/") + 1);

                System.out.println("[" + LocalDateTime.now() + "] Attempting to connect to MySQL server at " + urlNoDb);

                // Create database if it doesn't exist
                try (Connection conn = DriverManager.getConnection(urlNoDb, config.getUser(), config.getPassword());
                        Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + config.getDbName());
                    System.out.println("[" + LocalDateTime.now() + "] Successfully connected to MySQL server");
                }

                // Connect to the actual database
                System.out.println("[" + LocalDateTime.now() + "] Connecting to database: " + config.getDbName());
                connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
                initDatabase();
                connected = true;
                System.out.println(
                        "[" + LocalDateTime.now() + "] Successfully connected to database: " + config.getDbName());
            } catch (SQLException e) {
                retries++;
                if (retries >= MAX_RETRIES) {
                    System.err.println("[" + LocalDateTime.now() + "] Failed to connect to MySQL after " + MAX_RETRIES
                            + " attempts");
                    throw e;
                } else {
                    System.err.println("[" + LocalDateTime.now() + "] Database connection attempt " + retries
                            + " failed: " + e.getMessage());
                    System.err.println("Retrying in " + RETRY_DELAY_SECONDS + " seconds...");
                    try {
                        Thread.sleep(RETRY_DELAY_SECONDS * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void initDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Klienci (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "username VARCHAR(50) UNIQUE, " +
                            "password VARCHAR(255))");
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS HistoriaOperacji (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "client_id INT, " +
                            "operation VARCHAR(255), " +
                            "timestamp DATETIME, " +
                            "FOREIGN KEY (client_id) REFERENCES Klienci(id))");
            System.out.println("[" + LocalDateTime.now() + "] Database tables verified/created successfully");
        }
    }

    private void validateConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println(
                        "[" + LocalDateTime.now() + "] Database connection is closed or null. Reconnecting...");
                initializeDatabase();
            } else {
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeQuery("SELECT 1");
                }
            }
        } catch (SQLException | IOException e) {
            System.err.println("[" + LocalDateTime.now() + "] Connection validation failed: " + e.getMessage());
            try {
                initializeDatabase();
            } catch (IOException | SQLException ex) {
                System.err.println(
                        "[" + LocalDateTime.now() + "] Failed to re-establish database connection: " + ex.getMessage());
            }
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    initializeDatabase();
                } catch (IOException | SQLException e) {
                    throw new RuntimeException("Failed to re-establish database connection", e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking connection status", e);
        }
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[" + LocalDateTime.now() + "] Database connection closed");
            } catch (SQLException e) {
                System.err.println("[" + LocalDateTime.now() + "] Error closing connection: " + e.getMessage());
            }
        }
        scheduler.shutdown();
    }
}