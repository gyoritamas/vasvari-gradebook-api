package org.vasvari.gradebookapi.exception;

public class StudentUserNotFoundException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Student %d is not related to any user";

    public StudentUserNotFoundException(long studentId) {
        super(String.format(ERROR_MESSAGE, studentId));
    }
}
