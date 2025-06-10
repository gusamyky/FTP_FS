package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.common.model.OperationHistoryModel;
import ftp.gusamyky.server.service.ServiceFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Command for handling user logout.
 * This command updates the login state to log out the current user.
 */
public class LogoutCommand extends BaseCommand {
    private static final Logger LOGGER = Logger.getLogger(LogoutCommand.class.getName());
    private static final String COMMAND_NAME = "LOGOUT";

    /**
     * Creates a new logout command instance.
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
    public LogoutCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, LoginStateUpdater loginStateUpdater) {
        super(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername, loggedClientId, loginStateUpdater);
        LOGGER.info(String.format("Created logout command for client %s", clientIp));
    }

    @Override
    public void execute() throws IOException {
        LOGGER.info(String.format("Executing logout command for client %s", clientIp));
        if (!loggedIn) {
            LOGGER.warning(String.format("Client %s not logged in", clientIp));
            sendError("Not logged in");
            return;
        }

        try {
            LOGGER.info(String.format("User %s logged out successfully", loggedUsername));
            sendOk("Logout successful");
            updateLoginState(false, null, null);
            logOperation("LOGOUT_OK");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error during logout for user %s", loggedUsername), e);
            sendError("Error during logout");
        }
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    private void logOperation(String operation) {
        if (loggedClientId != null) {
            String user = loggedUsername != null ? loggedUsername : "unknown";
            String opWithUserAndTime = operation + " [user:" + user + "] [" + LocalDateTime.now() + "]";
            serviceFactory.getHistoryService().addOperation(
                    new OperationHistoryModel(0, loggedClientId, opWithUserAndTime, LocalDateTime.now()));
        }
    }
}