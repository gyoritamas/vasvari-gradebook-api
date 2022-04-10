package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;

public interface StudentRepository extends JpaRepositoryImplementation<Student, Long>, JpaSpecificationExecutor<Student> {
    Page<Student> findAll(Specification<Student> specification, Pageable pageable);

    List<Student> findAll(Specification<Student> specification);
}
