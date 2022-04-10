package com.codecool.gradebookapi.exception;

import com.codecool.gradebookapi.security.ApplicationUserRole;

public class DuplicateAccountException extends RuntimeException {
    public static final String ERROR_MESSAGE = "%s already has an account";

    public DuplicateAccountException(ApplicationUserRole role) {
        super(String.format(ERROR_MESSAGE, role.name()));
    }
}
