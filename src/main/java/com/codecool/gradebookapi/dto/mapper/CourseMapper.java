package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import com.codecool.gradebookapi.model.Course;
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
                                .map(student -> new SimpleData(student.getId(), student.getName()))
                                .collect(Collectors.toList())
                )
                .build();

        if (course.getTeacher() != null) {
            courseOutput.setTeacher(new SimpleData(course.getTeacher().getId(), course.getTeacher().getName()));
        }

        return courseOutput;
    }

    public List<CourseOutput> mapAll(List<Course> courses) {
        return courses.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
