package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.GradebookEntry;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;
import java.util.Optional;

public interface GradebookEntryRepository extends JpaRepositoryImplementation<GradebookEntry, Long>, JpaSpecificationExecutor<GradebookEntry> {
    List<GradebookEntry> findAll(Specification<GradebookEntry> specification);
}
