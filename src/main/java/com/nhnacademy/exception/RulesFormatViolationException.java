package com.nhnacademy.exception;

public class RulesFormatViolationException extends RuntimeException {
    public RulesFormatViolationException(String message) {
        super(message);
    }

    public RulesFormatViolationException() {
        super();
    }

}