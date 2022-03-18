package com.codecool.gradebookapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class SubjectInput {

    @NotBlank(message = "Name field cannot be empty")
    @Schema(example = "Algebra")
    private String name;

    @NotNull(message = "Teacher ID cannot be empty")
    @Schema(example = "1")
    private Long teacherId;
}
