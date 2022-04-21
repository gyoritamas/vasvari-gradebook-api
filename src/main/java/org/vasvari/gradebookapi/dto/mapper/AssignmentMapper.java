package org.vasvari.gradebookapi.dto.mapper;

import org.vasvari.gradebookapi.dto.AssignmentInput;
import org.vasvari.gradebookapi.dto.AssignmentOutput;
import org.vasvari.gradebookapi.dto.simpleTypes.SimpleData;
import org.vasvari.gradebookapi.model.Assignment;
import org.vasvari.gradebookapi.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AssignmentMapper {
    private final SubjectRepository subjectRepository;

    public Assignment map(AssignmentInput input) {
        return Assignment.builder()
                .name(input.getName())
                .type(input.getType())
                .description(input.getDescription())
                .deadline(input.getDeadline())
                .subject(
                        subjectRepository.getById(input.getSubjectId())
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
                .subject(
                        new SimpleData(assignment.getSubject().getId(), assignment.getSubject().getName())
                )
                .build();
    }

    public List<AssignmentOutput> mapAll(List<Assignment> assignments) {
        return assignments.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
