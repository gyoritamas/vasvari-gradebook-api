package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.GradebookEntry;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;
import java.util.Optional;

public interface GradebookEntryRepository extends JpaRepositoryImplementation<GradebookEntry, Long> {
    List<GradebookEntry> findAllByStudent_Id(long id);

    List<GradebookEntry> findAllByCourse_Id(long id);

    List<GradebookEntry> findAllByAssignment_Id(long id);

    Optional<GradebookEntry> findByStudent_IdAndCourse_IdAndAssignment_Id(long studentId, long courseId, long assignmentId);
}
