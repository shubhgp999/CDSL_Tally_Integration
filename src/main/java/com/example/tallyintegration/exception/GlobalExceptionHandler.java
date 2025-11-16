package com.example.tallyintegration.exception;

import com.example.tallyintegration.model.TallyResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<TallyResult> handleInvalid(InvalidRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(TallyResult.failure("Invalid request: " + ex.getMessage()));
    }

    @ExceptionHandler(TallyConnectionException.class)
    public ResponseEntity<TallyResult> handleTallyConn(TallyConnectionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(TallyResult.failure("Tally connection failed: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TallyResult> handleAll(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TallyResult.failure("Internal server error: " + ex.getMessage()));
    }
}
