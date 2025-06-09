package ftp.gusamyky.server;

import ftp.gusamyky.server.config.ConfigLoader;
import ftp.gusamyky.server.config.ServerConfig;
import ftp.gusamyky.server.config.DatabaseConfig;
import ftp.gusamyky.server.service.ServiceFactory;
import ftp.gusamyky.server.util.DatabaseInitializer;
import ftp.gusamyky.server.network.ServerNetworkService;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import java.net.Socket;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import ftp.gusamyky.server.handler.ClientHandler;

public class ServerMain {
    private static ServerNetworkService networkService;

    public static void main(String[] args) throws Exception {
        // SSL/TLS: wymagany keystore (patrz poniżej)
        String keystore = System.getenv().getOrDefault("SSL_KEYSTORE", "keystore.jks");
        String keystorePass = System.getenv().getOrDefault("SSL_KEYSTORE_PASS", "changeit");
        System.setProperty("javax.net.ssl.keyStore", keystore);
        System.setProperty("javax.net.ssl.keyStorePassword", keystorePass);

        System.out.println("[" + LocalDateTime.now() + "] Server starting...");
        try {
            // Load config and initialize services
            ServerConfig serverConfig = ConfigLoader.loadServerConfig();
            DatabaseConfig dbConfig = ConfigLoader.loadDatabaseConfig();
            DatabaseInitializer.initialize(dbConfig);
            ServiceFactory serviceFactory = new ServiceFactory(dbConfig, serverConfig);
            networkService = new ServerNetworkService(serverConfig, serviceFactory);

            int port = serverConfig.getPort();
            AtomicInteger activeConnections = new AtomicInteger(0);
            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            try (SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port)) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        serverSocket.close();
                        System.out.println("[Server] Zamknięto serwer (SIGTERM/SIGINT)");
                    } catch (Exception e) {
                        System.err.println("[Server] Błąd przy zamykaniu serwera: " + e.getMessage());
                    }
                }));
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    if (activeConnections.get() >= 50) {

                        try (BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(clientSocket.getOutputStream()))) {
                            writer.write("ERROR: Server full. Try again later.\n");
                            writer.flush();
                        }
                        clientSocket.close();
                        continue;
                    }
                    activeConnections.incrementAndGet();
                    new Thread(() -> {
                        try {
                            new ClientHandler(clientSocket, serviceFactory).run();
                        } finally {
                            activeConnections.decrementAndGet();
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}