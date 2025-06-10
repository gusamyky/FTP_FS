package ftp.gusamyky.server.handler.command;

@FunctionalInterface
public interface LoginStateUpdater {
    void update(boolean loggedIn, String username, Integer clientId);
}