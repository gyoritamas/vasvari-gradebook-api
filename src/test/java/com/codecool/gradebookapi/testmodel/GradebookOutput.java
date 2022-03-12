package com.codecool.gradebookapi.testmodel;

import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import lombok.*;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GradebookOutput {

    private Long id;
    private SimpleData student;
    private SimpleData course;
    private SimpleData assignment;
    private Integer grade;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradebookOutput that = (GradebookOutput) o;
        return Objects.equals(student, that.student) &&
                Objects.equals(course, that.course) &&
                Objects.equals(assignment, that.assignment) &&
                Objects.equals(grade, that.grade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(student, course, assignment, grade);
    }
}
