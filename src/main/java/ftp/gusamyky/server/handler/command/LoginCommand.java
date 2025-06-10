package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.common.service.IUserService;
import ftp.gusamyky.server.common.model.ClientModel;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Command for handling user login.
 * This command validates user credentials and updates the login state if
 * successful.
 */
public class LoginCommand extends BaseCommand {
    private static final Logger LOGGER = Logger.getLogger(LoginCommand.class.getName());
    private static final String COMMAND_NAME = "LOGIN";
    private final IUserService userService;

    /**
     * Creates a new login command instance.
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
    public LoginCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, LoginStateUpdater loginStateUpdater) {
        super(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername, loggedClientId, loginStateUpdater);
        this.userService = serviceFactory.getUserService();
        LOGGER.info(String.format("Created login command for client %s", clientIp));
    }

    @Override
    public void execute() throws IOException {
        LOGGER.info(String.format("Executing login command for client %s", clientIp));
        if (loggedIn) {
            LOGGER.warning(String.format("Client %s already logged in as %s", clientIp, loggedUsername));
            sendError("Already logged in");
            return;
        }

        String[] parts = args.split(" ");
        if (parts.length != 2) {
            LOGGER.warning(String.format("Invalid login arguments from client %s: %s", clientIp, args));
            sendError("Invalid login arguments");
            return;
        }

        String username = parts[0];
        String password = parts[1];

        try {
            String result = userService.login(username, password);
            if (result.equals("LOGIN OK")) {
                LOGGER.info(String.format("User %s logged in successfully", username));
                ClientModel client = userService.findUserByUsername(username);
                if (client != null) {
                    sendOk("Login successful");
                    updateLoginState(true, username, client.getId());
                } else {
                    LOGGER.severe(String.format("User %s not found after successful login", username));
                    sendError("Login successful but state update failed");
                    updateLoginState(false, null, null);
                }
            } else {
                LOGGER.warning(String.format("Failed login attempt for user %s", username));
                sendError(result.replace("LOGIN ERROR: ", ""));
                updateLoginState(false, null, null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error during login for user %s", username), e);
            sendError("Error during login");
            updateLoginState(false, null, null);
        }
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}