package com.codecool.gradebookapi.exception;

public class StudentInUseException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Student %d is used by a GradebookEntry";

    public StudentInUseException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
