package com.codecool.gradebookapi.testmodel;

import lombok.*;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ClassOutput {

    private Long id;
    private String course;
    private List<String> students;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassOutput that = (ClassOutput) o;
        return Objects.equals(course, that.course) && Objects.equals(students, that.students);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, students);
    }
}
