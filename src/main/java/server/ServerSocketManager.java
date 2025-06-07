package server;

import common.db.ClientDAO;
import common.db.DatabaseManager;
import common.model.Client;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import common.db.OperationHistoryDAO;
import common.model.OperationHistory;
import java.time.LocalDateTime;

public class ServerSocketManager {
    // --- Konfiguracja i zależności ---
    private final int port;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final ClientDAO clientDAO;
    private final OperationHistoryDAO historyDAO;
    private static final String FILES_DIR = "server_files";

    // --- Inicjalizacja ---
    public ServerSocketManager(int port) {
        this.port = port;
        this.clientDAO = new ClientDAO(DatabaseManager.getInstance().getConnection());
        this.historyDAO = new OperationHistoryDAO(DatabaseManager.getInstance().getConnection());
    }

    // --- Start serwera ---
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("[Server] Listening on port " + port);
        while (running) {
            Socket clientSocket = serverSocket.accept();
            String clientIp = clientSocket.getInetAddress().getHostAddress();
            System.out.println("[Server] New client connected from IP: " + clientIp);
            executor.submit(new ClientHandler(clientSocket, clientDAO, historyDAO, FILES_DIR));
        }
    }

    // --- Zatrzymanie serwera ---
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        executor.shutdownNow();
    }

    // =====================
    // Obsługa klienta
    // =====================
    private void handleClient(Socket clientSocket) {
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
                processClientCommand(line.trim(), in, out, reader, writer, clientIp);
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

    // =====================
    // Obsługa komend klienta
    // =====================
    private void processClientCommand(String input, InputStream in, OutputStream out, BufferedReader reader,
            BufferedWriter writer, String clientIp) throws IOException {
        String[] parts = input.split(" ", 2);
        String cmd = parts[0].toUpperCase();
        String args = parts.length > 1 ? parts[1] : "";
        switch (cmd) {
            case "UPLOAD":
                writer.write("READY\n");
                writer.flush();
                receiveFile(args, in, reader, writer, clientIp);
                break;
            case "DOWNLOAD":
                sendFile(args, out, writer, clientIp);
                break;
            default:
                String response = handleCommand(input);
                writer.write(response + "\n");
                writer.flush();
        }
    }

    // =====================
    // Transfer plików
    // =====================
    /**
     * Odbiór pliku od klienta (UPLOAD)
     */
    private void receiveFile(String filename, InputStream in, BufferedReader reader, BufferedWriter writer,
            String clientIp) throws IOException {
        if (filename.isEmpty()) {
            sendError(writer, "UPLOAD ERROR: No filename given");
            logOperation(0, "UPLOAD_FAIL:NoFilename");
            System.out.println("[UPLOAD][FAIL] No filename given from IP: " + clientIp);
            return;
        }
        Files.createDirectories(Paths.get(FILES_DIR));
        Path filePath = Paths.get(FILES_DIR, filename);
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

    /**
     * Wysyłka pliku do klienta (DOWNLOAD)
     */
    private void sendFile(String filename, OutputStream out, BufferedWriter writer, String clientIp)
            throws IOException {
        if (filename.isEmpty()) {
            sendError(writer, "DOWNLOAD ERROR: No filename given");
            logOperation(0, "DOWNLOAD_FAIL:NoFilename");
            System.out.println("[DOWNLOAD][FAIL] No filename given to IP: " + clientIp);
            return;
        }
        Path filePath = Paths.get(FILES_DIR, filename);
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

    // =====================
    // Komendy tekstowe (proste)
    // =====================
    private String handleCommand(String input) {
        String[] parts = input.split(" ", 2);
        String cmd = parts[0].toUpperCase();
        String args = parts.length > 1 ? parts[1] : "";
        switch (cmd) {
            case "ECHO":
                return "ECHO: " + args;
            case "LOGIN":
                return handleLogin(args);
            case "REGISTER":
                return handleRegister(args);
            case "LIST":
                return handleList();
            case "UPLOAD":
                return handleUpload(args);
            case "DOWNLOAD":
                return handleDownload(args);
            case "HISTORY":
                return handleHistory(args);
            case "REPORT":
                return handleReport();
            default:
                return "Unknown command: " + cmd;
        }
    }

    // =====================
    // Komendy: rejestracja, logowanie, lista, historia, raport
    // =====================
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
            Files.createDirectories(Paths.get(FILES_DIR));
            File dir = new File(FILES_DIR);
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

    private String handleUpload(String args) {
        if (args.isEmpty())
            return "UPLOAD ERROR: Usage UPLOAD <filename>";
        // Tu będzie logika odbioru pliku
        return "UPLOAD OK (mock) for " + args;
    }

    private String handleDownload(String args) {
        if (args.isEmpty())
            return "DOWNLOAD ERROR: Usage DOWNLOAD <filename>";
        // Tu będzie logika wysyłki pliku
        return "DOWNLOAD OK (mock) for " + args;
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

    // =====================
    // Pomocnicze: logowanie, odpowiedzi
    // =====================
    private void logOperation(int clientId, String operation) {
        try {
            historyDAO.addOperation(new OperationHistory(0, clientId, operation, LocalDateTime.now()));
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