package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.mapper.TeacherMapper;
import com.codecool.gradebookapi.model.Teacher;
import com.codecool.gradebookapi.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;

    public List<TeacherDto> findAll() {
        return teacherMapper.mapAll(teacherRepository.findAll());
    }

    public TeacherDto save(TeacherDto teacherDto) {
        Teacher teacherToSave = teacherMapper.map(teacherDto);
        Teacher saved = teacherRepository.save(teacherToSave);

        return teacherMapper.map(saved);
    }

    public Optional<TeacherDto> findById(Long id) {
        return teacherRepository.findById(id).map(teacherMapper::map);
    }

    public void deleteById(Long id) {
        teacherRepository.deleteById(id);
    }

}
