package ftp.gusamyky.server.handler;

import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.handler.command.Command;
import ftp.gusamyky.server.handler.command.CommandFactory;
import ftp.gusamyky.server.handler.command.LoginStateUpdater;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Handler for client connections.
 * This class is responsible for handling client connections and executing
 * commands.
 */
public class ClientHandler implements Runnable, LoginStateUpdater {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private static final int SOCKET_TIMEOUT = 300000; // 5 minutes
    private static final int SOCKET_BUFFER_SIZE = 65536; // 64KB
    private final Socket clientSocket;
    private final CommandFactory commandFactory;
    private boolean loggedIn;
    private String loggedUsername;
    private Integer loggedClientId;

    /**
     * Creates a new client handler instance.
     *
     * @param clientSocket   The client socket to handle
     * @param serviceFactory The service factory to use
     * @throws IOException If an I/O error occurs
     */
    public ClientHandler(Socket clientSocket, ServiceFactory serviceFactory) throws IOException {
        this.clientSocket = clientSocket;
        this.loggedIn = false;
        this.loggedUsername = null;
        this.loggedClientId = null;

        // Configure socket
        clientSocket.setSoTimeout(SOCKET_TIMEOUT);
        clientSocket.setSendBufferSize(SOCKET_BUFFER_SIZE);
        clientSocket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
        clientSocket.setKeepAlive(true);
        clientSocket.setTcpNoDelay(true);

        this.commandFactory = new CommandFactory(
                serviceFactory,
                new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),
                clientSocket.getInetAddress().getHostAddress(),
                loggedIn,
                loggedUsername,
                loggedClientId,
                this,
                clientSocket.getInputStream(),
                clientSocket.getOutputStream());
        LOGGER.info(String.format("Created client handler for %s", clientSocket.getRemoteSocketAddress()));
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
            sendWelcomeMessage(writer);
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    handleCommand(line, writer);
                } catch (IOException e) {
                    if (e instanceof SocketException) {
                        LOGGER.info(String.format("Client %s disconnected during command execution",
                                clientSocket.getRemoteSocketAddress()));
                        break;
                    }
                    LOGGER.log(Level.SEVERE, String.format("Error handling command from client %s",
                            clientSocket.getRemoteSocketAddress()), e);
                    try {
                        writer.write("ERROR: Internal server error\n");
                        writer.flush();
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Error sending error message to client", ex);
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.info(String.format("Client %s disconnected", clientSocket.getRemoteSocketAddress()));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format("Error handling client %s", clientSocket.getRemoteSocketAddress()),
                    e);
        } finally {
            try {
                clientSocket.close();
                LOGGER.info(String.format("Client %s disconnected", clientSocket.getRemoteSocketAddress()));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error closing client socket", e);
            }
        }
    }

    private void sendWelcomeMessage(BufferedWriter writer) throws IOException {
        LOGGER.info(String.format("Sending welcome message to client %s", clientSocket.getRemoteSocketAddress()));
        writer.write("Welcome to FTP Server\n");
        writer.write("Available commands: LOGIN, REGISTER, LOGOUT, UPLOAD, DOWNLOAD, LIST, HISTORY, REPORT, ECHO\n");
        writer.write("END\n");
        writer.flush();
    }

    private void handleCommand(String line, BufferedWriter writer) throws IOException {
        LOGGER.info(String.format("Received command from client %s: %s", clientSocket.getRemoteSocketAddress(), line));
        if (line == null || line.trim().isEmpty()) {
            return;
        }
        String[] parts = line.split(" ", 2);
        String commandName = parts[0].toUpperCase();
        String args = parts.length > 1 ? parts[1] : "";

        try {
            Command command = commandFactory.createCommand(commandName, args);
            command.execute();
        } catch (IOException e) {
            if (e instanceof SocketException) {
                LOGGER.info(String.format("Client %s disconnected during command execution",
                        clientSocket.getRemoteSocketAddress()));
                throw e;
            }
            LOGGER.log(Level.SEVERE, String.format("Error executing command %s", commandName), e);
            writer.write("ERROR: Internal server error\n");
            writer.flush();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error executing command %s", commandName), e);
            writer.write("ERROR: Internal server error\n");
            writer.flush();
        }
    }

    @Override
    public void update(boolean loggedIn, String username, Integer clientId) {
        LOGGER.info(String.format("Updating login state for client %s - Logged in: %b, Username: %s, Client ID: %d",
                clientSocket.getRemoteSocketAddress(), loggedIn, username, clientId));
        this.loggedIn = loggedIn;
        this.loggedUsername = username;
        this.loggedClientId = clientId;
        commandFactory.updateLoginState(loggedIn, username, clientId);
    }
}