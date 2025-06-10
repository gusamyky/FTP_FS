package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.common.model.ServerFileModel;
import ftp.gusamyky.server.common.model.OperationHistoryModel;
import ftp.gusamyky.server.common.service.IFileService;
import ftp.gusamyky.server.service.ServiceFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ListCommand extends BaseCommand {
    public ListCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, LoginStateUpdater loginStateUpdater) {
        super(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername, loggedClientId, loginStateUpdater);
    }

    @Override
    public void execute() throws IOException {
        if (!validateLogin()) {
            return;
        }

        IFileService fileService = serviceFactory.getFileService();
        List<ServerFileModel> files = fileService.listFilesByOwner(loggedClientId);

        StringBuilder response = new StringBuilder("FILES:");
        if (files.isEmpty()) {
            response.append(" (no files)");
        } else {
            for (var f : files) {
                response.append(" ").append(f.getFilename());
            }
        }

        writer.write(response.toString() + "\n");
        writer.flush();
        logOperation("LIST");
    }

    @Override
    public String getCommandName() {
        return "LIST";
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