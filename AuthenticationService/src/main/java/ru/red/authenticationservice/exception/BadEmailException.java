package ru.red.authenticationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad Email")
public class BadEmailException extends RuntimeException {
    public BadEmailException() {
    }

    public BadEmailException(String message) {
        super(message);
    }

    public BadEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadEmailException(Throwable cause) {
        super(cause);
    }
}
