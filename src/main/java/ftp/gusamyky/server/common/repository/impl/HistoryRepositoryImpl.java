package ftp.gusamyky.server.common.repository.impl;

import ftp.gusamyky.server.common.model.OperationHistoryModel;
import ftp.gusamyky.server.common.repository.IHistoryRepository;
import ftp.gusamyky.server.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class HistoryRepositoryImpl implements IHistoryRepository {
    private static final Logger LOGGER = Logger.getLogger(HistoryRepositoryImpl.class.getName());
    private final DatabaseConfig config;

    public HistoryRepositoryImpl(DatabaseConfig config) {
        this.config = config;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    @Override
    public List<OperationHistoryModel> findByClientId(int clientId) {
        String sql = "SELECT id, client_id, operation, timestamp FROM HistoriaOperacji WHERE client_id = ? ORDER BY timestamp DESC";
        List<OperationHistoryModel> history = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new OperationHistoryModel(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getString("operation"),
                            rs.getTimestamp("timestamp").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding history for client ID: " + clientId, e);
        }
        return history;
    }

    @Override
    public void save(OperationHistoryModel history) {
        if (history == null) {
            LOGGER.warning("Attempted to save null history record");
            return;
        }

        String sql = "INSERT INTO HistoriaOperacji (client_id, operation, timestamp) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, history.getClientId());
            stmt.setString(2, history.getOperation());
            stmt.setObject(3, history.getTimestamp());
            stmt.executeUpdate();
            LOGGER.info("Successfully saved operation history for client ID: " + history.getClientId());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving operation history for client ID: " + history.getClientId(), e);
        }
    }
}