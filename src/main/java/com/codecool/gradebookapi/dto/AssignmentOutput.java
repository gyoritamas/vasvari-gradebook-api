package com.codecool.gradebookapi.dto;

import com.codecool.gradebookapi.dto.simpleTypes.SimpleData;
import com.codecool.gradebookapi.model.AssignmentType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@Relation(collectionRelation = "assignments", itemRelation = "assignment")
public class AssignmentOutput {

    private Long id;

    @Schema(example = "Homework 1")
    private String name;

    @Schema(example = "HOMEWORK")
    private AssignmentType type;

    @Schema(example = "Read Chapters 6 and 9.")
    private String description;

    @Schema(example = "2051-01-01")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate deadline;

    private SimpleData subject;

}
