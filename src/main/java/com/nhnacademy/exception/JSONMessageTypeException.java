package com.nhnacademy.exception;

public class JSONMessageTypeException extends RuntimeException {
    public JSONMessageTypeException(String message) {
        super(message);
    }

    public JSONMessageTypeException() {
        super();
    }
}
