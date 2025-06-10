package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.service.ServiceFactory;
import java.io.BufferedWriter;
import java.io.IOException;

public class UnknownCommand extends BaseCommand {
    public UnknownCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, LoginStateUpdater loginStateUpdater) {
        super(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername, loggedClientId, loginStateUpdater);
    }

    @Override
    public void execute() throws IOException {
        sendError("Unknown command");
    }

    @Override
    public String getCommandName() {
        return "UNKNOWN";
    }
}