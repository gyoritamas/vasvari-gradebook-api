package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.model.Course;
import com.codecool.gradebookapi.model.Student;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseMapper {

    public CourseOutput map(Course clazz) {
        return CourseOutput.builder()
                .id(clazz.getId())
                .course(clazz.getName())
                .students(
                        clazz.getStudents().stream()
                                .map(Student::getName)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public List<CourseOutput> mapAll(List<Course> courses) {
        return courses.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
