package org.vasvari.gradebookapi.repository;

import org.vasvari.gradebookapi.model.GradebookEntry;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;

public interface GradebookEntryRepository extends JpaRepositoryImplementation<GradebookEntry, Long>, JpaSpecificationExecutor<GradebookEntry> {
    List<GradebookEntry> findAll(Specification<GradebookEntry> specification);
}
