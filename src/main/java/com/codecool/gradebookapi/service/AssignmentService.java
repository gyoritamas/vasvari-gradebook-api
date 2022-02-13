package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import com.codecool.gradebookapi.dto.mapper.AssignmentMapper;
import com.codecool.gradebookapi.model.Assignment;
import com.codecool.gradebookapi.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository repository;

    @Autowired
    private AssignmentMapper mapper;

    public List<AssignmentOutput> findAll() {
        return mapper.mapAll(repository.findAll());
    }

    public AssignmentOutput save(AssignmentInput assignmentInput) {
        Assignment assignmentToSave = mapper.map(assignmentInput);
        assignmentToSave.setCreatedAt(Instant.now());
        Assignment assignmentSaved = repository.save(assignmentToSave);

        return mapper.map(assignmentSaved);
    }

    public AssignmentOutput update(Long id, AssignmentInput assignmentInput) {
        Assignment assignmentToUpdate = repository.getById(id);
        Assignment update = mapper.map(assignmentInput);
        update.setId(id);
        update.setCreatedAt(assignmentToUpdate.getCreatedAt());
        Assignment assignmentUpdated = repository.save(update);

        return mapper.map(assignmentUpdated);
    }

    public Optional<AssignmentOutput> findById(Long id) {
        return repository.findById(id).map(assignment -> mapper.map(assignment));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

}
