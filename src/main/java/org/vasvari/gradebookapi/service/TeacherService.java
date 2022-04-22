package org.vasvari.gradebookapi.service;

import org.vasvari.gradebookapi.dto.TeacherDto;
import org.vasvari.gradebookapi.dto.mapper.TeacherMapper;
import org.vasvari.gradebookapi.model.Teacher;
import org.vasvari.gradebookapi.model.specification.TeacherSpecification;
import org.vasvari.gradebookapi.model.request.TeacherRequest;
import org.vasvari.gradebookapi.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    private final TeacherSpecification specification;

    public List<TeacherDto> findAll() {
        return teacherMapper.mapAll(teacherRepository.findAll());
    }

    public List<TeacherDto> findTeachers(TeacherRequest request) {
        return teacherMapper.mapAll(teacherRepository.findAll(specification.getTeachers(request)));
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
