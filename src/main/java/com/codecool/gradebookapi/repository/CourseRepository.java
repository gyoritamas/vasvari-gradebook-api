package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.Course;
import com.codecool.gradebookapi.model.Student;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepositoryImplementation<Course, Long> {
    List<Course> findClassByStudentsContaining(Student student);

    Optional<Course> findClassByStudentsContainingAndId(Student student, long classId);
}
