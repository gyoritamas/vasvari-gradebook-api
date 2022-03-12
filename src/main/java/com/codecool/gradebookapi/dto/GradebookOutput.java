package com.codecool.gradebookapi.dto;

import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
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

    @Schema(example = "{\"id\":\"1\", \"name\":\"John Doe\"}")
    private SimpleData student;

    @Schema(example = "{\"id\":\"1\", \"name\":\"Algebra\"}")
    private SimpleData course;

    @Schema(example = "{\"id\":\"1\", \"name\":\"Pop Quiz\"}")
    private SimpleData assignment;

    private Integer grade;
}
