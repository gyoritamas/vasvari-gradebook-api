package com.codecool.gradebookapi.testmodel;

import com.codecool.gradebookapi.model.AssignmentType;
import com.codecool.gradebookapi.util.ZonedDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.springframework.hateoas.Link;

import java.time.ZonedDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AssignmentOutput {

    private Long id;

    private String name;

    private AssignmentType type;

    private String description;

    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime createdAt;

    private Link[] links;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentOutput that = (AssignmentOutput) o;
        return Objects.equals(name, that.name) && type == that.type && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, description);
    }
}
