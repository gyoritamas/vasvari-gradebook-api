package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.model.GradebookEntry;
import com.codecool.gradebookapi.repository.AssignmentRepository;
import com.codecool.gradebookapi.repository.CourseRepository;
import com.codecool.gradebookapi.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GradebookEntryMapper {
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    public GradebookOutput map(GradebookEntry gradebookEntry) {
        return GradebookOutput.builder()
                .id(gradebookEntry.getId())
                .studentId(gradebookEntry.getStudent().getId())
                .classId(gradebookEntry.getCourse().getId())
                .assignmentId(gradebookEntry.getAssignment().getId())
                .grade(gradebookEntry.getGrade())
                .build();
    }

    public GradebookEntry map(GradebookInput gradebookInput) {
        return GradebookEntry.builder()
                .student(
                        studentRepository.getById(gradebookInput.getStudentId())
                )
                .course(
                        courseRepository.getById(gradebookInput.getClassId())
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
