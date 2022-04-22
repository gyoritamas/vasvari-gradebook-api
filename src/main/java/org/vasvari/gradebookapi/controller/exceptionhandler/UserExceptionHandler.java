package org.vasvari.gradebookapi.controller.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.vasvari.gradebookapi.exception.*;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.net.URI;

@ControllerAdvice
@Slf4j
public class UserExceptionHandler {
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

    @ExceptionHandler(StudentUserNotFoundException.class)
    ResponseEntity<Problem> handleStudentHasNoRelationToUser(StudentUserNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("users/not-found"))
                .withTitle("Student has no relation to any user")
                .withStatus(Status.NOT_FOUND)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(TeacherUserNotFoundException.class)
    ResponseEntity<Problem> handleTeacherHasNoRelationToUser(TeacherUserNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("users/not-found"))
                .withTitle("Teacher has no relation to any user")
                .withStatus(Status.NOT_FOUND)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    ResponseEntity<Problem> handleIncorrectPassword(IncorrectPasswordException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("users/incorrect-password"))
                .withTitle("Given password is incorrect")
                .withStatus(Status.BAD_REQUEST)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(UsernameTakenException.class)
    ResponseEntity<Problem> handleUsernameAlreadyTaken(UsernameTakenException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("users/username-taken"))
                .withTitle("Username taken")
                .withStatus(Status.BAD_REQUEST)
                .withDetail(ex.getMessage())
                .build();

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(DuplicateAccountException.class)
    ResponseEntity<Problem> handleDuplicateAccount(DuplicateAccountException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("users/duplicate-account"))
                .withTitle("Already has an account")
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
