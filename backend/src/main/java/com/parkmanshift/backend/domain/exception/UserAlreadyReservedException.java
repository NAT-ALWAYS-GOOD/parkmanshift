package com.parkmanshift.backend.domain.exception;

public class UserAlreadyReservedException extends RuntimeException {
    public UserAlreadyReservedException(String message) {
        super(message);
    }
}
