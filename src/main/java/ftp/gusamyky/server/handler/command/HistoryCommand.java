package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.common.model.ClientModel;
import ftp.gusamyky.server.common.model.OperationHistoryModel;
import ftp.gusamyky.server.common.service.IUserService;
import ftp.gusamyky.server.service.ServiceFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class HistoryCommand extends BaseCommand {
    private static final String COMMAND_NAME = "HISTORY";

    public HistoryCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, LoginStateUpdater loginStateUpdater) {
        super(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername, loggedClientId, loginStateUpdater);
    }

    @Override
    public void execute() throws IOException {
        String username = args.trim();
        if (username.isEmpty()) {
            sendError("HISTORY ERROR: No username given");
            return;
        }

        IUserService userService = serviceFactory.getUserService();
        ClientModel client = userService.findUserByUsername(username);
        if (client == null) {
            sendError("HISTORY ERROR: User not found");
            return;
        }

        List<OperationHistoryModel> ops = serviceFactory.getHistoryService().getHistoryByClientId(client.getId());
        StringBuilder response = new StringBuilder("HISTORY: ");
        if (ops.isEmpty()) {
            response.append("(no operations)");
        } else {
            for (int i = 0; i < ops.size(); i++) {
                var op = ops.get(i);
                response.append(op.getTimestamp()).append(" | ").append(op.getOperation());
                if (i < ops.size() - 1) {
                    response.append("; ");
                }
            }
        }
        writer.write(response.toString() + "\n");
        writer.flush();
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}