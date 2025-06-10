package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.service.ServiceFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Command for handling echo requests.
 * This command simply echoes back the provided message to the client.
 */
public class EchoCommand extends BaseCommand {
    private static final Logger LOGGER = Logger.getLogger(EchoCommand.class.getName());
    private static final String COMMAND_NAME = "ECHO";

    public EchoCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, LoginStateUpdater loginStateUpdater) {
        super(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername, loggedClientId, loginStateUpdater);
    }

    @Override
    public void execute() throws IOException {
        LOGGER.info(String.format("Executing echo command for client %s with message: %s", clientIp, args));
        if (args == null || args.trim().isEmpty()) {
            sendError("ECHO ERROR: No message provided");
            return;
        }

        try {
            sendOk(args.trim());
            LOGGER.info(String.format("Echoed message back to client %s", clientIp));
        } catch (Exception e) {
            LOGGER.severe(String.format("Error during echo for client %s", clientIp));
            sendError("ECHO ERROR: Failed to echo message");
        }
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}