package common.db;

import common.model.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class ClientDAO {
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
        }
    }

    public Client getClientByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM Klienci WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Client(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"));
                }
            }
        }
        return null;
    }

    public boolean checkPassword(String username, String plainPassword) throws SQLException {
        Client client = getClientByUsername(username);
        if (client == null)
            return false;
        return BCrypt.checkpw(plainPassword, client.getPassword());
    }

    public List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM Klienci";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                clients.add(new Client(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")));
            }
        }
        return clients;
    }

    public void updateClient(Client client) throws SQLException {
        String sql = "UPDATE Klienci SET username = ?, password = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, client.getUsername());
            stmt.setString(2, client.getPassword());
            stmt.setInt(3, client.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteClient(int id) throws SQLException {
        String sql = "DELETE FROM Klienci WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}