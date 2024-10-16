package com.rio_rishabhNEU.UserApp.ExceptionHandlers;

public class EmailNotProvidedException extends RuntimeException {
    public EmailNotProvidedException(String message) {
        super(message);
    }
}
