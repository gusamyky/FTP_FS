package ftp.gusamyky.server.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.sql.DriverManager;
import ftp.gusamyky.server.config.DatabaseConfig;

public class DatabaseInitializer {
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    private static final String INIT_SCRIPT = "init.sql";

    public static void initialize(DatabaseConfig config) {
        String url = config.getUrlNoDb();
        try (Connection conn = DriverManager.getConnection(url, config.getUser(), config.getPassword());
                Statement stmt = conn.createStatement()) {

            InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream("schema.sql");
            if (is == null)
                throw new RuntimeException("schema.sql not found");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--") || line.isEmpty())
                    continue; // ignore comments and empty lines
                sb.append(line).append("\n");
            }
            for (String sql : sb.toString().split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    public void initializeDatabase(Connection connection) {
        List<String> statements = loadInitScript();
        executeStatements(connection, statements);
    }

    private List<String> loadInitScript() {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(INIT_SCRIPT)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }

                currentStatement.append(line).append(" ");
                if (line.endsWith(";")) {
                    statements.add(currentStatement.toString().trim());
                    currentStatement = new StringBuilder();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read initialization script", e);
            throw new IllegalStateException("Failed to read initialization script", e);
        }

        return statements;
    }

    private void executeStatements(Connection connection, List<String> statements) {
        try (Statement stmt = connection.createStatement()) {
            for (String sql : statements) {
                try {
                    stmt.execute(sql);
                    LOGGER.fine("Executed SQL: " + sql);
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to execute SQL: " + sql, e);
                    // Continue with next statement
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create statement", e);
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }
}