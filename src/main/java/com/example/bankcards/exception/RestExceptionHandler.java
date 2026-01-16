package com.example.bankcards.exception;

import com.example.bankcards.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

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
