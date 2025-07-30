package com.weyland.starter.synthetichumancorestarter.exception;

import com.weyland.starter.synthetichumancorestarter.command.CommandValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommandValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(CommandValidationException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Command validation error");
        error.put("details", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleQueueOverflow(IllegalStateException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Queue overflow");
        error.put("details", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Internal error");
        error.put("details", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}