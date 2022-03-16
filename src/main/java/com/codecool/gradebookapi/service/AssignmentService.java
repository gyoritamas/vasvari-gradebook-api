package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import com.codecool.gradebookapi.dto.mapper.AssignmentMapper;
import com.codecool.gradebookapi.model.Assignment;
import com.codecool.gradebookapi.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository repository;
    private final AssignmentMapper mapper;

    public List<AssignmentOutput> findAll() {
        return mapper.mapAll(repository.findAll());
    }

    public AssignmentOutput save(AssignmentInput assignmentInput) {
        Assignment assignment = mapper.map(assignmentInput);
        Assignment assignmentSaved = repository.save(assignment);

        return mapper.map(assignmentSaved);
    }

    public AssignmentOutput update(Long id, AssignmentInput assignmentInput) {
        Assignment update = mapper.map(assignmentInput);
        update.setId(id);
        Assignment assignmentUpdated = repository.save(update);

        return mapper.map(assignmentUpdated);
    }

    public Optional<AssignmentOutput> findById(Long id) {
        return repository.findById(id).map(mapper::map);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

}
