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
@EqualsAndHashCode
@Relation(collectionRelation = "entries", itemRelation = "entry")
public class GradebookOutput {

    @Schema(example = "1")
    private Long id;

    private SimpleData student;

    private SimpleData course;

    private SimpleData assignment;

    private Integer grade;
}
