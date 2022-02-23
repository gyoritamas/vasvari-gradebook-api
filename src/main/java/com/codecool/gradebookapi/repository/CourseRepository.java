package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.Course;
import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.model.Teacher;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepositoryImplementation<Course, Long> {
    List<Course> findCoursesByStudentsContaining(Student student);

    Optional<Course> findCoursesByStudentsContainingAndId(Student student, long classId);

    List<Course> findCoursesByTeacherContaining(Teacher teacher);
}
