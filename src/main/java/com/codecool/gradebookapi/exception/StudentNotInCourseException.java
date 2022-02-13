package com.codecool.gradebookapi.exception;

public class StudentNotInCourseException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Student %d is not enrolled in class %d";

    public StudentNotInCourseException(long studentId, long classId) {
        super(String.format(ERROR_MESSAGE, studentId, classId));
    }
}
