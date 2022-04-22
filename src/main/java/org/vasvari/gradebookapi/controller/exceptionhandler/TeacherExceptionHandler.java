package org.vasvari.gradebookapi.controller.exceptionhandler;

import org.vasvari.gradebookapi.exception.TeacherNotFoundException;
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
public class TeacherExceptionHandler {
    @ExceptionHandler(TeacherNotFoundException.class)
    ResponseEntity<Problem> handleCannotFindTeacher(TeacherNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("/teachers/not-found"))
                .withTitle("Teacher not found")
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
