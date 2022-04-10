package com.codecool.gradebookapi.dto;

import com.codecool.gradebookapi.dto.dataTypes.SimpleStudent;
import com.codecool.gradebookapi.dto.dataTypes.SimpleTeacher;
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
@Relation(collectionRelation = "subjects", itemRelation = "subject")
public class SubjectOutput {

    private Long id;

    @Schema(example = "Algebra")
    private String name;

    private SimpleTeacher teacher;

    private List<SimpleStudent> students;

}
