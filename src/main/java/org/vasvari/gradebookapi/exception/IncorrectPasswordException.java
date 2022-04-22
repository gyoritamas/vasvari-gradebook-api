package org.vasvari.gradebookapi.exception;

public class IncorrectPasswordException extends RuntimeException{
    public static final String ERROR_MESSAGE = "Password is incorrect";

    public IncorrectPasswordException() {
        super(ERROR_MESSAGE);
    }
}
