package ftp.gusamyky.server.config;

public class DatabaseConfig {
    private String url;
    private String user;
    private String password;
    private String filesDirectory;
    private String urlNoDb;

    public DatabaseConfig(String url, String user, String password, String filesDirectory, String urlNoDb) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.filesDirectory = filesDirectory;
        this.urlNoDb = urlNoDb;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getFilesDirectory() {
        return filesDirectory;
    }

    public String getUrlNoDb() {
        return urlNoDb;
    }

    public void setFilesDirectory(String filesDirectory) {
        this.filesDirectory = filesDirectory;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrlNoDb(String urlNoDb) {
        this.urlNoDb = urlNoDb;
    }
}