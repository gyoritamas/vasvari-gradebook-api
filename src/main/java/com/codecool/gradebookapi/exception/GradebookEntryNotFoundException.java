package com.codecool.gradebookapi.exception;

public class GradebookEntryNotFoundException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Could not find entry %d";

    public GradebookEntryNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
