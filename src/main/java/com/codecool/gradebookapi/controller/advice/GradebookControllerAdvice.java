package com.codecool.gradebookapi.controller.advice;

import com.codecool.gradebookapi.exception.DuplicateEntryException;
import com.codecool.gradebookapi.exception.GradebookEntryNotFoundException;
import com.codecool.gradebookapi.exception.SubjectRelationNotFoundException;
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
public class GradebookControllerAdvice {
    @ExceptionHandler(GradebookEntryNotFoundException.class)
    ResponseEntity<Problem> handleEntryNotFound(GradebookEntryNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("gradebook/not-found"))
                .withTitle("Gradebook entry not found")
                .withStatus(Status.NOT_FOUND)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(DuplicateEntryException.class)
    ResponseEntity<Problem> handleDuplicateEntry(DuplicateEntryException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("gradebook/conflict"))
                .withTitle("Duplicate entry")
                .withStatus(Status.CONFLICT)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(SubjectRelationNotFoundException.class)
    ResponseEntity<Problem> handleUnrelatedStudentAndSubject(SubjectRelationNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("gradebook/bad-request"))
                .withTitle("Student has no relation to the subject")
                .withStatus(Status.BAD_REQUEST)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

}
