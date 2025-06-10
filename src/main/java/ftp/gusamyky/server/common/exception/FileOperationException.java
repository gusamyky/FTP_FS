package ftp.gusamyky.server.common.exception;

public class FileOperationException extends ServerException {
    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}