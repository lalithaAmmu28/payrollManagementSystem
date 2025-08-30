package com.pms.backend.exception;

public class ConstraintViolationException extends RuntimeException {
    
    public ConstraintViolationException(String message) {
        super(message);
    }
    
    public ConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
