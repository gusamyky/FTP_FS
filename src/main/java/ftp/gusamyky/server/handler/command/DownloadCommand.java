package ftp.gusamyky.server.handler.command;

import ftp.gusamyky.server.common.model.ServerFileModel;
import ftp.gusamyky.server.common.model.OperationHistoryModel;
import ftp.gusamyky.server.service.ServiceFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.SocketException;

public class DownloadCommand extends BaseCommand {
    private static final Logger LOGGER = Logger.getLogger(DownloadCommand.class.getName());
    private static final String COMMAND_NAME = "DOWNLOAD";
    private static final int BUFFER_SIZE = 16384;
    private final OutputStream outputStream;

    public DownloadCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, OutputStream outputStream,
            LoginStateUpdater loginStateUpdater) {
        super(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername, loggedClientId, loginStateUpdater);
        this.outputStream = outputStream;
    }

    @Override
    public void execute() throws IOException {
        if (!validateLogin()) {
            return;
        }

        String filename = args.trim();
        if (filename.isEmpty()) {
            sendError("DOWNLOAD ERROR: No filename given");
            logOperation("DOWNLOAD_FAIL:NoFilename");
            return;
        }

        ServerFileModel file = serviceFactory.getFileService().getFileByName(filename);
        if (file == null) {
            sendError("DOWNLOAD ERROR: File not found");
            logOperation("DOWNLOAD_FAIL:FileNotFound");
            return;
        }

        if (file.getOwnerId() != loggedClientId) {
            sendError("DOWNLOAD ERROR: Access denied");
            logOperation("DOWNLOAD_FAIL:AccessDenied");
            return;
        }

        Path filePath = Paths.get(serviceFactory.getServerConfig().getFilesDir(), filename);
        if (!Files.exists(filePath)) {
            sendError("DOWNLOAD ERROR: File not found on server");
            logOperation("DOWNLOAD_FAIL:FileNotFoundOnServer");
            return;
        }

        long fileSize = Files.size(filePath);
        writer.write(fileSize + "\n");
        writer.flush();

        LOGGER.info(String.format("[DOWNLOAD][START] File: '%s', Size: %d bytes, To IP: %s",
                filename, fileSize, clientIp));

        try {
            if (sendFile(filePath, fileSize)) {
                logOperation("DOWNLOAD_OK: " + filename);
                LOGGER.info(String.format("[DOWNLOAD][END] File: '%s', Size: %d bytes, To IP: %s",
                        filename, fileSize, clientIp));
            } else {
                sendError("DOWNLOAD ERROR: Failed to send file");
                logOperation("DOWNLOAD_FAIL:TransferError");
            }
        } catch (SocketException e) {
            LOGGER.info(String.format("Client %s disconnected during file transfer", clientIp));
            throw e;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format("Error sending file to client %s", clientIp), e);
            sendError("DOWNLOAD ERROR: Failed to send file");
            logOperation("DOWNLOAD_FAIL:TransferError");
        }
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    private boolean sendFile(Path filePath, long fileSize) throws IOException {
        try (InputStream fileIn = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long sent = 0;
            int read;
            long lastProgressPercent = 0;

            while (sent < fileSize) {
                read = fileIn.read(buffer, 0, (int) Math.min(buffer.length, fileSize - sent));
                if (read == -1) {
                    LOGGER.severe("End of stream reached before file transfer completed");
                    return false;
                }

                try {
                    outputStream.write(buffer, 0, read);
                    outputStream.flush();
                    sent += read;

                    // Log progress every 10%
                    long currentPercent = (sent * 100) / fileSize;
                    if (currentPercent >= lastProgressPercent + 10) {
                        LOGGER.info(String.format("Download progress: %d%% (%d/%d bytes)",
                                currentPercent, sent, fileSize));
                        lastProgressPercent = currentPercent;
                    }
                } catch (SocketException e) {
                    LOGGER.info(String.format("Client %s disconnected during file transfer", clientIp));
                    throw e;
                }
            }

            if (sent != fileSize) {
                LOGGER.severe(String.format("File size mismatch. Sent %d of %d bytes", sent, fileSize));
                return false;
            }
            return true;
        }
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