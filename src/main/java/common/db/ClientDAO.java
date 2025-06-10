package common.db;

import common.model.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.mindrot.jbcrypt.BCrypt;

public class ClientDAO {
    private static final Logger LOGGER = Logger.getLogger(ClientDAO.class.getName());
    private final Connection connection;

    public ClientDAO(Connection connection) {
        this.connection = connection;
    }

    public void addClient(Client client) throws SQLException {
        String sql = "INSERT INTO Klienci (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, client.getUsername());
            stmt.setString(2, client.getPassword());
            stmt.executeUpdate();
            LOGGER.info("Added new client: " + client.getUsername());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add client: " + client.getUsername(), e);
            throw e;
        }
    }

    public Client getClientByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM Klienci WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClient(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get client by username: " + username, e);
            throw e;
        }
        return null;
    }

    public boolean checkPassword(String username, String plainPassword) throws SQLException {
        Client client = getClientByUsername(username);
        if (client == null) {
            LOGGER.warning("Password check failed: Client not found: " + username);
            return false;
        }
        boolean passwordValid = BCrypt.checkpw(plainPassword, client.getPassword());
        if (!passwordValid) {
            LOGGER.warning("Password check failed: Invalid password for client: " + username);
        }
        return passwordValid;
    }

    public List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM Klienci";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get all clients", e);
            throw e;
        }
        return clients;
    }

    public void updateClient(Client client) throws SQLException {
        String sql = "UPDATE Klienci SET username = ?, password = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, client.getUsername());
            stmt.setString(2, client.getPassword());
            stmt.setInt(3, client.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.warning("No client found to update with ID: " + client.getId());
            } else {
                LOGGER.info("Updated client: " + client.getUsername());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update client: " + client.getUsername(), e);
            throw e;
        }
    }

    public void deleteClient(int id) throws SQLException {
        String sql = "DELETE FROM Klienci WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.warning("No client found to delete with ID: " + id);
            } else {
                LOGGER.info("Deleted client with ID: " + id);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete client with ID: " + id, e);
            throw e;
        }
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"));
    }
}