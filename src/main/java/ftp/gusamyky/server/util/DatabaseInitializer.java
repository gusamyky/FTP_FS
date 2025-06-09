package ftp.gusamyky.server.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import ftp.gusamyky.server.config.DatabaseConfig;

public class DatabaseInitializer {
    public static void initialize(DatabaseConfig config) {
        String url = config.getUrlNoDb();
        try (Connection conn = java.sql.DriverManager.getConnection(url, config.getUser(), config.getPassword());
                Statement stmt = conn.createStatement()) {

            // Check if tables already exist
            boolean tablesExist = false;
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES FROM ftp_fs")) {
                if (rs.next()) {
                    tablesExist = true;
                    System.out.println("Database tables already exist, skipping schema initialization");
                }
            }

            if (!tablesExist) {
                System.out.println("Initializing database schema...");
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
                System.out.println("Database schema initialized successfully");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}