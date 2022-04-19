package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.model.Subject;
import com.codecool.gradebookapi.model.Teacher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepositoryImplementation<Subject, Long>, JpaSpecificationExecutor<Subject> {
    List<Subject> findSubjectsByStudentsContaining(Student student);
    Optional<Subject> findSubjectsByStudentsContainingAndId(Student student, long subjectId);
    List<Subject> findSubjectsByTeacher(Teacher teacher);
    List<Subject> findAll(Specification<Subject> specification);
}
