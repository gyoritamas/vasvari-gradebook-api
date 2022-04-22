package org.vasvari.gradebookapi.exception;

public class SubjectNotFoundException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Subject %d does not exist";

    public SubjectNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
