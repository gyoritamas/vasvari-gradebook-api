package org.vasvari.gradebookapi.service;

import org.vasvari.gradebookapi.dto.AssignmentInput;
import org.vasvari.gradebookapi.dto.AssignmentOutput;
import org.vasvari.gradebookapi.dto.mapper.AssignmentMapper;
import org.vasvari.gradebookapi.exception.StudentNotFoundException;
import org.vasvari.gradebookapi.exception.TeacherNotFoundException;
import org.vasvari.gradebookapi.model.Assignment;
import org.vasvari.gradebookapi.model.Student;
import org.vasvari.gradebookapi.model.Subject;
import org.vasvari.gradebookapi.model.Teacher;
import org.vasvari.gradebookapi.model.request.AssignmentRequest;
import org.vasvari.gradebookapi.model.specification.AssignmentSpecification;
import org.vasvari.gradebookapi.repository.AssignmentRepository;
import org.vasvari.gradebookapi.repository.StudentRepository;
import org.vasvari.gradebookapi.repository.SubjectRepository;
import org.vasvari.gradebookapi.repository.TeacherRepository;
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

    public List<AssignmentOutput> findAssignments(AssignmentRequest request) {
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

    /**
     * Returns the list of assignments created by the teacher specified by the teacherId param
     *
     * @param teacherId the ID of the teacher whose assignments are looked for
     * @return list of assignments
     * @throws TeacherNotFoundException if teacher does not exist with the given ID
     */
    public List<AssignmentOutput> findAssignmentsOfTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));
        List<Subject> subjectsOfTeacher = subjectRepository.findSubjectsByTeacher(teacher);
        List<Assignment> assignmentsOfTeacher = assignmentRepository.findAllBySubjectIn(subjectsOfTeacher);

        return mapper.mapAll(assignmentsOfTeacher);
    }

    /**
     * Returns the list of assignments created for the student specified by the studentId param
     *
     * @param studentId the ID of the student whose assignments are looked for
     * @return list of assignments
     * @throws StudentNotFoundException if student does not exist with the given ID
     */
    public List<AssignmentOutput> findAssignmentsOfStudent(Long studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        List<Subject> subjectsOfStudent = subjectRepository.findSubjectsByStudentsContaining(student);
        List<Assignment> assignmentsOfStudent = assignmentRepository.findAllBySubjectIn(subjectsOfStudent);

        return mapper.mapAll(assignmentsOfStudent);
    }
}
