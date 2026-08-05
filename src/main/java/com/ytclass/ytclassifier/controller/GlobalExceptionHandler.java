package com.ytclass.ytclassifier.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        e.printStackTrace(); 
        return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage() != null ? e.getMessage() : "Unknown error",
                "cause", e.getCause() != null ? e.getCause().getMessage() : "none"
        ));
    }
}
