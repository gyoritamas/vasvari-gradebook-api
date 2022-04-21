package org.vasvari.gradebookapi.service;

import org.vasvari.gradebookapi.dto.SubjectOutput;
import org.vasvari.gradebookapi.dto.StudentDto;
import org.vasvari.gradebookapi.dto.mapper.SubjectMapper;
import org.vasvari.gradebookapi.dto.mapper.StudentMapper;
import org.vasvari.gradebookapi.model.specification.StudentSpecification;
import org.vasvari.gradebookapi.model.Subject;
import org.vasvari.gradebookapi.model.Student;
import org.vasvari.gradebookapi.model.request.StudentRequest;
import org.vasvari.gradebookapi.repository.SubjectRepository;
import org.vasvari.gradebookapi.repository.StudentRepository;
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
