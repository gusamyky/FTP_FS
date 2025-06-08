package ftp.gusamyky.server.database_handler.repository;

import ftp.gusamyky.server.common.model.OperationHistoryModel;
import ftp.gusamyky.server.common.repository.IHistoryRepository;
import ftp.gusamyky.server.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryRepositoryImpl implements IHistoryRepository {
    private final DatabaseConfig config;

    public HistoryRepositoryImpl(DatabaseConfig config) {
        this.config = config;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    @Override
    public List<OperationHistoryModel> findByClientId(int clientId) {
        List<OperationHistoryModel> ops = new ArrayList<>();
        String sql = "SELECT * FROM HistoriaOperacji WHERE client_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ops.add(new OperationHistoryModel(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getString("operation"),
                            rs.getTimestamp("timestamp").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ops;
    }

    @Override
    public void save(OperationHistoryModel operation) {
        String sql = "INSERT INTO HistoriaOperacji (client_id, operation, timestamp) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, operation.getClientId());
            stmt.setString(2, operation.getOperation());
            stmt.setTimestamp(3, Timestamp.valueOf(operation.getTimestamp()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}