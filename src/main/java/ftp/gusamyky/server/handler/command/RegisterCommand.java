package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.common.service.IUserService;
import ftp.gusamyky.server.common.model.ClientModel;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Command for handling user registration.
 * This command creates a new user account and logs them in if successful.
 */
public class RegisterCommand extends BaseCommand {
    private static final Logger LOGGER = Logger.getLogger(RegisterCommand.class.getName());
    private static final String COMMAND_NAME = "REGISTER";
    private final IUserService userService;

    /**
     * Creates a new register command instance.
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
    public RegisterCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, LoginStateUpdater loginStateUpdater) {
        super(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername, loggedClientId, loginStateUpdater);
        this.userService = serviceFactory.getUserService();
        LOGGER.info(String.format("Created register command for client %s", clientIp));
    }

    @Override
    public void execute() throws IOException {
        LOGGER.info(String.format("Executing register command for client %s", clientIp));
        if (loggedIn) {
            LOGGER.warning(String.format("Client %s already logged in as %s", clientIp, loggedUsername));
            sendError("Already logged in");
            return;
        }

        String[] parts = args.split(" ");
        if (parts.length != 2) {
            LOGGER.warning(String.format("Invalid register arguments from client %s: %s", clientIp, args));
            sendError("Invalid register arguments");
            return;
        }

        String username = parts[0];
        String password = parts[1];

        try {
            String result = userService.register(username, password);
            if (result.equals("REGISTER OK")) {
                LOGGER.info(String.format("User %s registered successfully", username));
                ClientModel client = userService.findUserByUsername(username);
                if (client != null) {
                    LOGGER.info(String.format("User %s logged in after registration", username));
                    sendOk("Registration successful");
                    updateLoginState(true, username, client.getId());
                } else {
                    LOGGER.severe(String.format("User %s not found after successful registration", username));
                    sendError("Registration successful but login failed");
                    updateLoginState(false, null, null);
                }
            } else {
                LOGGER.warning(String.format("Failed registration attempt for user %s", username));
                sendError(result.replace("REGISTER ERROR: ", ""));
                updateLoginState(false, null, null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error during registration for user %s", username), e);
            sendError("Error during registration");
            updateLoginState(false, null, null);
        }
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}