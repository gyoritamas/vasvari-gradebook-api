package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.Student;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface StudentRepository extends JpaRepositoryImplementation<Student, Long> {
}
