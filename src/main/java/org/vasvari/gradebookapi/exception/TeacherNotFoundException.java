package org.vasvari.gradebookapi.exception;

public class TeacherNotFoundException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Teacher %d does not exist";

    public TeacherNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
