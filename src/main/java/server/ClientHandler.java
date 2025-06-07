package server;

import common.db.ClientDAO;
import common.db.OperationHistoryDAO;
import common.model.Client;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.io.File;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ClientDAO clientDAO;
    private final OperationHistoryDAO historyDAO;
    private final String filesDir;
    private boolean loggedIn = false;
    private String loggedUsername = null;

    public ClientHandler(Socket clientSocket, ClientDAO clientDAO, OperationHistoryDAO historyDAO, String filesDir) {
        this.clientSocket = clientSocket;
        this.clientDAO = clientDAO;
        this.historyDAO = historyDAO;
        this.filesDir = filesDir;
    }

    @Override
    public void run() {
        String clientIp = clientSocket.getInetAddress().getHostAddress();
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream()) {
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
                            writer.write("ERROR: Not logged in\n");
                            writer.flush();
                            break;
                        }
                        writer.write("READY\n");
                        writer.flush();
                        receiveFile(args, in, reader, writer, clientIp);
                        break;
                    case "DOWNLOAD":
                        if (!loggedIn) {
                            writer.write("ERROR: Not logged in\n");
                            writer.flush();
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
                        }
                        break;
                    }
                    case "REGISTER":
                        writer.write(handleRegister(args) + "\n");
                        writer.flush();
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
            System.out.println("[Server] Client disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    // --- Transfer plików ---
    private void receiveFile(String filename, InputStream in, BufferedReader reader, BufferedWriter writer,
            String clientIp) throws IOException {
        if (filename.isEmpty()) {
            sendError(writer, "UPLOAD ERROR: No filename given");
            logOperation(0, "UPLOAD_FAIL:NoFilename");
            System.out.println("[UPLOAD][FAIL] No filename given from IP: " + clientIp);
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
            logOperation(0, "UPLOAD_FAIL:InvalidFileSize");
            System.out.println("[UPLOAD][FAIL] Invalid file size for '" + filename + "' from IP: " + clientIp);
            return;
        }
        System.out.println(
                "[UPLOAD][START] File: '" + filename + "', Size: " + fileSize + " bytes, From IP: " + clientIp);
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
        logOperation(0, "UPLOAD_OK: " + filename);
        System.out
                .println("[UPLOAD][END] File: '" + filename + "', Size: " + fileSize + " bytes, From IP: " + clientIp);
    }

    private void sendFile(String filename, OutputStream out, BufferedWriter writer, String clientIp)
            throws IOException {
        if (filename.isEmpty()) {
            sendError(writer, "DOWNLOAD ERROR: No filename given");
            logOperation(0, "DOWNLOAD_FAIL:NoFilename");
            System.out.println("[DOWNLOAD][FAIL] No filename given to IP: " + clientIp);
            return;
        }
        Path filePath = Paths.get(filesDir, filename);
        if (!Files.exists(filePath)) {
            sendError(writer, "DOWNLOAD ERROR: File not found");
            logOperation(0, "DOWNLOAD_FAIL:FileNotFound");
            System.out.println("[DOWNLOAD][FAIL] File not found: '" + filename + "' for IP: " + clientIp);
            return;
        }
        long fileSize = Files.size(filePath);
        writer.write(fileSize + "\n");
        writer.flush();
        System.out.println(
                "[DOWNLOAD][START] File: '" + filename + "', Size: " + fileSize + " bytes, To IP: " + clientIp);
        try (InputStream fileIn = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
        logOperation(0, "DOWNLOAD_OK: " + filename);
        System.out
                .println("[DOWNLOAD][END] File: '" + filename + "', Size: " + fileSize + " bytes, To IP: " + clientIp);
    }

    // --- Komendy tekstowe ---
    private String handleRegister(String args) {
        String[] tokens = args.split(" ");
        if (tokens.length != 2) {
            return "REGISTER ERROR: Usage REGISTER <username> <password>";
        }
        String username = tokens[0];
        String password = tokens[1];
        try {
            if (clientDAO.getClientByUsername(username) != null) {
                logOperation(0, "REGISTER_FAIL:UsernameExists");
                return "REGISTER ERROR: Username already exists";
            }
            clientDAO.addClient(new Client(0, username, password));
            Client client = clientDAO.getClientByUsername(username);
            logOperation(client != null ? client.getId() : 0, "REGISTER_OK");
            return "REGISTER OK";
        } catch (SQLException e) {
            logOperation(0, "REGISTER_ERROR: " + e.getMessage());
            return "REGISTER ERROR: " + e.getMessage();
        }
    }

    private String handleLogin(String args) {
        String[] tokens = args.split(" ");
        if (tokens.length != 2) {
            return "LOGIN ERROR: Usage LOGIN <username> <password>";
        }
        String username = tokens[0];
        String password = tokens[1];
        try {
            Client client = clientDAO.getClientByUsername(username);
            if (client == null) {
                logOperation(0, "LOGIN_FAIL:UserNotFound");
                return "LOGIN ERROR: User not found";
            }
            if (!clientDAO.checkPassword(username, password)) {
                logOperation(client.getId(), "LOGIN_FAIL:InvalidPassword");
                return "LOGIN ERROR: Invalid password";
            }
            logOperation(client.getId(), "LOGIN_OK");
            return "LOGIN OK";
        } catch (SQLException e) {
            logOperation(0, "LOGIN_ERROR: " + e.getMessage());
            return "LOGIN ERROR: " + e.getMessage();
        }
    }

    private String handleList() {
        try {
            Files.createDirectories(Paths.get(filesDir));
            File dir = new File(filesDir);
            File[] files = dir.listFiles();
            int clientId = 0;
            logOperation(clientId, "LIST");
            if (files == null || files.length == 0) {
                return "FILES: (brak plików)";
            }
            StringBuilder sb = new StringBuilder("FILES:");
            for (File f : files) {
                if (f.isFile()) {
                    sb.append(" ").append(f.getName());
                }
            }
            return sb.toString();
        } catch (Exception e) {
            logOperation(0, "LIST_ERROR: " + e.getMessage());
            return "LIST ERROR: " + e.getMessage();
        }
    }

    private String handleHistory(String args) {
        String username = args.trim();
        if (username.isEmpty())
            return "HISTORY ERROR: No username given";
        try {
            Client client = clientDAO.getClientByUsername(username);
            if (client == null)
                return "HISTORY ERROR: User not found";
            var ops = historyDAO.getOperationsByClientId(client.getId());
            if (ops.isEmpty())
                return "HISTORY: (brak operacji)";
            StringBuilder sb = new StringBuilder("HISTORY:\n");
            for (var op : ops) {
                sb.append(op.getTimestamp()).append(" | ")
                        .append(op.getOperation()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "HISTORY ERROR: " + e.getMessage();
        }
    }

    private String handleReport() {
        try {
            var allOps = historyDAO.getAllOperations();
            StringBuilder sb = new StringBuilder();
            sb.append("RAPORT OPERACJI\n====================\n");
            for (var op : allOps) {
                sb.append(op.getTimestamp()).append(" | clientId=")
                        .append(op.getClientId()).append(" | ")
                        .append(op.getOperation()).append("\n");
            }
            String path = "report.txt";
            try (java.io.FileWriter fw = new java.io.FileWriter(path)) {
                fw.write(sb.toString());
            }
            return "REPORT OK: " + path;
        } catch (Exception e) {
            return "REPORT ERROR: " + e.getMessage();
        }
    }

    // --- Pomocnicze ---
    private void logOperation(int clientId, String operation) {
        String user = loggedUsername != null ? loggedUsername : "unknown";
        String timestamp = java.time.LocalDateTime.now().toString();
        String opWithUserAndTime = operation + " [user:" + user + "] [" + timestamp + "]";
        try {
            historyDAO.addOperation(
                    new common.model.OperationHistory(0, clientId, opWithUserAndTime, java.time.LocalDateTime.now()));
        } catch (Exception ignored) {
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