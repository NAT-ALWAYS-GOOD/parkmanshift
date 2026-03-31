package com.parkmanshift.backend.domain.exception;

public class SpotNotAvailableException extends RuntimeException {
    public SpotNotAvailableException(String message) {
        super(message);
    }
}
