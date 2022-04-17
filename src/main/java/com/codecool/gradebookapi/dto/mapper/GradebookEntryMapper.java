package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.simpleTypes.SimpleData;
import com.codecool.gradebookapi.dto.simpleTypes.SimpleStudent;
import com.codecool.gradebookapi.model.GradebookEntry;
import com.codecool.gradebookapi.repository.AssignmentRepository;
import com.codecool.gradebookapi.repository.SubjectRepository;
import com.codecool.gradebookapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GradebookEntryMapper {
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final AssignmentRepository assignmentRepository;

    public GradebookOutput map(GradebookEntry gradebookEntry) {
        return GradebookOutput.builder()
                .id(gradebookEntry.getId())
                .student(
                        SimpleStudent.builder()
                                .id(gradebookEntry.getStudent().getId())
                                .firstname(gradebookEntry.getStudent().getFirstname())
                                .lastname(gradebookEntry.getStudent().getLastname())
                                .build()
                )
                .subject(
                        new SimpleData(gradebookEntry.getSubject().getId(), gradebookEntry.getSubject().getName())
                )
                .assignment(
                        new SimpleData(gradebookEntry.getAssignment().getId(), gradebookEntry.getAssignment().getName())
                )
                .grade(gradebookEntry.getGrade())
                .build();
    }

    public GradebookEntry map(GradebookInput gradebookInput) {
        return GradebookEntry.builder()
                .student(
                        studentRepository.getById(gradebookInput.getStudentId())
                )
                .subject(
                        subjectRepository.getById(gradebookInput.getSubjectId())
                )
                .assignment(
                        assignmentRepository.getById(gradebookInput.getAssignmentId())
                )
                .grade(gradebookInput.getGrade())
                .build();
    }

    public List<GradebookOutput> mapAll(List<GradebookEntry> entries) {
        return entries.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
