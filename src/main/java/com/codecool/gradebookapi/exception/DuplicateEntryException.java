package com.codecool.gradebookapi.exception;

import com.codecool.gradebookapi.dto.GradebookInput;

public class DuplicateEntryException extends RuntimeException {
    public static final String ERROR_MESSAGE =
            "Entry with student %d, class %d and assignment %d already exists";

    public DuplicateEntryException(GradebookInput entry) {
        super(String.format(ERROR_MESSAGE, entry.getStudentId(), entry.getCourseId(), entry.getAssignmentId()));
    }
}
