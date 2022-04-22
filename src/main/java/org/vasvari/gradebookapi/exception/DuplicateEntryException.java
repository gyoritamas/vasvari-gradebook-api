package org.vasvari.gradebookapi.exception;

import org.vasvari.gradebookapi.dto.GradebookInput;

public class DuplicateEntryException extends RuntimeException {
    public static final String ERROR_MESSAGE =
            "Entry with student %d, subject %d and assignment %d already exists";

    public DuplicateEntryException(GradebookInput entry) {
        super(String.format(ERROR_MESSAGE, entry.getStudentId(), entry.getSubjectId(), entry.getAssignmentId()));
    }
}
