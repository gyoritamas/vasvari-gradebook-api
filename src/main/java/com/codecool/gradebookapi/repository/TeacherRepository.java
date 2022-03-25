package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;

public interface TeacherRepository extends JpaRepositoryImplementation<Teacher, Long>, JpaSpecificationExecutor<Teacher> {
    Page<Teacher> findAll(Specification<Teacher> specification, Pageable pageable);

    List<Teacher> findAll(Specification<Teacher> specification);
}
