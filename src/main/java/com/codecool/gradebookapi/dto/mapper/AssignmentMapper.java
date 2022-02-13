package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import com.codecool.gradebookapi.model.Assignment;
import com.codecool.gradebookapi.model.AssignmentType;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssignmentMapper {
    public Assignment map(AssignmentInput input) {
        return Assignment.builder()
                .name(input.getName())
                .type(AssignmentType.valueOf(input.getType()))
                .description(input.getDescription())
                .build();
    }

    public AssignmentOutput map(Assignment assignment) {
        return AssignmentOutput.builder()
                .id(assignment.getId())
                .name(assignment.getName())
                .type(assignment.getType())
                .description(assignment.getDescription())
                .createdAt(
                        assignment.getCreatedAt().atZone(ZoneId.systemDefault())
                )
                .build();
    }

    public List<AssignmentOutput> mapAll(List<Assignment> assignments) {
        return assignments.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
