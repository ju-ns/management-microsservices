package com.ms.user.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandardError> handleUserNotFound(UserNotFoundException g){
        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "User Not Found",
                g.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<StandardError> handleConflict(ConflictException ex, HttpServletRequest request){
        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

}
