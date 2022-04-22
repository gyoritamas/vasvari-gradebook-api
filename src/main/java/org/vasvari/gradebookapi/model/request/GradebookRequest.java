package org.vasvari.gradebookapi.model.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class GradebookRequest {
    private Long studentId;
    private Long subjectId;
    private Long assignmentId;
}
