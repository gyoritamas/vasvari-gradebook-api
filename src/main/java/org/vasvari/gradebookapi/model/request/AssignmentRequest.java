package org.vasvari.gradebookapi.model.request;

import org.vasvari.gradebookapi.model.AssignmentType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AssignmentRequest {
    private String title;
    private AssignmentType type;
    private Long subjectId;
}
