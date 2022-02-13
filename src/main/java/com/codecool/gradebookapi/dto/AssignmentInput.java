package com.codecool.gradebookapi.dto;

import com.codecool.gradebookapi.model.AssignmentType;
import com.codecool.gradebookapi.validation.ValueOfEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class AssignmentInput {

    @NotBlank(message = "Name field cannot be empty")
    @Schema(example = "Homework 1")
    private String name;

    @ValueOfEnum(enumClass = AssignmentType.class,
            message = "Type must be any of TEST, HOMEWORK, PROJECT, QUIZ")
    @Schema(example = "HOMEWORK")
    private String type;

    @Schema(example = "Read Chapters 6 and 9.")
    private String description;

}
