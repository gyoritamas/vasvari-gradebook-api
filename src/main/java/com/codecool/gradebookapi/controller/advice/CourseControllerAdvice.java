package com.codecool.gradebookapi.controller.advice;

import com.codecool.gradebookapi.exception.CourseInUseException;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
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
public class CourseControllerAdvice {
    @ExceptionHandler(CourseNotFoundException.class)
    ResponseEntity<Problem> handleCannotFindClass(CourseNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("classes/not-found"))
                .withTitle("Class not found")
                .withStatus(Status.NOT_FOUND)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(CourseInUseException.class)
    ResponseEntity<Problem> handleAssignmentInUse(CourseInUseException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("classes/method-not-allowed"))
                .withTitle("Class in use")
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
