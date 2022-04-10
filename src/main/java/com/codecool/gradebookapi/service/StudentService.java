package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.SubjectOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.mapper.SubjectMapper;
import com.codecool.gradebookapi.dto.mapper.StudentMapper;
import com.codecool.gradebookapi.model.specification.StudentSpecification;
import com.codecool.gradebookapi.model.Subject;
import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.model.request.StudentRequest;
import com.codecool.gradebookapi.repository.SubjectRepository;
import com.codecool.gradebookapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final StudentMapper studentMapper;
    private final SubjectMapper subjectMapper;
    private final StudentSpecification specification;

    public List<StudentDto> findAll() {
        return studentMapper.mapAll(studentRepository.findAll());
    }

    public List<StudentDto> findStudents(StudentRequest request) {
        return studentMapper.mapAll(studentRepository.findAll(specification.getStudents(request)));
    }

    public StudentDto save(StudentDto studentDto) {
        Student studentToSave = studentMapper.map(studentDto);
        Student saved = studentRepository.save(studentToSave);

        return studentMapper.map(saved);
    }

    public Optional<StudentDto> findById(Long id) {
        return studentRepository.findById(id).map(studentMapper::map);
    }

    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    public List<SubjectOutput> findSubjectsOfStudent(StudentDto studentDto) {
        Student student = studentMapper.map(studentDto);
        List<Subject> subjects = subjectRepository.findSubjectsByStudentsContaining(student);

        return subjectMapper.mapAll(subjects);
    }

}
