package ru.red.productservice.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Not enough in stock")
public class NotEnoughInStockException extends RuntimeException {
    public NotEnoughInStockException() {
    }

    public NotEnoughInStockException(String message) {
        super(message);
    }

    public NotEnoughInStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughInStockException(Throwable cause) {
        super(cause);
    }
}
