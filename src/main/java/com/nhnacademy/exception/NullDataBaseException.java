package com.nhnacademy.exception;

public class NullDataBaseException extends RuntimeException {

    public NullDataBaseException(String message) {
        super(message);
    }

    public NullDataBaseException() {
        super();
    }

}
