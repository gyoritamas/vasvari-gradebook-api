package com.codecool.gradebookapi.exception;

public class UserNotFoundException extends RuntimeException{
    public static final String ERROR_MESSAGE = "User %d does not exist";

    public UserNotFoundException(Long id){
        super(String.format(ERROR_MESSAGE, id));
    }
}
