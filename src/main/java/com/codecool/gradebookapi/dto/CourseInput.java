package com.codecool.gradebookapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CourseInput {

    @NotBlank(message = "Course field cannot be empty")
    @Schema(example = "Algebra")
    private String course;

}
