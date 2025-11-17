package com.imp.shared.exception;

/**
 * Exception thrown during message classification
 */
public class ClassificationException extends RuntimeException {

    public ClassificationException(String message) {
        super(message);
    }

    public ClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
