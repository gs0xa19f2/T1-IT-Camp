package com.weyland.starter.synthetichumancorestarter.command;

public class CommandValidationException extends RuntimeException {
    public CommandValidationException(String message) {
        super(message);
    }
}