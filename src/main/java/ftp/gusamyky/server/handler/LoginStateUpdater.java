package ftp.gusamyky.server.handler;

/**
 * Interface for updating the login state of a client.
 * This interface is implemented by classes that need to update the login state
 * of a client, such as the ClientHandler class.
 */
public interface LoginStateUpdater {
    /**
     * Updates the login state of a client.
     *
     * @param loggedIn Whether the client is logged in
     * @param username The username of the logged-in client, or null if not logged
     *                 in
     * @param clientId The ID of the logged-in client, or null if not logged in
     */
    void update(boolean loggedIn, String username, Integer clientId);
}