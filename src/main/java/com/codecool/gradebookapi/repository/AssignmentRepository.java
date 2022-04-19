package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.Assignment;
import com.codecool.gradebookapi.model.Subject;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;

public interface AssignmentRepository extends JpaRepositoryImplementation<Assignment, Long>, JpaSpecificationExecutor<Assignment> {
    List<Assignment> findAllBySubjectIn(List<Subject> subjects);
    List<Assignment> findAll(Specification<Assignment> specification);
}
