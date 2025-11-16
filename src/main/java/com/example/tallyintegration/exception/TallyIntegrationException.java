package com.example.tallyintegration.exception;

public class TallyIntegrationException extends RuntimeException {
    public TallyIntegrationException(String message) {
        super(message);
    }

    public TallyIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
