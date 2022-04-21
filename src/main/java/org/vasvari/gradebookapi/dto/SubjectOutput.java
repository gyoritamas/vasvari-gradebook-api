package org.vasvari.gradebookapi.dto;

import org.vasvari.gradebookapi.dto.simpleTypes.SimpleStudent;
import org.vasvari.gradebookapi.dto.simpleTypes.SimpleTeacher;
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
