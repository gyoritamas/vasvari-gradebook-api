package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.model.Teacher;
import com.codecool.gradebookapi.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    private final TeacherRepository repository;

    public TeacherService(TeacherRepository repository) {
        this.repository = repository;
    }

    public List<Teacher> findAll() {
        return repository.findAll();
    }

    public Teacher save(Teacher teacher) {
        return repository.save(teacher);
    }

    public Optional<Teacher> findById(Long id) {
        return repository.findById(id);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
