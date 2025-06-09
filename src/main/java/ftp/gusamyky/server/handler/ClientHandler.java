package ftp.gusamyky.server.handler;

import ftp.gusamyky.server.common.model.ClientModel;
import ftp.gusamyky.server.common.model.OperationHistoryModel;
import ftp.gusamyky.server.common.service.IFileService;
import ftp.gusamyky.server.common.service.IHistoryService;
import ftp.gusamyky.server.common.service.IUserService;
import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.util.ReportExportUtil;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket socket;
    private final ServiceFactory serviceFactory;
    private boolean loggedIn = false;
    private String loggedUsername = null;
    private Integer loggedClientId = null;
    private final String filesDir;

    public ClientHandler(Socket socket, ServiceFactory serviceFactory) {
        this.socket = socket;
        this.serviceFactory = serviceFactory;
        this.filesDir = serviceFactory.getServerConfig().getFilesDir();
    }

    @Override
    public void run() {
        String clientIp = socket.getInetAddress().getHostAddress();
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream()) {
            writer.write("Welcome to FTP_FS server!\n");
            writer.flush();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(" ", 2);
                String cmd = parts[0].toUpperCase();
                String args = parts.length > 1 ? parts[1] : "";
                switch (cmd) {
                    case "UPLOAD":
                        if (!loggedIn) {
                            sendError(writer, "ERROR: Not logged in");
                            break;
                        }
                        writer.write("READY\n");
                        writer.flush();
                        receiveFile(args, in, reader, writer, clientIp);
                        break;
                    case "DOWNLOAD":
                        if (!loggedIn) {
                            sendError(writer, "ERROR: Not logged in");
                            break;
                        }
                        sendFile(args, out, writer, clientIp);
                        break;
                    case "ECHO":
                        writer.write("ECHO: " + args + "\n");
                        writer.flush();
                        break;
                    case "LOGIN": {
                        String loginResult = handleLogin(args);
                        writer.write(loginResult + "\n");
                        writer.flush();
                        if (loginResult.equals("LOGIN OK")) {
                            loggedIn = true;
                            loggedUsername = args.split(" ")[0];
                            IUserService userService = serviceFactory.getUserService();
                            ClientModel client = userService.findUserByUsername(loggedUsername);
                            loggedClientId = (client != null) ? client.getId() : null;
                        }
                        break;
                    }
                    case "REGISTER":
                        String registerResult = handleRegister(args);
                        writer.write(registerResult + "\n");
                        writer.flush();
                        if (registerResult.equals("REGISTER OK")) {
                            loggedIn = true;
                            loggedUsername = args.split(" ")[0];
                            IUserService userService = serviceFactory.getUserService();
                            ClientModel client = userService.findUserByUsername(loggedUsername);
                            loggedClientId = (client != null) ? client.getId() : null;
                        }
                        break;
                    case "LIST":
                        writer.write(handleList() + "\n");
                        writer.flush();
                        break;
                    case "HISTORY":
                        writer.write(handleHistory(args) + "\n");
                        writer.flush();
                        break;
                    case "REPORT":
                        writer.write(handleReport() + "\n");
                        writer.flush();
                        break;
                    default:
                        writer.write("Unknown command: " + cmd + "\n");
                        writer.flush();
                }
            }
        } catch (IOException e) {
            logger.info("[Server] Client disconnected: {}", clientIp, e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn("[Server] Error closing socket for client {}: {}", clientIp, e.getMessage());
            }
        }
    }

    private boolean isFilenameValid(String filename) {
        // Nie pozwalaj na ../, \ lub puste nazwy plików
        return filename != null && !filename.isBlank() && !filename.contains("..") && !filename.contains("/")
                && !filename.contains("\\");
    }

    private void receiveFile(String filename, InputStream in, BufferedReader reader, BufferedWriter writer,
            String clientIp)
            throws IOException {
        if (!isFilenameValid(filename)) {
            sendError(writer, "UPLOAD ERROR: Invalid filename");
            logOperation(loggedClientId, "UPLOAD_FAIL:InvalidFilename");
            logger.warn("[UPLOAD][FAIL] Invalid filename '{}' from IP: {}", filename, clientIp);
            return;
        }
        if (filename.isEmpty()) {
            sendError(writer, "UPLOAD ERROR: No filename given");
            logOperation(loggedClientId, "UPLOAD_FAIL:NoFilename");
            logger.warn("[UPLOAD][FAIL] No filename given from IP: {}", clientIp);
            return;
        }
        Files.createDirectories(Paths.get(filesDir));
        Path filePath = Paths.get(filesDir, filename);
        String lenLine = reader.readLine();
        long fileSize;
        try {
            fileSize = Long.parseLong(lenLine);
        } catch (NumberFormatException e) {
            sendError(writer, "UPLOAD ERROR: Invalid file size");
            logOperation(loggedClientId, "UPLOAD_FAIL:InvalidFileSize");
            logger.warn("[UPLOAD][FAIL] Invalid file size for '{}' from IP: {}", filename, clientIp);
            return;
        }
        // Ograniczenie rozmiaru pliku do 100MB
        if (fileSize > 104857600) {
            sendError(writer, "UPLOAD ERROR: File too large (max 100MB)");
            logOperation(loggedClientId, "UPLOAD_FAIL:FileTooLarge");
            logger.warn("[UPLOAD][FAIL] File '{}' too large ({} bytes) from IP: {}", filename, fileSize, clientIp);
            return;
        }
        logger.info(
                "[UPLOAD][START] File: '{}', Size: {} bytes, From IP: {}", filename, fileSize, clientIp);
        try (OutputStream fileOut = Files.newOutputStream(filePath)) {
            byte[] buffer = new byte[4096];
            long received = 0;
            while (received < fileSize) {
                int toRead = (int) Math.min(buffer.length, fileSize - received);
                int read = in.read(buffer, 0, toRead);
                if (read == -1)
                    break;
                fileOut.write(buffer, 0, read);
                received += read;
            }
        }
        sendOk(writer, "UPLOAD OK");
        logOperation(loggedClientId, "UPLOAD_OK: " + filename);
        logger.info("[UPLOAD][END] File: '{}', Size: {} bytes, From IP: {}", filename, fileSize, clientIp);
    }

    private void sendFile(String filename, OutputStream out, BufferedWriter writer, String clientIp)
            throws IOException {
        if (!isFilenameValid(filename)) {
            sendError(writer, "DOWNLOAD ERROR: Invalid filename");
            logOperation(loggedClientId, "DOWNLOAD_FAIL:InvalidFilename");
            logger.warn("[DOWNLOAD][FAIL] Invalid filename '{}' for IP: {}", filename, clientIp);
            return;
        }
        if (filename.isEmpty()) {
            sendError(writer, "DOWNLOAD ERROR: No filename given");
            logOperation(loggedClientId, "DOWNLOAD_FAIL:NoFilename");
            logger.warn("[DOWNLOAD][FAIL] No filename given to IP: {}", clientIp);
            return;
        }
        Path filePath = Paths.get(filesDir, filename);
        if (!Files.exists(filePath)) {
            sendError(writer, "DOWNLOAD ERROR: File not found");
            logOperation(loggedClientId, "DOWNLOAD_FAIL:FileNotFound");
            logger.warn("[DOWNLOAD][FAIL] File not found: '{}' for IP: {}", filename, clientIp);
            return;
        }
        long fileSize = Files.size(filePath);
        writer.write(fileSize + "\n");
        writer.flush();
        logger.info(
                "[DOWNLOAD][START] File: '{}', Size: {} bytes, To IP: {}", filename, fileSize, clientIp);
        try (InputStream fileIn = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
        logOperation(loggedClientId, "DOWNLOAD_OK: " + filename);
        logger.info("[DOWNLOAD][END] File: '{}', Size: {} bytes, To IP: {}", filename, fileSize, clientIp);
    }

    private String handleRegister(String args) {
        IUserService userService = serviceFactory.getUserService();
        String[] tokens = args.split(" ");
        if (tokens.length != 2)
            return "REGISTER ERROR: Usage REGISTER <username> <password>";
        String username = tokens[0];
        String password = tokens[1];
        // delegacja do serwisu
        return ((ftp.gusamyky.server.service.impl.UserServiceImpl) userService).register(username, password);
    }

    private String handleLogin(String args) {
        IUserService userService = serviceFactory.getUserService();
        String[] tokens = args.split(" ");
        if (tokens.length != 2)
            return "LOGIN ERROR: Usage LOGIN <username> <password>";
        String username = tokens[0];
        String password = tokens[1];
        // delegacja do serwisu
        return ((ftp.gusamyky.server.service.impl.UserServiceImpl) userService).login(username, password);
    }

    private String handleList() {
        IFileService fileService = serviceFactory.getFileService();
        if (loggedClientId == null) {
            return "LIST ERROR: Not logged in";
        }
        List<ftp.gusamyky.server.common.model.ServerFileModel> files = fileService.listFilesByOwner(loggedClientId);
        if (files.isEmpty())
            return "FILES: (brak plików)";
        StringBuilder sb = new StringBuilder("FILES:");
        for (var f : files) {
            sb.append(" ").append(f.getFilename());
        }
        logOperation(loggedClientId, "LIST");
        return sb.toString();
    }

    private String handleHistory(String args) {
        IUserService userService = serviceFactory.getUserService();
        IHistoryService historyService = serviceFactory.getHistoryService();
        String username = args.trim();
        if (username.isEmpty())
            return "HISTORY ERROR: No username given";
        ClientModel client = userService.findUserByUsername(username);
        if (client == null)
            return "HISTORY ERROR: User not found";
        List<OperationHistoryModel> ops = historyService.getHistoryByClientId(client.getId());
        if (ops.isEmpty())
            return "HISTORY: (brak operacji)";
        StringBuilder sb = new StringBuilder("HISTORY:\n");
        for (var op : ops) {
            sb.append(op.getTimestamp()).append(" | ").append(op.getOperation()).append("\n");
        }
        return sb.toString();
    }

    private String handleReport() {
        IHistoryService historyService = serviceFactory.getHistoryService();
        List<OperationHistoryModel> allOps = historyService.getHistoryByClientId(0);
        String path = "report.csv";
        ReportExportUtil.exportToCsv(allOps, path);
        return "REPORT OK: " + path;
    }

    private void logOperation(Integer clientId, String operation) {
        IHistoryService historyService = serviceFactory.getHistoryService();
        String user = loggedUsername != null ? loggedUsername : "unknown";
        String opWithUserAndTime = operation + " [user:" + user + "] [" + LocalDateTime.now() + "]";
        if (clientId != null) {
            historyService.addOperation(new OperationHistoryModel(0, clientId, opWithUserAndTime, LocalDateTime.now()));
        }
    }

    private void sendError(BufferedWriter writer, String msg) throws IOException {
        writer.write(msg + "\n");
        writer.flush();
    }

    private void sendOk(BufferedWriter writer, String msg) throws IOException {
        writer.write(msg + "\n");
        writer.flush();
    }
}