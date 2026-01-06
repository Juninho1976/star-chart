package com.example.starchart.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    var msg = ex.getBindingResult().getAllErrors().stream()
        .findFirst()
        .map(e -> e.getDefaultMessage())
        .orElse("Validation error");

    return ResponseEntity.badRequest().body(new ErrorResponse(
        "validation_error", msg, req.getRequestURI(), Instant.now()
    ));
  }

  // ✅ 401s
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest req) {
    return ResponseEntity.status(401).body(new ErrorResponse(
        "unauthorized", "Not authenticated", req.getRequestURI(), Instant.now()
    ));
  }

  // ✅ 403s
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
    return ResponseEntity.status(403).body(new ErrorResponse(
        "forbidden", "Not allowed", req.getRequestURI(), Instant.now()
    ));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest().body(new ErrorResponse(
        "bad_request", ex.getMessage(), req.getRequestURI(), Instant.now()
    ));
  }

  // keep last
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
    return ResponseEntity.internalServerError().body(new ErrorResponse(
        "internal_error", "Unexpected error", req.getRequestURI(), Instant.now()
    ));
  }
}