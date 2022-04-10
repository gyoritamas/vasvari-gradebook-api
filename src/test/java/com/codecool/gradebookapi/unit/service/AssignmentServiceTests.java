package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.*;
import com.codecool.gradebookapi.exception.StudentNotFoundException;
import com.codecool.gradebookapi.exception.TeacherNotFoundException;
import com.codecool.gradebookapi.model.AssignmentType;
import com.codecool.gradebookapi.model.request.AssignmentRequest;
import com.codecool.gradebookapi.service.AssignmentService;
import com.codecool.gradebookapi.service.StudentService;
import com.codecool.gradebookapi.service.SubjectService;
import com.codecool.gradebookapi.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

@SpringBootTest
public class AssignmentServiceTests {

    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private StudentService studentService;

    private AssignmentInput assignmentInput1;
    private AssignmentInput assignmentInput2;

    private Long teacherId;
    private Long studentId;
    private Long subjectId;

    @BeforeEach
    public void setUp() {
        TeacherDto teacher = TeacherDto.builder()
                .firstname("Darrell")
                .lastname("Bowen")
                .email("darrellbowen@email.com")
                .address("3982 Turnpike Drive, Birmingham, AL 35203")
                .phone("619-446-8496")
                .birthdate(LocalDate.of(1984, 2, 1))
                .build();

        teacherId = teacherService.save(teacher).getId();

        StudentDto student = StudentDto.builder()
                .firstname("John")
                .lastname("Doe")
                .gradeLevel(2)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate(LocalDate.of(1990, 12, 1))
                .build();

        studentId = studentService.save(student).getId();

        SubjectInput subject = SubjectInput.builder()
                .name("Algebra")
                .teacherId(teacherId)
                .build();

        subjectId = subjectService.save(subject).getId();

        subjectService.addStudentToSubject(studentId, subjectId);

        assignmentInput1 = AssignmentInput.builder()
                .name("Algebra Homework #11")
                .type(AssignmentType.HOMEWORK)
                .description("Read chapters 9 to 11")
                .deadline(LocalDate.of(2051, 1, 1))
                .subjectId(subjectId)
                .build();
        assignmentInput2 = AssignmentInput.builder()
                .name("Algebra Test #5")
                .type(AssignmentType.TEST)
                .description("Quadratic equations")
                .deadline(LocalDate.of(2052, 1, 1))
                .subjectId(subjectId)
                .build();
    }

    @Test
    @Transactional
    @DisplayName("save should return saved Assignment")
    public void saveShouldReturnAssignment() {
        AssignmentOutput assignmentSaved = assignmentService.save(assignmentInput1);

        assertThat(assignmentSaved.getId()).isNotNull();
        assertThat(assignmentSaved.getName()).isEqualTo(assignmentInput1.getName());
        assertThat(assignmentSaved.getType()).isEqualTo(assignmentInput1.getType());
        assertThat(assignmentSaved.getDescription()).isEqualTo(assignmentInput1.getDescription());
        assertThat(assignmentSaved.getDeadline()).isNotNull();
    }

    @Test
    @Transactional
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("findAll should return list of Assignments")
    public void findAll_shouldReturnListOfAssignments() {
        assignmentService.save(assignmentInput1);
        assignmentService.save(assignmentInput2);

        List<AssignmentOutput> actualListOfAssignments = assignmentService.findAll();

        assertThat(actualListOfAssignments).hasSize(2);
        assertThat(actualListOfAssignments.get(0).getName()).isEqualTo(assignmentInput1.getName());
        assertThat(actualListOfAssignments.get(1).getName()).isEqualTo(assignmentInput2.getName());
    }

    @Test
    @Transactional
    @DisplayName("when Assignment with given ID exists, findById should return Assignment")
    public void whenAssignmentWithGivenIdExists_findByIdShouldReturnAssignment() {
        AssignmentOutput saved = assignmentService.save(assignmentInput1);

        Optional<AssignmentOutput> assignmentFound = assignmentService.findById(saved.getId());

        assertThat(assignmentFound).isPresent();
        assertThat(assignmentFound.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @Transactional
    @DisplayName("when Assignment with given ID does not exist, findById should return empty Optional")
    public void whenAssignmentWithGivenIdDoesNotExist_findByIdShouldReturnEmptyOptional() {
        Long id = assignmentService.save(assignmentInput2).getId();

        Optional<AssignmentOutput> assignmentFound = assignmentService.findById(id + 1);

        assertThat(assignmentFound).isEqualTo(Optional.empty());
    }

    @Test
    @Transactional
    @DisplayName("deleteById should delete Assignment with given ID")
    public void deleteById_shouldDeleteAssignmentWithGivenId() {
        long id = assignmentService.save(assignmentInput1).getId();

        assignmentService.deleteById(id);
        Optional<AssignmentOutput> assignmentFound = assignmentService.findById(id);

        assertThat(assignmentFound).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when Assignment with given ID already exists, save should update existing Assignment")
    public void whenAssignmentWithGivenIdAlreadyExists_saveShouldUpdateExistingAssignment() {
        long id = assignmentService.save(assignmentInput1).getId();
        AssignmentInput update = assignmentInput1;
        update.setName("Updated name");
        assignmentService.update(id, update);

        AssignmentOutput updatedAssignment = assignmentService.findById(id).orElse(null);

        assertThat(updatedAssignment).isNotNull();
        assertThat(updatedAssignment.getName()).isEqualTo("Updated name");
    }

    @Test
    @Transactional
    @DisplayName("when Assignments exist with Subject which given Teacher is teaching, findAssignmentsOfTeacher should return list of Assignments")
    public void whenAssignmentsExistWithSubjectWhichGivenTeacherIsTeaching_findAssignmentsOfTeacher_shouldReturnListOfAssignments() {
        AssignmentOutput assignment1 = assignmentService.save(assignmentInput1);
        AssignmentOutput assignment2 = assignmentService.save(assignmentInput2);

        List<AssignmentOutput> assignmentsOfTeacher = assignmentService.findAssignmentsOfTeacher(teacherId);

        assertThat(assignmentsOfTeacher).containsExactly(assignment1, assignment2);
    }

    @Test
    @Transactional
    @DisplayName("when Assignment does not exist with Subject which given Teacher is teaching, findAssignmentsOfTeacher should return empty list")
    public void whenAssignmentDoesNotExistWithSubjectWhichGivenTeacherIsTeaching_findAssignmentsOfTeacher_shouldReturnEmptyList() {
        List<AssignmentOutput> assignmentsOfTeacher = assignmentService.findAssignmentsOfTeacher(teacherId);

        assertThat(assignmentsOfTeacher).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when Teacher does not exist with given ID, findAssignmentsOfTeacher should throw exception")
    public void whenTeacherDoesNotExistWithGivenId_findAssignmentsOfTeacher_shouldThrowException() {
        assertThatThrownBy(() -> assignmentService.findAssignmentsOfTeacher(teacherId + 1))
                .isInstanceOf(TeacherNotFoundException.class)
                .hasMessage(String.format(TeacherNotFoundException.ERROR_MESSAGE, teacherId + 1));
    }

    @Test
    @Transactional
    @DisplayName("when Assignments exist with Subject which given Student is learning, findAssignmentsOfStudent should return list of Assignments")
    public void whenAssignmentsExistWithSubjectWhichGivenStudentIsLearning_findAssignmentsOfStudent_shouldReturnListOfAssignments() {
        AssignmentOutput assignment1 = assignmentService.save(assignmentInput1);
        AssignmentOutput assignment2 = assignmentService.save(assignmentInput2);

        List<AssignmentOutput> assignmentsOfStudent = assignmentService.findAssignmentsOfStudent(studentId);

        assertThat(assignmentsOfStudent).containsExactly(assignment1, assignment2);
    }

    @Test
    @Transactional
    @DisplayName("when Assignment does not exist with Subject which given Student is learning, findAssignmentsOfStudent should return empty list")
    public void whenAssignmentDoesNotExistWithSubjectWhichGivenStudentIsLearning_findAssignmentsOfStudent_shouldReturnEmptyList() {
        List<AssignmentOutput> assignmentsOfStudent = assignmentService.findAssignmentsOfStudent(studentId);

        assertThat(assignmentsOfStudent).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when Student with given ID does not exist, findAssignmentsOfStudent should throw exception")
    public void whenStudentWithGivenIdDoesNotExist_findAssignmentsOfStudent_shouldThrowException() {
        assertThatThrownBy(() -> assignmentService.findAssignmentsOfStudent(studentId + 1))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessage(String.format(StudentNotFoundException.ERROR_MESSAGE, studentId + 1));

    }

    @Test
    @Transactional
    @DisplayName("when Assignment exists with given title, type and subjectId, findAssignments should return list of Assignments")
    public void whenAssignmentExistsWithGivenTitleTypeAndSubjectId_findAssignmentsTests_shouldReturnListOfAssignments() {
        AssignmentOutput assignment1 = assignmentService.save(assignmentInput1);
        AssignmentOutput assignment2 = assignmentService.save(assignmentInput2);

        AssignmentRequest[] requests = {
                new AssignmentRequest(),
                AssignmentRequest.builder().title("test #5").build(),
                AssignmentRequest.builder().type(AssignmentType.PROJECT).build(),
                AssignmentRequest.builder().subjectId(subjectId).build(),
                AssignmentRequest.builder().type(AssignmentType.HOMEWORK).subjectId(subjectId).build(),
                AssignmentRequest.builder().title("homework").type(AssignmentType.TEST).build(),
                AssignmentRequest.builder().title("test").type(AssignmentType.TEST).subjectId(subjectId).build()
        };

        Map<Integer, List<AssignmentOutput>> results = new HashMap<>();

        for (int i = 0; i < requests.length; i++) {
            results.put(i, assignmentService.findAssignments(requests[i]));
        }

        assertThat(results.get(0)).containsExactly(assignment1, assignment2);
        assertThat(results.get(1)).containsExactly(assignment2);
        assertThat(results.get(2)).isEmpty();
        assertThat(results.get(3)).containsExactly(assignment1, assignment2);
        assertThat(results.get(4)).containsExactly(assignment1);
        assertThat(results.get(5)).isEmpty();
        assertThat(results.get(6)).containsExactly(assignment2);
    }

}
