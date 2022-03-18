package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.GradebookEntry;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;
import java.util.Optional;

public interface GradebookEntryRepository extends JpaRepositoryImplementation<GradebookEntry, Long> {
    List<GradebookEntry> findAllByStudent_Id(long id);

    List<GradebookEntry> findAllBySubject_Id(long id);

    List<GradebookEntry> findAllByAssignment_Id(long id);

    List<GradebookEntry> findAllByStudent_IdAndSubject_Id(long studentId, long subjectId);

    Optional<GradebookEntry> findByStudent_IdAndSubject_IdAndAssignment_Id(long studentId, long subjectId, long assignmentId);
}
