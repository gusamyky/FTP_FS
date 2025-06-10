package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.service.ServiceFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Factory class for creating command instances.
 * This class is responsible for creating the appropriate command instance based
 * on the command name.
 */
public class CommandFactory {
    private static final Logger LOGGER = Logger.getLogger(CommandFactory.class.getName());
    private final ServiceFactory serviceFactory;
    private final BufferedWriter writer;
    private final String clientIp;
    private final LoginStateUpdater loginStateUpdater;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private boolean loggedIn;
    private String loggedUsername;
    private Integer loggedClientId;

    /**
     * Creates a new command factory.
     *
     * @param serviceFactory    The service factory to use
     * @param writer            The writer to use for sending responses
     * @param clientIp          The IP address of the client
     * @param loggedIn          Whether the client is logged in
     * @param loggedUsername    The username of the logged-in client, or null if not
     *                          logged in
     * @param loggedClientId    The ID of the logged-in client, or null if not
     *                          logged in
     * @param loginStateUpdater The login state updater to use
     * @param inputStream       The input stream to use for reading data
     * @param outputStream      The output stream to use for writing data
     */
    public CommandFactory(ServiceFactory serviceFactory, BufferedWriter writer, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, LoginStateUpdater loginStateUpdater,
            InputStream inputStream, OutputStream outputStream) {
        this.serviceFactory = serviceFactory;
        this.writer = writer;
        this.clientIp = clientIp;
        this.loginStateUpdater = loginStateUpdater;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.loggedIn = loggedIn;
        this.loggedUsername = loggedUsername;
        this.loggedClientId = loggedClientId;
        LOGGER.info(String.format("Created command factory for client %s", clientIp));
    }

    /**
     * Creates a command instance based on the command name.
     *
     * @param commandName The name of the command to create
     * @param args        The command arguments
     * @return The created command instance
     * @throws IOException If an I/O error occurs
     */
    public Command createCommand(String commandName, String args) throws IOException {
        LOGGER.info(String.format("Creating command: %s, Logged in: %b, Username: %s, Client ID: %d",
                commandName, loggedIn, loggedUsername, loggedClientId));

        return switch (commandName) {
            case "LOGIN" -> new LoginCommand(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername,
                    loggedClientId, loginStateUpdater);
            case "REGISTER" -> new RegisterCommand(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername,
                    loggedClientId, loginStateUpdater);
            case "LOGOUT" -> new LogoutCommand(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername,
                    loggedClientId, loginStateUpdater);
            case "UPLOAD" -> new UploadCommand(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername,
                    loggedClientId, inputStream, loginStateUpdater);
            case "DOWNLOAD" -> new DownloadCommand(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername,
                    loggedClientId, outputStream, loginStateUpdater);
            case "LIST" -> new ListCommand(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername,
                    loggedClientId, loginStateUpdater);
            case "HISTORY" -> new HistoryCommand(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername,
                    loggedClientId, loginStateUpdater);
            case "REPORT" -> new ReportCommand(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername,
                    loggedClientId, loginStateUpdater);
            case "ECHO" -> new EchoCommand(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername,
                    loggedClientId, loginStateUpdater);
            default -> {
                LOGGER.warning("Unknown command: " + commandName);
                throw new IOException("Unknown command: " + commandName);
            }
        };
    }

    /**
     * Updates the login state.
     *
     * @param loggedIn       Whether the client is logged in
     * @param loggedUsername The username of the logged-in client, or null if not
     *                       logged in
     * @param loggedClientId The ID of the logged-in client, or null if not logged
     *                       in
     */
    public void updateLoginState(boolean loggedIn, String loggedUsername, Integer loggedClientId) {
        this.loggedIn = loggedIn;
        this.loggedUsername = loggedUsername;
        this.loggedClientId = loggedClientId;
        LOGGER.info(String.format(
                "Updated login state in CommandFactory for client %s - Logged in: %b, Username: %s, Client ID: %d",
                clientIp, loggedIn, loggedUsername, loggedClientId));
    }
}