package com.example.bankcards.exception;

import com.example.bankcards.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        String message = (details == null || details.isBlank())
                ? "Validation failed"
                : "Validation failed: " + details;

        return ResponseEntity.status(400).body(
                ErrorResponse.of("VALIDATION_ERROR", message, request.getRequestURI())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.status(400).body(
                ErrorResponse.of("BAD_REQUEST", "Malformed JSON request", request.getRequestURI())
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String name = ex.getName();
        String value = (ex.getValue() == null) ? "null" : String.valueOf(ex.getValue());
        String message = "Invalid value for parameter '" + name + "': " + value;

        return ResponseEntity.status(400).body(
                ErrorResponse.of("BAD_REQUEST", message, request.getRequestURI())
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus()).body(
                ErrorResponse.of(ex.getError(), ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException ex, HttpServletRequest request) {
        return ResponseEntity.status(401).body(
                ErrorResponse.of("UNAUTHORIZED", "Unauthorized", request.getRequestURI())
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(403).body(
                ErrorResponse.of("FORBIDDEN", "Forbidden", request.getRequestURI())
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(409).body(
                ErrorResponse.of("CONFLICT", "Operation violates database constraints", request.getRequestURI())
        );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return ResponseEntity.status(409).body(
                ErrorResponse.of("CONFLICT", "Concurrent update detected. Please retry.", request.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error for path={}", request.getRequestURI(), ex);

        return ResponseEntity.status(500).body(
                ErrorResponse.of("INTERNAL_ERROR", "Unexpected server error", request.getRequestURI())
        );
    }
}
