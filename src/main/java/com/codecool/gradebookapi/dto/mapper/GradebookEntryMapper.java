package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import com.codecool.gradebookapi.model.GradebookEntry;
import com.codecool.gradebookapi.repository.AssignmentRepository;
import com.codecool.gradebookapi.repository.CourseRepository;
import com.codecool.gradebookapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GradebookEntryMapper {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;

    public GradebookOutput map(GradebookEntry gradebookEntry) {
        return GradebookOutput.builder()
                .id(gradebookEntry.getId())
                .student(
                        new SimpleData(gradebookEntry.getStudent().getId(), gradebookEntry.getStudent().getName())
                )
                .course(
                        new SimpleData(gradebookEntry.getCourse().getId(), gradebookEntry.getCourse().getName())
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
                .course(
                        courseRepository.getById(gradebookInput.getCourseId())
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
