package com.jakubkras.project.exception;

public class EmptyValueException extends RuntimeException {
    public EmptyValueException(String message) {
        super(message);
    }
}
