package com.codecool.gradebookapi.testmodel;

import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
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
    private SimpleData teacher;
    private List<SimpleData> students;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseOutput that = (CourseOutput) o;
        return Objects.equals(name, that.name)
                && Objects.equals(teacher, that.teacher)
                && Objects.equals(students, that.students);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, teacher, students);
    }

}
