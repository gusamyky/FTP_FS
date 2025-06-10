package ftp.gusamyky.server.common.service;

import ftp.gusamyky.server.common.model.ClientModel;

public interface IUserService {
    ClientModel findUserByUsername(String username);

    boolean checkPassword(String username, String password);

    String login(String username, String password);

    String register(String username, String password);
}