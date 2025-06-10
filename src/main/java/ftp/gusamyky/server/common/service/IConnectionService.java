package ftp.gusamyky.server.common.service;

import java.io.IOException;
import java.net.Socket;

/**
 * Service for handling client connections and command processing.
 * This service is responsible for managing client connections and executing
 * commands.
 */
public interface IConnectionService {
    /**
     * Handles a client connection.
     *
     * @param clientSocket The client socket to handle
     * @throws IOException If an I/O error occurs
     */
    void handleConnection(Socket clientSocket) throws IOException;

    /**
     * Sends a welcome message to the client.
     *
     * @param clientSocket The client socket to send the message to
     * @throws IOException If an I/O error occurs
     */
    void sendWelcomeMessage(Socket clientSocket) throws IOException;

    /**
     * Processes a command from the client.
     *
     * @param commandLine  The command line to process
     * @param clientSocket The client socket
     * @throws IOException If an I/O error occurs
     */
    void processCommand(String commandLine, Socket clientSocket) throws IOException;
}