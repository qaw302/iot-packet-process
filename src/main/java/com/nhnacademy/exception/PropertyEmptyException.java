package com.nhnacademy.exception;

public class PropertyEmptyException extends RuntimeException {
    public PropertyEmptyException(String message) {
        super(message);
    }

    public PropertyEmptyException() {
        super();
    }
}
