package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.mapper.GradebookEntryMapper;
import com.codecool.gradebookapi.model.GradebookEntry;
import com.codecool.gradebookapi.repository.GradebookEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradebookService {

    @Autowired
    private GradebookEntryRepository repository;

    @Autowired
    private GradebookEntryMapper mapper;

    public List<GradebookOutput> findAll() {
        return mapper.mapAll(repository.findAll());
    }

    public Optional<GradebookOutput> findById(Long id) {
        return repository.findById(id).map(entry -> mapper.map(entry));
    }

    public List<GradebookOutput> findByStudentId(Long studentId) {
        List<GradebookEntry> entries = repository.findAllByStudent_Id(studentId);

        return mapper.mapAll(entries);
    }

    public List<GradebookOutput> findByClassId(Long classId) {
        List<GradebookEntry> entriesFound = repository.findAllByCourse_Id(classId);

        return mapper.mapAll(entriesFound);
    }

    public List<GradebookOutput> findByStudentIdAndCourseId(Long studentId, Long courseId) {
        List<GradebookEntry> entriesFound = repository.findAllByStudent_IdAndCourse_Id(studentId, courseId);

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
                .findByStudent_IdAndCourse_IdAndAssignment_Id(entry.getStudentId(), entry.getCourseId(), entry.getAssignmentId())
                .isPresent();
    }
}
