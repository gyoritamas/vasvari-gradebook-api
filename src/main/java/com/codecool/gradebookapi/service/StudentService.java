package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.mapper.StudentMapper;
import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private StudentMapper mapper;

    public List<StudentDto> findAll() {
        return mapper.mapAll(repository.findAll());
    }

    public StudentDto save(StudentDto studentDto) {
        Student studentToSave = mapper.map(studentDto);
        Student saved = repository.save(studentToSave);

        return mapper.map(saved);
    }

    public Optional<StudentDto> findById(Long id) {
        return repository.findById(id).map(student -> mapper.map(student));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

}
