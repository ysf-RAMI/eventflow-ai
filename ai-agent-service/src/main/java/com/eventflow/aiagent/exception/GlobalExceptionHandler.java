package com.eventflow.aiagent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AgentUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleUnavailable(AgentUnavailableException ex) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(
                status, "The assistant is unavailable right now, try again later"));
    }
}
