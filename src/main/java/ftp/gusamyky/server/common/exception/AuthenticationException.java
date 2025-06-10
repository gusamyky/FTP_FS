package ftp.gusamyky.server.common.exception;

public class AuthenticationException extends ServerException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}