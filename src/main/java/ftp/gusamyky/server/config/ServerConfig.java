package ftp.gusamyky.server.config;

public class ServerConfig {
    private int port;
    private String filesDir;
    private String host;

    public ServerConfig(int port, String filesDir, String host) {
        this.port = port;
        this.filesDir = filesDir;
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public String getFilesDir() {
        return filesDir;
    }

    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setFilesDir(String filesDir) {
        this.filesDir = filesDir;
    }

    public void setHost(String host) {
        this.host = host;
    }
}