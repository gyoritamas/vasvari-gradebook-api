package com.codecool.gradebookapi.model.request;

import com.codecool.gradebookapi.model.AssignmentType;
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
