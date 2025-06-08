package ftp.gusamyky.server.config;

public class ServerConfig {
    private int port;
    private String filesDir;

    public ServerConfig(int port, String filesDir) {
        this.port = port;
        this.filesDir = filesDir;
    }

    public int getPort() {
        return port;
    }

    public String getFilesDir() {
        return filesDir;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setFilesDir(String filesDir) {
        this.filesDir = filesDir;
    }
}