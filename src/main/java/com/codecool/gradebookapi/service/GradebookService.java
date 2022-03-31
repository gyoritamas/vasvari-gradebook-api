package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.mapper.GradebookEntryMapper;
import com.codecool.gradebookapi.model.GradebookEntry;
import com.codecool.gradebookapi.model.request.GradebookRequest;
import com.codecool.gradebookapi.model.specification.GradebookEntrySpecification;
import com.codecool.gradebookapi.repository.GradebookEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GradebookService {

    private final GradebookEntryRepository repository;
    private final GradebookEntryMapper mapper;
    private final GradebookEntrySpecification specification;

    public List<GradebookOutput> findAll() {
        return mapper.mapAll(repository.findAll());
    }

    public List<GradebookOutput> findGradebookEntries(GradebookRequest request) {
        List<GradebookEntry> entries = repository.findAll(specification.getGradebookEntries(request));

        return mapper.mapAll(entries);
    }

    public Optional<GradebookOutput> findById(Long id) {
        return repository.findById(id).map(mapper::map);
    }

    public List<GradebookOutput> findByStudentId(Long studentId) {
        List<GradebookEntry> entries = repository.findAllByStudent_Id(studentId);

        return mapper.mapAll(entries);
    }

    public List<GradebookOutput> findBySubjectId(Long subjectId) {
        List<GradebookEntry> entriesFound = repository.findAllBySubject_Id(subjectId);

        return mapper.mapAll(entriesFound);
    }

    public List<GradebookOutput> findByStudentIdAndSubjectId(Long studentId, Long subjectId) {
        List<GradebookEntry> entriesFound = repository.findAllByStudent_IdAndSubject_Id(studentId, subjectId);

        return mapper.mapAll(entriesFound);
    }

    public List<GradebookOutput> findByAssignmentId(Long assignmentId) {
        List<GradebookEntry> entries = repository.findAllByAssignment_Id(assignmentId);

        return mapper.mapAll(entries);
    }

    public GradebookOutput save(GradebookInput gradebookInput) {
        GradebookEntry entryToSave = mapper.map(gradebookInput);
        GradebookEntry entrySaved = repository.save(entryToSave);

        return mapper.map(entrySaved);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public boolean isDuplicateEntry(GradebookInput entry) {
        return repository
                .findByStudent_IdAndSubject_IdAndAssignment_Id(entry.getStudentId(), entry.getSubjectId(), entry.getAssignmentId())
                .isPresent();
    }
}
