package com.codecool.gradebookapi.controller.exceptionhandler;

import com.codecool.gradebookapi.exception.SubjectInUseException;
import com.codecool.gradebookapi.exception.SubjectNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.net.URI;

@ControllerAdvice
@Slf4j
public class SubjectExceptionHandler {
    @ExceptionHandler(SubjectNotFoundException.class)
    ResponseEntity<Problem> handleCannotFindSubject(SubjectNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("subjects/not-found"))
                .withTitle("Subject not found")
                .withStatus(Status.NOT_FOUND)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(SubjectInUseException.class)
    ResponseEntity<Problem> handleSubjectInUse(SubjectInUseException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("subjects/method-not-allowed"))
                .withTitle("Subject in use")
                .withStatus(Status.METHOD_NOT_ALLOWED)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

}
