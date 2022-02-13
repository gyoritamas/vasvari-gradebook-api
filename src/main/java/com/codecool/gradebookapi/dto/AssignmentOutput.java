package com.codecool.gradebookapi.dto;

import com.codecool.gradebookapi.model.AssignmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Relation(collectionRelation = "assignments", itemRelation = "assignment")
public class AssignmentOutput {

    private Long id;

    @Schema(example = "Homework 1")
    private String name;

    @Schema(example = "HOMEWORK")
    private AssignmentType type;

    @Schema(example = "Read Chapters 6 and 9.")
    private String description;

    private ZonedDateTime createdAt;

}
