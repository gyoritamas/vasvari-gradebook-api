package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.model.Course;
import com.codecool.gradebookapi.model.Student;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseMapper {

    public CourseOutput map(Course course) {
        CourseOutput courseOutput = CourseOutput.builder()
                .id(course.getId())
                .name(course.getName())
                .students(
                        course.getStudents().stream()
                                .map(Student::getName)
                                .collect(Collectors.toList())
                )
                .build();

        if (course.getTeacher() != null) {
            courseOutput.setTeacherId(course.getTeacher().getId());
        }

        return courseOutput;
    }

    public List<CourseOutput> mapAll(List<Course> courses) {
        return courses.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
