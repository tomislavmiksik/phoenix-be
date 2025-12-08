package dev.tomislavmiksik.phoenixbe.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RestException> handleBadCredentials(BadCredentialsException ex) {
        RestException error = new RestException(
            "401 UNAUTHORIZED",
            "Invalid credentials"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<RestException> handleRuntimeException(RuntimeException ex) {
        RestException error = new RestException(
            "400 BAD_REQUEST",
            ex.getMessage() != null ? ex.getMessage() : "An error occurred"
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestException> handleGenericException(Exception ex) {
        RestException error = new RestException(
            "500 INTERNAL_SERVER_ERROR",
            "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
