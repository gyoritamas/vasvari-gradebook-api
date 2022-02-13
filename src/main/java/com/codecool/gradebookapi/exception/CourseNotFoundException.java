package com.codecool.gradebookapi.exception;

public class CourseNotFoundException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Class %d does not exist";

    public CourseNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
