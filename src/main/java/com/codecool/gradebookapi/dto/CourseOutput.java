package com.codecool.gradebookapi.dto;

import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@Relation(collectionRelation = "courses", itemRelation = "course")
public class CourseOutput {

    private Long id;

    @Schema(example = "Algebra")
    private String name;

    private SimpleData teacher;

    private List<SimpleData> students;

}
