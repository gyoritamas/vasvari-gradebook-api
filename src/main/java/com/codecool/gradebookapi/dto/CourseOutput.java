package com.codecool.gradebookapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Relation(collectionRelation = "courses", itemRelation = "course")
@EqualsAndHashCode
public class CourseOutput {

    private Long id;

    @Schema(example = "Algebra")
    private String name;

    @Schema(example = "1")
    private Long teacherId;

    @Schema(example = "[\"John Doe\", \"Jane Doe\"]")
    private List<String> students;

}
