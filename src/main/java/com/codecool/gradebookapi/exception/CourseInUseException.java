package com.codecool.gradebookapi.exception;

public class CourseInUseException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Class %d is used by a GradebookEntry";

    public CourseInUseException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
