package ftp.gusamyky.server.network;

import ftp.gusamyky.server.config.ServerConfig;
import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.handler.ClientHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;



public class ServerNetworkService {
    private static final Logger LOGGER = Logger.getLogger(ServerNetworkService.class.getName());
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

    private final ServerConfig config;
    private final ServiceFactory serviceFactory;
    private ServerSocket serverSocket;
    private final ExecutorService executor;
    private volatile boolean running = false;

    public ServerNetworkService(ServerConfig config, ServiceFactory serviceFactory) {
        this.config = config;
        this.serviceFactory = serviceFactory;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setName("ClientHandler-" + t.getId());
            return t;
        });
    }



    public void start() {
        if (running) {
            LOGGER.warning("Server is already running");
            return;
        }

        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(config.getHost(), config.getPort()));
            running = true;
            LOGGER.info("Server listening on " + config.getHost() + ":" + config.getPort());

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    LOGGER.info("New client connected from IP: " + clientAddress);
                    try {
                        executor.submit(new ClientHandler(clientSocket, serviceFactory));
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error creating client handler for IP: " + clientAddress, e);
                        clientSocket.close();
                    }
                } catch (Exception e) {
                    if (running) {
                        LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Server error", e);
            throw new ServerException("Failed to start server", e);
        } finally {
            cleanup();
        }
    }

    public void stop() {
        if (!running) {
            LOGGER.warning("Server is not running");
            return;
        }

        LOGGER.info("Stopping server...");
        running = false;

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing server socket", e);
            }
        }

        // Shutdown executor service
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                LOGGER.warning("Forcing executor shutdown after timeout");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.warning("Executor shutdown interrupted");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        LOGGER.info("Server stopped");
    }

    private void cleanup() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing server socket", e);
            }
        }
        executor.shutdownNow();
    }

    public static class ServerException extends RuntimeException {
        public ServerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}