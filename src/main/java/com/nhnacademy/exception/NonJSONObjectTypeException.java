package com.nhnacademy.exception;

public class NonJSONObjectTypeException extends RuntimeException {
    public NonJSONObjectTypeException(String message) {
        super(message);
    }

    public NonJSONObjectTypeException() {
        super();
    }
}
