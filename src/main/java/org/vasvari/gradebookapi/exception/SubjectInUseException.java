package org.vasvari.gradebookapi.exception;

public class SubjectInUseException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Subject %d is used by a GradebookEntry";

    public SubjectInUseException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
