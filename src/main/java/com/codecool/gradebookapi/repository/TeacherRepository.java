package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.Teacher;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface TeacherRepository extends JpaRepositoryImplementation<Teacher, Long> {
}
