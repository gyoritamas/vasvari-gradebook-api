package com.codecool.gradebookapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class GradebookInput {

    @NotNull(message = "Student ID cannot be empty")
    @Schema(example = "1")
    private Long studentId;

    @NotNull(message = "Course ID cannot be empty")
    @Schema(example = "1")
    private Long courseId;

    @NotNull(message = "Assignment ID cannot be empty")
    @Schema(example = "1")
    private Long assignmentId;

    @NotNull(message = "Grade field cannot be empty")
    @Min(value = 1, message = "Grade value must be between 1-5")
    @Max(value = 5, message = "Grade value must be between 1-5")
    @Schema(example = "4")
    private Integer grade;
}
