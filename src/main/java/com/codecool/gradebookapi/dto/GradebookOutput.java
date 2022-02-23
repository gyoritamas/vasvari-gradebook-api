package com.codecool.gradebookapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Relation(collectionRelation = "entries", itemRelation = "entry")
@EqualsAndHashCode
public class GradebookOutput {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "1")
    private Long studentId;

    @Schema(example = "1")
    private Long courseId;

    @Schema(example = "1")
    private Long assignmentId;

    @Schema(example = "4")
    private Integer grade;
}
