package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.Assignment;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface AssignmentRepository extends JpaRepositoryImplementation<Assignment, Long> {
}
