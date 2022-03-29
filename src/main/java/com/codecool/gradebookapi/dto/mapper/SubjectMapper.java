package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.SubjectOutput;
import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import com.codecool.gradebookapi.dto.dataTypes.SimpleTeacher;
import com.codecool.gradebookapi.model.Subject;
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
                                .map(student -> new SimpleData(student.getId(), student.getName()))
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
