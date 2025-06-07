package common.db;

import common.model.OperationHistory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OperationHistoryDAO {
    private final Connection connection;

    public OperationHistoryDAO(Connection connection) {
        this.connection = connection;
    }

    public void addOperation(OperationHistory op) throws SQLException {
        String sql = "INSERT INTO HistoriaOperacji (client_id, operation, timestamp) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, op.getClientId());
            stmt.setString(2, op.getOperation());
            stmt.setTimestamp(3, Timestamp.valueOf(op.getTimestamp()));
            stmt.executeUpdate();
        }
    }

    public List<OperationHistory> getOperationsByClientId(int clientId) throws SQLException {
        List<OperationHistory> ops = new ArrayList<>();
        String sql = "SELECT * FROM HistoriaOperacji WHERE client_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ops.add(new OperationHistory(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getString("operation"),
                            rs.getTimestamp("timestamp").toLocalDateTime()));
                }
            }
        }
        return ops;
    }

    public List<OperationHistory> getAllOperations() throws SQLException {
        List<OperationHistory> ops = new ArrayList<>();
        String sql = "SELECT * FROM HistoriaOperacji";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ops.add(new OperationHistory(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("operation"),
                        rs.getTimestamp("timestamp").toLocalDateTime()));
            }
        }
        return ops;
    }

    public void deleteOperation(int id) throws SQLException {
        String sql = "DELETE FROM HistoriaOperacji WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}