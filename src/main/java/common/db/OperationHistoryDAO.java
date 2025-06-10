package common.db;

import common.model.OperationHistory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class OperationHistoryDAO {
    private static final Logger LOGGER = Logger.getLogger(OperationHistoryDAO.class.getName());
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
            LOGGER.info("Added operation history for client ID: " + op.getClientId());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add operation history for client ID: " + op.getClientId(), e);
            throw e;
        }
    }

    public List<OperationHistory> getOperationsByClientId(int clientId) throws SQLException {
        List<OperationHistory> ops = new ArrayList<>();
        String sql = "SELECT * FROM HistoriaOperacji WHERE client_id = ? ORDER BY timestamp DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ops.add(mapResultSetToOperationHistory(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get operations for client ID: " + clientId, e);
            throw e;
        }
        return ops;
    }

    public List<OperationHistory> getAllOperations() throws SQLException {
        List<OperationHistory> ops = new ArrayList<>();
        String sql = "SELECT * FROM HistoriaOperacji ORDER BY timestamp DESC";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ops.add(mapResultSetToOperationHistory(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get all operations", e);
            throw e;
        }
        return ops;
    }

    public void deleteOperation(int id) throws SQLException {
        String sql = "DELETE FROM HistoriaOperacji WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.warning("No operation found to delete with ID: " + id);
            } else {
                LOGGER.info("Deleted operation with ID: " + id);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete operation with ID: " + id, e);
            throw e;
        }
    }

    private OperationHistory mapResultSetToOperationHistory(ResultSet rs) throws SQLException {
        return new OperationHistory(
                rs.getInt("id"),
                rs.getInt("client_id"),
                rs.getString("operation"),
                rs.getTimestamp("timestamp").toLocalDateTime());
    }
}