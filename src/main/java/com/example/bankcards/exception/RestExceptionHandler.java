package com.example.bankcards.exception;

import com.example.bankcards.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String details = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        String message = (details == null || details.isBlank())
                ? "Validation failed"
                : "Validation failed: " + details;

        return ResponseEntity.status(400).body(
                ErrorResponse.of(
                        "VALIDATION_ERROR",
                        message,
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler({ BadCredentialsException.class, AuthenticationException.class })
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(401).body(
                ErrorResponse.of(
                        "UNAUTHORIZED",
                        "Unauthorized",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(403).body(
                ErrorResponse.of(
                        "FORBIDDEN",
                        "Forbidden",
                        request.getRequestURI()
                )
        );
    }
}
