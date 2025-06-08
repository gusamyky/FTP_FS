package ftp.gusamyky.server.common.repository;

import ftp.gusamyky.server.common.model.ClientModel;

public interface IClientRepository {
    ClientModel findById(int id);

    ClientModel findByUsername(String username);

    void save(ClientModel client);

    void delete(int id);

    void updateLastLogin(int clientId);
}