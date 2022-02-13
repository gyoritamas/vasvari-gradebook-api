package com.codecool.gradebookapi.exception;

public class StudentNotFoundException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Student %d does not exist";

    public StudentNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
