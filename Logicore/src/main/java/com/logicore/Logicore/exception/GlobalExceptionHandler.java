
package com.logicore.Logicore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
// Ye class application ke REST controllers ke exceptions centrally handle karegi.
public class GlobalExceptionHandler {

    // Shipment database mein nahi milta toh HTTP 404 return hoga.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {

        ErrorResponse errorResponse = new ErrorResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    // Duplicate tracking number milne par HTTP 409 return hoga.
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceFound(
            DuplicateResourceException exception
    ) {

        ErrorResponse errorResponse = new ErrorResponse(
                exception.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    // Invalid shipment status transition hone par HTTP 400 return hoga.
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
            InvalidStatusTransitionException exception
    ) {

        ErrorResponse errorResponse = new ErrorResponse(
                exception.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    // DTO validation fail hone par ye handler execute hoga.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {

        // Saare field validation errors ko Map mein convert kar rahe hain.
        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()

                // Field ka naam Map ki key aur error message value banega.
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),

                        // Agar same field ke multiple errors aaye,
                        // toh pehla error message retain karenge.
                        (existingMessage, newMessage) -> existingMessage
                ));

        // Validation errors ke saath structured response create kar rahe hain.
        ErrorResponse errorResponse = new ErrorResponse(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                errors
        );

        // HTTP 400 ke saath complete error response return kar rahe hain.
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
    // Invalid Excel data ya invalid input ke liye 400 response
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.badRequest().body(error);
    }
}
