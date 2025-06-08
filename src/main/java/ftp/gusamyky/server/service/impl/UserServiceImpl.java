package ftp.gusamyky.server.service.impl;

import ftp.gusamyky.server.common.model.ClientModel;
import ftp.gusamyky.server.common.repository.IClientRepository;
import ftp.gusamyky.server.common.service.IUserService;
import ftp.gusamyky.server.util.PasswordUtil;

public class UserServiceImpl implements IUserService {
    private final IClientRepository clientRepository;

    public UserServiceImpl(IClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public ClientModel findUserByUsername(String username) {
        return clientRepository.findByUsername(username);
    }

    @Override
    public boolean checkPassword(String username, String password) {
        ClientModel client = clientRepository.findByUsername(username);
        if (client == null)
            return false;
        return PasswordUtil.verifyPassword(password, client.getPassword());
    }

    public String register(String username, String password) {
        if (clientRepository.findByUsername(username) != null) {
            return "REGISTER ERROR: Username already exists";
        }
        String hash = PasswordUtil.hashPassword(password);
        ClientModel client = new ClientModel(0, username, hash);
        clientRepository.save(client);
        return "REGISTER OK";
    }

    public String login(String username, String password) {
        ClientModel client = clientRepository.findByUsername(username);
        if (client == null) {
            return "LOGIN ERROR: User not found";
        }
        if (!PasswordUtil.verifyPassword(password, client.getPassword())) {
            return "LOGIN ERROR: Invalid password";
        }
        clientRepository.updateLastLogin(client.getId());
        return "LOGIN OK";
    }
}