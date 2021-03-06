package org.vasvari.gradebookapi.repository;

import org.vasvari.gradebookapi.model.Student;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;

public interface StudentRepository extends JpaRepositoryImplementation<Student, Long>, JpaSpecificationExecutor<Student> {
    List<Student> findAll(Specification<Student> specification);
}
