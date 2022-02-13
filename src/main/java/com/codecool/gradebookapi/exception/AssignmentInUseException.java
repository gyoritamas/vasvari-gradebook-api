package com.codecool.gradebookapi.exception;

public class AssignmentInUseException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Assignment %d is used by a GradebookEntry";

    public AssignmentInUseException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
