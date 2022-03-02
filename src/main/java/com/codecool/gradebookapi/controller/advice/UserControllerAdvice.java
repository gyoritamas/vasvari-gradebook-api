package com.codecool.gradebookapi.controller.advice;

import com.codecool.gradebookapi.exception.UserNotFoundException;
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
public class UserControllerAdvice {
    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<Problem> handleCannotFindUser(UserNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("users/not-found"))
                .withTitle("User not found")
                .withStatus(Status.NOT_FOUND)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
