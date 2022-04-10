package com.codecool.gradebookapi.exception;

public class UsernameTakenException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Username %s already taken";

    public UsernameTakenException(String username) {
        super(String.format(ERROR_MESSAGE, username));
    }
}
