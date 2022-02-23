package com.codecool.gradebookapi.testmodel;

import lombok.*;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CourseOutput {

    private Long id;
    private String name;
    private Long teacherId;
    private List<String> students;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseOutput that = (CourseOutput) o;
        return Objects.equals(name, that.name)
                && Objects.equals(teacherId, that.teacherId)
                && Objects.equals(students, that.students);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, teacherId, students);
    }
}
