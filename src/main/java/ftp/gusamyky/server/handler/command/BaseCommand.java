package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.service.ServiceFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Base class for all FTP server commands.
 * This class provides common functionality for all command implementations.
 */
public abstract class BaseCommand implements Command {
    private static final Logger LOGGER = Logger.getLogger(BaseCommand.class.getName());
    protected final ServiceFactory serviceFactory;
    protected final BufferedWriter writer;
    protected final String args;
    protected final String clientIp;
    protected final boolean loggedIn;
    protected final String loggedUsername;
    protected final Integer loggedClientId;
    protected final LoginStateUpdater loginStateUpdater;

    /**
     * Creates a new command instance.
     *
     * @param serviceFactory    The service factory to use
     * @param writer            The writer to use for sending responses
     * @param args              The command arguments
     * @param clientIp          The IP address of the client
     * @param loggedIn          Whether the client is logged in
     * @param loggedUsername    The username of the logged-in client, or null if not
     *                          logged in
     * @param loggedClientId    The ID of the logged-in client, or null if not
     *                          logged in
     * @param loginStateUpdater The login state updater to use
     */
    public BaseCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, LoginStateUpdater loginStateUpdater) {
        this.serviceFactory = serviceFactory;
        this.writer = writer;
        this.args = args;
        this.clientIp = clientIp;
        this.loggedIn = loggedIn;
        this.loggedUsername = loggedUsername;
        this.loggedClientId = loggedClientId;
        this.loginStateUpdater = loginStateUpdater;
        LOGGER.info(String.format("Created command %s for client %s", getCommandName(), clientIp));
    }

    /**
     * Sends an error message to the client.
     *
     * @param message The error message to send
     * @throws IOException If an I/O error occurs
     */
    protected void sendError(String message) throws IOException {
        LOGGER.warning(String.format("Error for client %s: %s", clientIp, message));
        writer.write("ERROR: " + message + "\n");
        writer.flush();
    }

    /**
     * Sends a success message to the client.
     *
     * @param message The success message to send
     * @throws IOException If an I/O error occurs
     */
    protected void sendOk(String message) throws IOException {
        LOGGER.info(String.format("Success for client %s: %s", clientIp, message));
        writer.write("OK: " + message + "\n");
        writer.flush();
    }

    /**
     * Updates the login state of the client.
     *
     * @param loggedIn Whether the client is logged in
     * @param username The username of the logged-in client, or null if not logged
     *                 in
     * @param clientId The ID of the logged-in client, or null if not logged in
     */
    protected void updateLoginState(boolean loggedIn, String username, Integer clientId) {
        if (loginStateUpdater != null) {
            LOGGER.info(String.format("Updating login state for client %s - Logged in: %b, Username: %s, Client ID: %d",
                    clientIp, loggedIn, username, clientId));
            loginStateUpdater.update(loggedIn, username, clientId);
        } else {
            LOGGER.warning(String.format("No login state updater available for client %s", clientIp));
        }
    }

    /**
     * Validates that the client is logged in.
     *
     * @return true if the client is logged in, false otherwise
     * @throws IOException If an I/O error occurs
     */
    protected boolean validateLogin() throws IOException {
        if (!loggedIn || loggedUsername == null || loggedClientId == null) {
            LOGGER.warning(String.format("Client %s not logged in", clientIp));
            sendError("Not logged in");
            return false;
        }
        return true;
    }
}