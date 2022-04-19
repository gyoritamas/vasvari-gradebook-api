package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.Teacher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;

public interface TeacherRepository extends JpaRepositoryImplementation<Teacher, Long>, JpaSpecificationExecutor<Teacher> {
    List<Teacher> findAll(Specification<Teacher> specification);
}
