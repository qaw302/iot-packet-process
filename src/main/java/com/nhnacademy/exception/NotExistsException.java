package com.nhnacademy.exception;

public class NotExistsException extends RuntimeException {
    public NotExistsException(String message) {
        super(message);
    }

    public NotExistsException() {
        super();
    }
}
