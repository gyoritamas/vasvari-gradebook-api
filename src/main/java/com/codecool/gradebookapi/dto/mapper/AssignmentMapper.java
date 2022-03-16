package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import com.codecool.gradebookapi.model.Assignment;
import com.codecool.gradebookapi.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AssignmentMapper {
    private final TeacherRepository teacherRepository;

    public Assignment map(AssignmentInput input) {
        return Assignment.builder()
                .name(input.getName())
                .type(input.getType())
                .description(input.getDescription())
                .deadline(input.getDeadline())
                .createdBy(
                        teacherRepository.getById(input.getTeacherId())
                )
                .build();
    }

    public AssignmentOutput map(Assignment assignment) {
        return AssignmentOutput.builder()
                .id(assignment.getId())
                .name(assignment.getName())
                .type(assignment.getType())
                .description(assignment.getDescription())
                .deadline(assignment.getDeadline())
                .createdBy(
                        new SimpleData(assignment.getCreatedBy().getId(), assignment.getCreatedBy().getName())
                )
                .build();
    }

    public List<AssignmentOutput> mapAll(List<Assignment> assignments) {
        return assignments.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
