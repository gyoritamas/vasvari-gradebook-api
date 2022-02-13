package com.codecool.gradebookapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Relation(collectionRelation = "classes", itemRelation = "class")
@EqualsAndHashCode
public class CourseOutput {

    private Long id;

    @Schema(example = "Algebra")
    private String course;

    @Schema(example = "[\"John Doe\", \"Jane Doe\"]")
    private List<String> students;

}
