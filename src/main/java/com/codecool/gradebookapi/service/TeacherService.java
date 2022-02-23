package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.mapper.TeacherMapper;
import com.codecool.gradebookapi.model.Teacher;
import com.codecool.gradebookapi.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    private final TeacherRepository repository;
    private final TeacherMapper mapper;

    public TeacherService(TeacherRepository repository, TeacherMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<TeacherDto> findAll() {
        return mapper.mapAll(repository.findAll());
    }

    public TeacherDto save(TeacherDto teacherDto) {
        Teacher teacherToSave = mapper.map(teacherDto);
        Teacher saved = repository.save(teacherToSave);

        return mapper.map(saved);
    }

    public Optional<TeacherDto> findById(Long id) {
        return repository.findById(id).map(teacher -> mapper.map(teacher));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
