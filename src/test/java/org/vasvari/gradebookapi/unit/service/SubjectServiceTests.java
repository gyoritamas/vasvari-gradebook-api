package org.vasvari.gradebookapi.unit.service;

import org.vasvari.gradebookapi.dto.StudentDto;
import org.vasvari.gradebookapi.dto.SubjectInput;
import org.vasvari.gradebookapi.dto.SubjectOutput;
import org.vasvari.gradebookapi.dto.TeacherDto;
import org.vasvari.gradebookapi.dto.simpleTypes.SimpleStudent;
import org.vasvari.gradebookapi.exception.SubjectNotFoundException;
import org.vasvari.gradebookapi.service.StudentService;
import org.vasvari.gradebookapi.service.SubjectService;
import org.vasvari.gradebookapi.service.TeacherService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

@SpringBootTest
public class SubjectServiceTests {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    private SubjectInput subject1;
    private SubjectInput subject2;
    private StudentDto student;
    private TeacherDto teacher1;
    private TeacherDto teacher2;

    @BeforeEach
    public void setUp() {
        subject1 = SubjectInput.builder()
                .name("Algebra")
                .build();
        subject2 = SubjectInput.builder()
                .name("Biology")
                .build();
        student = StudentDto.builder()
                .firstname("John")
                .lastname("Doe")
                .gradeLevel(11)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate(LocalDate.of(2004, 2, 1))
                .build();
        teacher1 = TeacherDto.builder()
                .firstname("Darrell")
                .lastname("Bowen")
                .email("darrellbowen@email.com")
                .address("3982 Turnpike Drive, Birmingham, AL 35203")
                .phone("619-446-8496")
                .birthdate(LocalDate.of(1984, 2, 1))
                .build();
        teacher2 = TeacherDto.builder()
                .firstname("Lilian")
                .lastname("Stafford")
                .email("lilianstafford@email.com")
                .address("4498 Sugar Camp Road, Vernon Center, MN 56090")
                .phone("507-549-1665")
                .birthdate(LocalDate.of(1985, 4, 13))
                .build();
    }

    @Test
    @Transactional
    @DisplayName("save should return saved Subject")
    public void saveShouldReturnSavedSubject() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        SubjectOutput subjectSaved = subjectService.save(subject1);

        assertThat(subjectSaved.getId()).isNotNull();
        assertThat(subjectSaved.getName()).isEqualTo("Algebra");
        Assertions.assertThat(subjectSaved.getStudents()).isEmpty();
    }

    @Test
    @Transactional
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("findAll should return list of Subjects")
    public void findAll_shouldReturnListOfSubjects() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        subject2.setTeacherId(teacherId);
        SubjectOutput output1 = subjectService.save(subject1);
        SubjectOutput output2 = subjectService.save(subject2);

        List<SubjectOutput> subjects = subjectService.findAll();

        assertThat(subjects).hasSize(2);
        assertThat(subjects).containsExactly(output1, output2);
    }

    @Test
    @Transactional
    @DisplayName("when Subject with given ID exists, findById should return Subject")
    public void whenSubjectWithGivenIdExists_findByIdShouldReturnSubject() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        Long id = subjectService.save(subject1).getId();

        Optional<SubjectOutput> subjectFound = subjectService.findById(id);

        assertThat(subjectFound).isPresent();
        assertThat(subjectFound.get().getName()).isEqualTo("Algebra");
    }

    @Test
    @Transactional
    @DisplayName("when Subject with given ID does not exist, findById should return empty Optional")
    public void whenSubjectWithGivenIdDoesNotExist_findByIdShouldReturnEmptyOptional() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        subject2.setTeacherId(teacherId);
        Long id = subjectService.save(subject2).getId();

        Optional<SubjectOutput> subjectFound = subjectService.findById(id + 1);

        assertThat(subjectFound).isEqualTo(Optional.empty());
    }

    @Test
    @Transactional
    @DisplayName("deleteById should delete Subject with given ID")
    public void deleteById_shouldDeleteSubjectWithGivenId() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        long id = subjectService.save(subject1).getId();

        subjectService.deleteById(id);
        Optional<SubjectOutput> subjectFound = subjectService.findById(id);

        assertThat(subjectFound).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when Subject with given ID exists, save should update existing Subject")
    public void whenSubjectWithGivenIdExists_saveShouldUpdateExistingSubject() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        long id = subjectService.save(subject1).getId();
        subject1.setName("Algebra II");
        SubjectOutput updatedSubject = subjectService.update(id, subject1);

        assertThat(updatedSubject).isNotNull();
        assertThat(updatedSubject.getName()).isEqualTo("Algebra II");
    }

    @Test
    @Transactional
    @DisplayName("addStudentToSubject should add Student to list of students of Subject")
    public void addStudentToSubject_shouldAddStudentToListOfStudentsOfSubject() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        student = studentService.save(student);
        SubjectOutput subjectSaved = subjectService.save(subject1);

        subjectSaved = subjectService.addStudentToSubject(student.getId(), subjectSaved.getId());
        List<SimpleStudent> listOfStudents = subjectSaved.getStudents();
        SimpleStudent simpleStudent = SimpleStudent.builder()
                .id(student.getId())
                .firstname(student.getFirstname())
                .lastname(student.getLastname())
                .build();

        assertThat(listOfStudents).containsExactly(simpleStudent);
    }

    @Test
    @Transactional
    @DisplayName("removeStudentFromSubject should remove Student from list of students of Subject")
    public void removeStudentFromSubject_shouldRemoveStudentFromListOfStudentsOfSubject() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        student = studentService.save(student);
        SubjectOutput subject = subjectService.save(subject1);
        subject = subjectService.addStudentToSubject(student.getId(), subject.getId());

        subject = subjectService.removeStudentFromSubject(student.getId(), subject.getId());
        List<SimpleStudent> listOfStudents = subject.getStudents();

        assertThat(listOfStudents).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when Student exists with given ID, findSubjectsOfStudent should return list of Subjects")
    public void whenStudentExistsWithGivenId_findSubjectsOfStudentShouldReturnListOfSubjects() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        subject2.setTeacherId(teacherId);
        student = studentService.save(student);
        SubjectOutput subject1Saved = subjectService.save(subject1);
        SubjectOutput subject2Saved = subjectService.save(subject2);
        subject1Saved = subjectService.addStudentToSubject(student.getId(), subject1Saved.getId());
        subject2Saved = subjectService.addStudentToSubject(student.getId(), subject2Saved.getId());

        List<SubjectOutput> listOfSubjects = studentService.findSubjectsOfStudent(student);

        assertThat(listOfSubjects).containsExactly(subject1Saved, subject2Saved);
    }

    @Test
    @Transactional
    @DisplayName("when Student is enrolled in given Subject, isStudentInSubject should return true")
    public void whenStudentIsEnrolledInGivenSubject_isStudentInSubjectShouldReturnTrue() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        long studentId = studentService.save(student).getId();
        long subjectId = subjectService.save(subject1).getId();
        subjectService.addStudentToSubject(studentId, subjectId);

        assertTrue(subjectService.isStudentAddedToSubject(studentId, subjectId));
    }

    @Test
    @Transactional
    @DisplayName("when Student is not enrolled in given Subject, isStudentInSubject should return false")
    public void whenStudentIsNotEnrolledInGivenSubject_isStudentInSubjectShouldReturnFalse() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        long studentId = studentService.save(student).getId();
        long subjectId = subjectService.save(subject1).getId();

        assertFalse(subjectService.isStudentAddedToSubject(studentId, subjectId));
    }

    @Test
    @Transactional
    @DisplayName("given Subject exists with ID, getStudentOfSubject should return list of Students")
    public void givenSubjectExistsWithId_getStudentsOfSubject_shouldReturnListOfStudents() {
        long teacherId = teacherService.save(teacher1).getId();
        subject1.setTeacherId(teacherId);
        StudentDto studentSaved = studentService.save(student);
        SubjectOutput subjectSaved = subjectService.save(subject1);
        subjectService.addStudentToSubject(studentSaved.getId(), subjectSaved.getId());

        List<StudentDto> studentsOfSubject = subjectService.getStudentsOfSubject(subjectSaved.getId());

        assertThat(studentsOfSubject).containsExactly(studentSaved);
    }

    @Test
    @DisplayName("given Subject does not exist with ID, getStudentsOfSubject should throw exception")
    public void givenSubjectDoesNotExistWithId_getStudentsOfSubject_shouldThrowException() {
        assertThatThrownBy(() -> subjectService.getStudentsOfSubject(99L))
                .isInstanceOf(SubjectNotFoundException.class)
                .hasMessage(SubjectNotFoundException.ERROR_MESSAGE, 99L);
    }

    @Test
    @DisplayName("given Teacher is not set as teacher of any Subjects, findSubjectsOfTeacher should return empty list")
    public void givenTeacherIsNotSetAsTeacherOfAnySubjects_findSubjectsOfTeacher_shouldReturnListOfSubjects() {
        TeacherDto teacherSaved = teacherService.save(teacher1);
        List<SubjectOutput> subjectsOfTeacher = subjectService.findSubjectsOfTeacher(teacherSaved);

        assertThat(subjectsOfTeacher).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("given Teacher is set as teacher of Subjects, findSubjectsOfTeacher should return list of Subjects")
    public void givenTeacherIsSetAsTeacherOfSubjects_findSubjectsOfTeacher_shouldReturnListOfSubjects() {
        teacher1 = teacherService.save(teacher1);
        subject1.setTeacherId(teacher1.getId());
        subject2.setTeacherId(teacher1.getId());
        SubjectOutput subject1Saved = subjectService.save(subject1);
        SubjectOutput subject2Saved = subjectService.save(subject2);

        List<SubjectOutput> subjectsOfTeacher = subjectService.findSubjectsOfTeacher(teacher1);

        assertThat(subjectsOfTeacher).containsExactly(subject1Saved, subject2Saved);
    }

    @Test
    @Transactional
    @DisplayName("findStudentsOfTeacher should return Students of Subjects which the Teacher is teaching")
    public void findStudentsOfTeacher_shouldReturnStudentsOfSubjectsWhichTheTeacherIsTeaching() {
        // save students
        StudentDto johnDoe = StudentDto.builder()
                .firstname("John").lastname("Doe")
                .gradeLevel(11)
                .email("johndoe@gmail.com")
                .address("").phone("")
                .birthdate(LocalDate.of(2005, 1, 1))
                .build();
        StudentDto janeDoe = StudentDto.builder()
                .firstname("Jane").lastname("Doe")
                .gradeLevel(11)
                .email("janedoe@gmail.com")
                .address("").phone("")
                .birthdate(LocalDate.of(2004, 11, 21))
                .build();
        StudentDto jimDoe = StudentDto.builder()
                .firstname("Jim")
                .lastname("Doe")
                .gradeLevel(11)
                .email("jimdoe@gmail.com")
                .address("").phone("")
                .birthdate(LocalDate.of(2005, 3, 9))
                .build();
        StudentDto jackDoe = StudentDto.builder()
                .firstname("Jack")
                .lastname("Doe")
                .gradeLevel(11)
                .email("jackdoe@gmail.com")
                .address("").phone("")
                .birthdate(LocalDate.of(2005, 2, 27))
                .build();
        johnDoe = studentService.save(johnDoe);
        janeDoe = studentService.save(janeDoe);
        jimDoe = studentService.save(jimDoe);
        jackDoe = studentService.save(jackDoe);

        // save teacher
        TeacherDto darrelBowen = teacherService.save(teacher1);
        TeacherDto lilianStafford = teacherService.save(teacher2);

        subject1.setTeacherId(darrelBowen.getId());
        subject2.setTeacherId(lilianStafford.getId());

        // save subjects
        SubjectOutput algebra = subjectService.save(subject1);
        SubjectOutput biology = subjectService.save(subject2);
        SubjectOutput physics = subjectService.save(
                SubjectInput.builder().name("Physics").teacherId(darrelBowen.getId()).build()
        );

        // add students to subjects
        algebra = subjectService.addStudentToSubject(johnDoe.getId(), algebra.getId());
        algebra = subjectService.addStudentToSubject(janeDoe.getId(), algebra.getId());
        physics = subjectService.addStudentToSubject(jimDoe.getId(), physics.getId());
        physics = subjectService.addStudentToSubject(johnDoe.getId(), physics.getId());
        biology = subjectService.addStudentToSubject(jackDoe.getId(), biology.getId());

        List<StudentDto> studentsOfTeacher = subjectService.findStudentsOfTeacher(darrelBowen);

        assertThat(studentsOfTeacher).containsExactlyInAnyOrder(johnDoe, janeDoe, jimDoe);
    }
}
