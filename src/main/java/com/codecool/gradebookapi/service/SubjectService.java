package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.SubjectInput;
import com.codecool.gradebookapi.dto.SubjectOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.mapper.SubjectMapper;
import com.codecool.gradebookapi.dto.mapper.StudentMapper;
import com.codecool.gradebookapi.dto.mapper.TeacherMapper;
import com.codecool.gradebookapi.exception.SubjectNotFoundException;
import com.codecool.gradebookapi.model.Subject;
import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.model.specification.SubjectSpecification;
import com.codecool.gradebookapi.model.Teacher;
import com.codecool.gradebookapi.model.request.SubjectRequest;
import com.codecool.gradebookapi.repository.SubjectRepository;
import com.codecool.gradebookapi.repository.StudentRepository;
import com.codecool.gradebookapi.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final SubjectMapper subjectMapper;
    private final SubjectSpecification specification;

    public List<SubjectOutput> findAll() {
        return subjectMapper.mapAll(subjectRepository.findAll());
    }

    public List<SubjectOutput> findSubjects(SubjectRequest request) {
        return subjectMapper.mapAll(subjectRepository.findAll(specification.getSubjects(request)));
    }

    public SubjectOutput save(SubjectInput subjectInput) {
        Subject subject = Subject.builder()
                .name(subjectInput.getName())
                .teacher(
                        teacherRepository.getById(subjectInput.getTeacherId())
                )
                .students(new HashSet<>())
                .build();

        Subject subjectCreated = subjectRepository.save(subject);

        return subjectMapper.map(subjectCreated);
    }

    public SubjectOutput update(Long id, SubjectInput subjectInput) {
        Subject subject = Subject.builder()
                .id(id)
                .name(subjectInput.getName())
                .teacher(
                        teacherRepository.getById(subjectInput.getTeacherId())
                )
                .students(subjectRepository.getById(id).getStudents())
                .build();

        Subject subjectUpdated = subjectRepository.save(subject);

        return subjectMapper.map(subjectUpdated);
    }

    public Optional<SubjectOutput> findById(Long id) {
        return subjectRepository.findById(id).map(subjectMapper::map);
    }

    public void deleteById(Long id) {
        subjectRepository.deleteById(id);
    }

    public SubjectOutput addStudentToSubject(Long studentId, Long subjectId) {
        Student student = studentRepository.getById(studentId);
        Subject subject = subjectRepository.getById(subjectId);
        subject.addStudent(student);

        return subjectMapper.map(subjectRepository.save(subject));
    }

    public SubjectOutput removeStudentFromSubject(Long studentId, Long subjectId) {
        Student student = studentRepository.getById(studentId);
        Subject subject = subjectRepository.getById(subjectId);
        subject.getStudents().remove(student);

        return subjectMapper.map(subjectRepository.save(subject));
    }

    public boolean isStudentAddedToSubject(Long studentId, Long subjectId) {
        Student student = studentRepository.getById(studentId);

        return subjectRepository.findSubjectsByStudentsContainingAndId(student, subjectId).isPresent();
    }

    public List<StudentDto> getStudentsOfSubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> new SubjectNotFoundException(subjectId));

        return studentMapper.mapAll(subject.getStudents());
    }

    public List<SubjectOutput> findSubjectsOfTeacher(TeacherDto teacherDto) {
        return subjectMapper.mapAll(findSubjectsByTeacher(teacherDto));
    }

    public List<StudentDto> findStudentsOfTeacher(TeacherDto teacherDto) {
        List<Subject> subjects = findSubjectsByTeacher(teacherDto);
        Set<Student> studentsSet = new HashSet<>();
        for (Subject subject : subjects) {
            studentsSet.addAll(subject.getStudents());
        }
        List<Student> studentsList = studentsSet.stream()
                .sorted(Comparator.comparing(Student::getId))
                .collect(Collectors.toList());

        return studentMapper.mapAll(studentsList);
    }

    private List<Subject> findSubjectsByTeacher(TeacherDto teacherDto) {
        Teacher teacher = teacherMapper.map(teacherDto);
        return subjectRepository.findSubjectsByTeacher(teacher);
    }
}
