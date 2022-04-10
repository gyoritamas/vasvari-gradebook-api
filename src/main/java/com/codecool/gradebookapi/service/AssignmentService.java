package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import com.codecool.gradebookapi.dto.mapper.AssignmentMapper;
import com.codecool.gradebookapi.exception.StudentNotFoundException;
import com.codecool.gradebookapi.exception.TeacherNotFoundException;
import com.codecool.gradebookapi.model.Assignment;
import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.model.Subject;
import com.codecool.gradebookapi.model.Teacher;
import com.codecool.gradebookapi.model.request.AssignmentRequest;
import com.codecool.gradebookapi.model.specification.AssignmentSpecification;
import com.codecool.gradebookapi.repository.AssignmentRepository;
import com.codecool.gradebookapi.repository.StudentRepository;
import com.codecool.gradebookapi.repository.SubjectRepository;
import com.codecool.gradebookapi.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final AssignmentMapper mapper;
    private final AssignmentSpecification specification;

    public List<AssignmentOutput> findAll() {
        return mapper.mapAll(assignmentRepository.findAll());
    }

    public List<AssignmentOutput> findAssignments(AssignmentRequest request){
        return mapper.mapAll(assignmentRepository.findAll(specification.getAssignments(request)));
    }

    public AssignmentOutput save(AssignmentInput assignmentInput) {
        Assignment assignment = mapper.map(assignmentInput);
        Assignment assignmentSaved = assignmentRepository.save(assignment);

        return mapper.map(assignmentSaved);
    }

    public AssignmentOutput update(Long id, AssignmentInput assignmentInput) {
        Assignment update = mapper.map(assignmentInput);
        update.setId(id);
        Assignment assignmentUpdated = assignmentRepository.save(update);

        return mapper.map(assignmentUpdated);
    }

    public Optional<AssignmentOutput> findById(Long id) {
        return assignmentRepository.findById(id).map(mapper::map);
    }

    public void deleteById(Long id) {
        assignmentRepository.deleteById(id);
    }

    public List<AssignmentOutput> findAssignmentsOfTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));
        List<Subject> subjectsOfTeacher = subjectRepository.findSubjectsByTeacher(teacher);
        List<Assignment> assignmentsOfTeacher = assignmentRepository.findAllBySubjectIn(subjectsOfTeacher);

        return mapper.mapAll(assignmentsOfTeacher);
    }

    public List<AssignmentOutput> findAssignmentsOfStudent(Long studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        List<Subject> subjectsOfStudent = subjectRepository.findSubjectsByStudentsContaining(student);
        List<Assignment> assignmentsOfStudent = assignmentRepository.findAllBySubjectIn(subjectsOfStudent);

        return mapper.mapAll(assignmentsOfStudent);
    }
}
