package ftp.gusamyky.server.common.exception;

public class DatabaseException extends ServerException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}