package org.vasvari.gradebookapi.exception;

public class AssignmentNotFoundException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Assignment %d does not exist";

    public AssignmentNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
