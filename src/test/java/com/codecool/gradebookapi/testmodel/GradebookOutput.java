package com.codecool.gradebookapi.testmodel;

import lombok.*;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GradebookOutput {

    private Long id;
    private Long studentId;
    private Long classId;
    private Long assignmentId;
    private Integer grade;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradebookOutput that = (GradebookOutput) o;
        return Objects.equals(studentId, that.studentId)
                && Objects.equals(classId, that.classId)
                && Objects.equals(assignmentId, that.assignmentId)
                && Objects.equals(grade, that.grade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, classId, assignmentId, grade);
    }

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        GradebookOutput that = (GradebookOutput) o;
//        return Objects.equals(student, that.student)
//                && Objects.equals(clazz, that.clazz)
//                && Objects.equals(assignment, that.assignment)
//                && Objects.equals(grade, that.grade);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(student, clazz, assignment, grade);
//    }
}
