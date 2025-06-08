package ftp.gusamyky.server.database_handler.repository;

import ftp.gusamyky.server.common.model.ClientModel;
import ftp.gusamyky.server.common.repository.IClientRepository;
import ftp.gusamyky.server.config.DatabaseConfig;
import common.db.ClientDAO;
import common.model.Client;
import java.sql.Connection;
import java.sql.DriverManager;

public class ClientRepositoryImpl implements IClientRepository {
    private final DatabaseConfig config;

    public ClientRepositoryImpl(DatabaseConfig config) {
        this.config = config;
    }

    private Connection getConnection() throws Exception {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    private ClientModel toModel(Client client) {
        if (client == null)
            return null;
        return new ClientModel(client.getId(), client.getUsername(), client.getPassword());
    }

    private Client toEntity(ClientModel model) {
        if (model == null)
            return null;
        return new Client(model.getId(), model.getUsername(), model.getPassword());
    }

    @Override
    public ClientModel findById(int id) {
        try (Connection conn = getConnection()) {
            ClientDAO dao = new ClientDAO(conn);
            for (Client c : dao.getAllClients()) {
                if (c.getId() == id)
                    return toModel(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ClientModel findByUsername(String username) {
        try (Connection conn = getConnection()) {
            ClientDAO dao = new ClientDAO(conn);
            return toModel(dao.getClientByUsername(username));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void save(ClientModel client) {
        try (Connection conn = getConnection()) {
            ClientDAO dao = new ClientDAO(conn);
            if (client.getId() == 0) {
                dao.addClient(toEntity(client));
            } else {
                dao.updateClient(toEntity(client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = getConnection()) {
            ClientDAO dao = new ClientDAO(conn);
            dao.deleteClient(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLastLogin(int clientId) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE Klienci SET last_login = NOW() WHERE id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, clientId);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}