package ftp.gusamyky.server.network;

import ftp.gusamyky.server.config.ServerConfig;
import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.handler.ClientHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerNetworkService {
    private final ServerConfig config;
    private final ServiceFactory serviceFactory;
    private ServerSocket serverSocket;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ServerNetworkService(ServerConfig config, ServiceFactory serviceFactory) {
        this.config = config;
        this.serviceFactory = serviceFactory;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(config.getHost(), config.getPort()));
            System.out.println("[Server] Listening on " + config.getHost() + ":" + config.getPort());
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(
                        "[Server] New client connected from IP: " + clientSocket.getInetAddress().getHostAddress());
                executor.submit(new ClientHandler(clientSocket, serviceFactory));
            }
        } catch (Exception e) {
            System.err.println("[Server] Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (Exception ignored) {
            }
            executor.shutdownNow();
        }
    }
}