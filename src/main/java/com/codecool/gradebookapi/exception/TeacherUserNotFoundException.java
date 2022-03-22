package com.codecool.gradebookapi.exception;

public class TeacherUserNotFoundException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Teacher %d is not related to any user";

    public TeacherUserNotFoundException(long teacherId) {
        super(String.format(ERROR_MESSAGE, teacherId));
    }
}
