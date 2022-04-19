package com.codecool.gradebookapi.controller.exceptionhandler;

import com.codecool.gradebookapi.exception.StudentInUseException;
import com.codecool.gradebookapi.exception.StudentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.net.URI;

@ControllerAdvice
@Slf4j
public class StudentExceptionHandler {
    @ExceptionHandler(StudentNotFoundException.class)
    ResponseEntity<Problem> handleCannotFindStudent(StudentNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("students/not-found"))
                .withTitle("Student not found")
                .withStatus(Status.NOT_FOUND)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(StudentInUseException.class)
    ResponseEntity<Problem> handleStudentInUse(StudentInUseException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("students/method-not-allowed"))
                .withTitle("Student in use")
                .withStatus(Status.METHOD_NOT_ALLOWED)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Problem> handleInvalidMethodArgument(MethodArgumentNotValidException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("gradebook/invalid-argument"))
                .withTitle("Invalid argument")
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
