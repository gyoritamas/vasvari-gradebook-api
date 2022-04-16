package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.mapper.GradebookEntryMapper;
import com.codecool.gradebookapi.exception.DuplicateEntryException;
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

    public GradebookOutput save(GradebookInput gradebookInput) {
        if (isDuplicateEntry(gradebookInput)) throw new DuplicateEntryException(gradebookInput);
        GradebookEntry entryToSave = mapper.map(gradebookInput);
        GradebookEntry entrySaved = repository.save(entryToSave);

        return mapper.map(entrySaved);
    }

    public GradebookOutput update(Long id, GradebookInput gradebookInput) {
        GradebookEntry oldEntry = repository.getById(id);
        GradebookEntry update = mapper.map(gradebookInput);
        if (!areEntriesOnlyDifferInGrade(oldEntry, update) && isDuplicateEntry(gradebookInput)) {
            throw new DuplicateEntryException(gradebookInput);
        }

        update.setId(id);
        GradebookEntry entryUpdated = repository.save(update);

        return mapper.map(entryUpdated);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Determines if an entry is a duplicate entry. An entry is duplicate if another entry already exists with
     * the same subject, student and assignment
     *
     * @param entry the examined gradebook entry
     * @return true if the entry is duplicate entry, false otherwise
     */
    private boolean isDuplicateEntry(GradebookInput entry) {
        GradebookRequest request = GradebookRequest.builder()
                .subjectId(entry.getSubjectId())
                .studentId(entry.getStudentId())
                .assignmentId(entry.getAssignmentId())
                .build();
        return !findGradebookEntries(request).isEmpty();
    }

    /**
     * Compares two entries and determines if they only differ in grade value
     *
     * @param entry1 one of the examined gradebook entries
     * @param entry2 the other examined gradebook entry
     * @return true if the entries only differ in grade value, false otherwise
     */
    private boolean areEntriesOnlyDifferInGrade(GradebookEntry entry1, GradebookEntry entry2) {
        return entry1.getSubject().equals(entry2.getSubject())
                && entry1.getStudent().equals(entry2.getStudent())
                && entry1.getAssignment().equals(entry2.getAssignment());
    }
}
