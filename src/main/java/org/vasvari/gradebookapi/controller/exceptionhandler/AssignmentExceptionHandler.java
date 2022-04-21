package org.vasvari.gradebookapi.controller.exceptionhandler;

import org.vasvari.gradebookapi.exception.AssignmentInUseException;
import org.vasvari.gradebookapi.exception.AssignmentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.net.URI;

@ControllerAdvice
@Slf4j
public class AssignmentExceptionHandler {
    @ExceptionHandler(AssignmentNotFoundException.class)
    ResponseEntity<Problem> handleCannotFindAssignment(AssignmentNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("assignments/not-found"))
                .withTitle("Assignment not found")
                .withStatus(Status.NOT_FOUND)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(AssignmentInUseException.class)
    ResponseEntity<Problem> handleAssignmentInUse(AssignmentInUseException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("assignments/method-not-allowed"))
                .withTitle("Assignment in use")
                .withStatus(Status.METHOD_NOT_ALLOWED)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Problem problem = Problem.builder()
                .withType(URI.create("/assignments/json-parse-error"))
                .withTitle("JSON parse error")
                .withStatus(Status.BAD_REQUEST)
                .withDetail(e.getMessage())
                .build();

        log.warn(e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
