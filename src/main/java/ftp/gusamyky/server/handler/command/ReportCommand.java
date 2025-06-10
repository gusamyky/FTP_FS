package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.util.ReportExportUtil;
import java.io.BufferedWriter;
import java.io.IOException;

public class ReportCommand extends BaseCommand {
    public ReportCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, LoginStateUpdater loginStateUpdater) {
        super(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername, loggedClientId, loginStateUpdater);
    }

    @Override
    public void execute() throws IOException {
        if (!validateLogin()) {
            return;
        }

        String path = "report.csv";
        ReportExportUtil.exportToCsv(serviceFactory.getHistoryService().getHistoryByClientId(loggedClientId), path);
        writer.write("Report generated successfully: " + path + "\n");
        writer.flush();
    }

    @Override
    public String getCommandName() {
        return "REPORT";
    }
}