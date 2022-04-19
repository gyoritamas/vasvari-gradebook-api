package com.codecool.gradebookapi.dto;

import com.codecool.gradebookapi.dto.simpleTypes.SimpleData;
import com.codecool.gradebookapi.dto.simpleTypes.SimpleStudent;
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

    private SimpleStudent student;

    private SimpleData subject;

    private SimpleData assignment;

    private Integer grade;
}
