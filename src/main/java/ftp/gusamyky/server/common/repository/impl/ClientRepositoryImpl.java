package ftp.gusamyky.server.common.repository.impl;

import ftp.gusamyky.server.common.model.ClientModel;
import ftp.gusamyky.server.common.repository.IClientRepository;
import ftp.gusamyky.server.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ClientRepositoryImpl implements IClientRepository {
    private static final Logger LOGGER = Logger.getLogger(ClientRepositoryImpl.class.getName());
    private final DatabaseConfig config;

    public ClientRepositoryImpl(DatabaseConfig config) {
        this.config = config;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    @Override
    public ClientModel findById(int id) {
        String sql = "SELECT id, username, password FROM Klienci WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ClientModel(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding client by ID: " + id, e);
        }
        return null;
    }

    @Override
    public ClientModel findByUsername(String username) {
        String sql = "SELECT id, username, password FROM Klienci WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ClientModel(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding client by username: " + username, e);
        }
        return null;
    }

    @Override
    public void save(ClientModel client) {
        if (client == null) {
            LOGGER.warning("Attempted to save null client");
            return;
        }

        String sql = client.getId() == 0
                ? "INSERT INTO Klienci (username, password) VALUES (?, ?)"
                : "UPDATE Klienci SET username = ?, password = ? WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, client.getUsername());
            stmt.setString(2, client.getPassword());
            if (client.getId() != 0) {
                stmt.setInt(3, client.getId());
            }
            stmt.executeUpdate();
            LOGGER.info("Successfully saved client: " + client.getUsername());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving client: " + client.getUsername(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Klienci WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                LOGGER.info("Successfully deleted client with ID: " + id);
            } else {
                LOGGER.warning("No client found with ID: " + id);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting client with ID: " + id, e);
        }
    }

    public void updateLastLogin(int clientId) {
        String sql = "UPDATE Klienci SET last_login = NOW() WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                LOGGER.info("Updated last login for client ID: " + clientId);
            } else {
                LOGGER.warning("No client found with ID: " + clientId);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating last login for client ID: " + clientId, e);
        }
    }
}