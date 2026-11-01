package com.logicore.Logicore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {
        // ErrorResponse object create kar rahe hain
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );

        // HTTP 404 ke saath structured JSON response return kar rahe hain
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> handleDuplicateResourceFound(
            DuplicateResourceException exception
    ) {
        // ErrorResponse object create kar rahe hain
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now()
        );

        // HTTP 404 ke saath structured JSON response return kar rahe hain
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<?> handleInvalidStatusTransition(
            InvalidStatusTransitionException exception
    ) {
        // ErrorResponse object create kar rahe hain
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        // HTTP 404 ke saath structured JSON response return kar rahe hain
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        // Validation ke saare field errors retrieve karta hai.
        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .get(0) // Filhaal first validation error retrieve kar rahe hain.
                .getDefaultMessage(); // DTO mein diya hua custom validation message leta hai.

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // Validation failure ke liye HTTP 400.
                .body(errorMessage); // User ko actual validation error batata hai.
    }
}
