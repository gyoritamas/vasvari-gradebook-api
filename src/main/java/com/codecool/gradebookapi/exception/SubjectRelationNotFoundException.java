package com.codecool.gradebookapi.exception;

public class SubjectRelationNotFoundException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Student %d is not related to subject %d";

    public SubjectRelationNotFoundException(long studentId, long classId) {
        super(String.format(ERROR_MESSAGE, studentId, classId));
    }
}
