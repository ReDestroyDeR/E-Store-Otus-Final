package ru.red.authenticationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class BadPasswordException extends RuntimeException {
    public BadPasswordException() {
    }

    public BadPasswordException(String message) {
        super(message);
    }

    public BadPasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadPasswordException(Throwable cause) {
        super(cause);
    }
}
