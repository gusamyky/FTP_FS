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

public class UploadCommand extends BaseCommand {
    private static final Logger LOGGER = Logger.getLogger(UploadCommand.class.getName());
    private static final int BUFFER_SIZE = 16384;
    private static final int TIMEOUT_MS = 300000;
    private final InputStream inputStream;

    public UploadCommand(ServiceFactory serviceFactory, BufferedWriter writer, String args, String clientIp,
            boolean loggedIn, String loggedUsername, Integer loggedClientId, InputStream inputStream,
            LoginStateUpdater loginStateUpdater) {
        super(serviceFactory, writer, args, clientIp, loggedIn, loggedUsername, loggedClientId, loginStateUpdater);
        this.inputStream = inputStream;
    }

    @Override
    public void execute() throws IOException {
        if (!validateLogin()) {
            return;
        }

        String filename = args.trim();
        if (filename.isEmpty()) {
            sendError("UPLOAD ERROR: No filename given");
            logOperation("UPLOAD_FAIL:NoFilename");
            return;
        }

        // Send READY response
        writer.write("READY\n");
        writer.flush();
        LOGGER.info(String.format("Sent READY response to client %s for file %s", clientIp, filename));

        // Read file size from input stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String sizeStr = reader.readLine();
        if (sizeStr == null) {
            sendError("UPLOAD ERROR: No file size received");
            logOperation("UPLOAD_FAIL:NoSize");
            return;
        }

        long fileSize;
        try {
            fileSize = Long.parseLong(sizeStr.trim());
            if (fileSize <= 0) {
                sendError("UPLOAD ERROR: Invalid file size");
                logOperation("UPLOAD_FAIL:InvalidSize");
                return;
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, String.format("Invalid file size format received: '%s'", sizeStr));
            sendError("UPLOAD ERROR: Invalid file size format");
            logOperation("UPLOAD_FAIL:InvalidSize");
            return;
        }

        Path filePath = prepareUploadPath(filename);
        LOGGER.info(String.format("[UPLOAD][START] File: '%s', Size: %d bytes, From IP: %s",
                filename, fileSize, clientIp));

        try {
            if (receiveFile(filePath, inputStream, fileSize)) {
                ServerFileModel fileModel = new ServerFileModel(
                        0,
                        filename,
                        fileSize,
                        loggedClientId,
                        LocalDateTime.now());
                serviceFactory.getFileService().saveFile(fileModel);

                sendOk("Upload successful");
                logOperation("UPLOAD_OK: " + filename);
                LOGGER.info(String.format("[UPLOAD][END] File: '%s', Size: %d bytes, From IP: %s",
                        filename, fileSize, clientIp));
            } else {
                // Clean up partial file
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    LOGGER.warning("Failed to delete partial file: " + filePath);
                }
                sendError("UPLOAD ERROR: Failed to receive file");
                logOperation("UPLOAD_FAIL:TransferError");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during file upload: " + e.getMessage(), e);
            sendError("UPLOAD ERROR: " + e.getMessage());
            logOperation("UPLOAD_FAIL:IOError");
        }
    }

    @Override
    public String getCommandName() {
        return "UPLOAD";
    }

    private Path prepareUploadPath(String filename) throws IOException {
        String filesDir = serviceFactory.getServerConfig().getFilesDir();
        Files.createDirectories(Paths.get(filesDir));
        return Paths.get(filesDir, filename);
    }

    private boolean receiveFile(Path filePath, InputStream in, long fileSize) {
        try (OutputStream fileOut = Files.newOutputStream(filePath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long received = 0;
            int read;
            long lastProgressTime = System.currentTimeMillis();
            long lastProgressPercent = 0;

            while (received < fileSize) {
                // Check for timeout
                if (System.currentTimeMillis() - lastProgressTime > TIMEOUT_MS) {
                    LOGGER.severe("Upload timeout - no progress for " + (TIMEOUT_MS / 1000) + " seconds");
                    return false;
                }

                read = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize - received));
                if (read == -1) {
                    LOGGER.severe("End of stream reached before file transfer completed");
                    return false;
                }

                fileOut.write(buffer, 0, read);
                received += read;
                lastProgressTime = System.currentTimeMillis();

                // Log progress every 10%
                long currentPercent = (received * 100) / fileSize;
                if (currentPercent >= lastProgressPercent + 10) {
                    LOGGER.info(String.format("Upload progress: %d%% (%d/%d bytes)",
                            currentPercent, received, fileSize));
                    lastProgressPercent = currentPercent;
                }
            }

            if (received != fileSize) {
                LOGGER.severe(String.format("File size mismatch. Received %d of %d bytes", received, fileSize));
                return false;
            }
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error receiving file: " + filePath, e);
            return false;
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