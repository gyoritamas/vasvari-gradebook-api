package org.vasvari.gradebookapi.dto.mapper;

import org.vasvari.gradebookapi.dto.SubjectOutput;
import org.vasvari.gradebookapi.dto.simpleTypes.SimpleStudent;
import org.vasvari.gradebookapi.dto.simpleTypes.SimpleTeacher;
import org.vasvari.gradebookapi.model.Subject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubjectMapper {

    public SubjectOutput map(Subject subject) {
        SubjectOutput subjectOutput = SubjectOutput.builder()
                .id(subject.getId())
                .name(subject.getName())
                .students(
                        subject.getStudents().stream()
                                .map(student -> SimpleStudent.builder()
                                        .id(student.getId())
                                        .firstname(student.getFirstname())
                                        .lastname(student.getLastname())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();

        if (subject.getTeacher() != null) {
            subjectOutput.setTeacher(
                    SimpleTeacher.builder()
                            .id(subject.getTeacher().getId())
                            .firstname(subject.getTeacher().getFirstname())
                            .lastname(subject.getTeacher().getLastname())
                            .build()
            );
        }

        return subjectOutput;
    }

    public List<SubjectOutput> mapAll(List<Subject> subjects) {
        return subjects.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
