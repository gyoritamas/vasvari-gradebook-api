package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.model.request.StudentRequest;
import com.codecool.gradebookapi.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class StudentServiceTests {

    @Autowired
    private StudentService service;

    private StudentDto student1;
    private StudentDto student2;

    @BeforeEach
    public void setUp() {
        student1 = StudentDto.builder()
                .firstname("John")
                .lastname("Doe")
                .gradeLevel(2)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate(LocalDate.of(1990, 12, 1))
                .build();

        student2 = StudentDto.builder()
                .firstname("Jane")
                .lastname("Doe")
                .gradeLevel(3)
                .email("janedoe@email.com")
                .address("9351 Morris St., Reisterstown, MD 21136")
                .phone("202-555-0198")
                .birthdate(LocalDate.of(1990, 4, 13))
                .build();
    }

    @Test
    @DisplayName("save should return saved Student")
    public void saveShouldReturnSavedStudent() {
        StudentDto studentSaved = service.save(student1);

        assertThat(studentSaved.getName()).isEqualTo("John Doe");
        assertThat(studentSaved.getGradeLevel()).isEqualTo(2);
        assertThat(studentSaved.getEmail()).isEqualTo("johndoe@email.com");
        assertThat(studentSaved.getAddress()).isEqualTo("666 Armstrong St., Mesa, AZ 85203");
        assertThat(studentSaved.getPhone()).isEqualTo("202-555-0198");
        assertThat(studentSaved.getBirthdate()).isEqualTo("1990-12-01");
    }

    @Test
    @DisplayName("findAll should return list of Students")
    public void findAll_shouldReturnListOfStudents() {
        student1 = service.save(student1);
        student2 = service.save(student2);

        List<StudentDto> actualListOfStudents = service.findAll();

        assertThat(actualListOfStudents).containsExactly(student1, student2);
    }

    @Test
    @DisplayName("when Student with given ID exists, findById should return Student")
    public void whenStudentWithGivenIdExists_findByIdShouldReturnStudent() {
        student1 = service.save(student1);

        Optional<StudentDto> studentFound = service.findById(student1.getId());

        assertThat(studentFound).isPresent();
        assertThat(studentFound.get()).isEqualTo(student1);
    }

    @Test
    @DisplayName("when Student with given ID does not exist, findById should return empty Optional")
    public void whenStudentWithGivenIdDoesNotExist_findByIdShouldReturnEmptyOptional() {
        Long id = service.save(student2).getId();

        Optional<StudentDto> studentFound = service.findById(id + 1);

        assertThat(studentFound).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("deleteById should delete Student with given ID")
    public void deleteById_shouldDeleteStudentWithGivenId() {
        long id = service.save(student1).getId();

        service.deleteById(id);
        Optional<StudentDto> studentFound = service.findById(id);

        assertThat(studentFound).isEmpty();
    }

    @Test
    @DisplayName("when Student with given ID already exists, save should update existing Student")
    public void whenStudentWithGivenIdAlreadyExists_saveShouldUpdateExistingStudent() {
        StudentDto student = service.save(student1);

        student.setFirstname("Johnathan");
        service.save(student);
        Optional<StudentDto> updatedStudent = service.findById(student.getId());

        assertThat(updatedStudent).isPresent();
        assertThat(updatedStudent.get().getFirstname()).isEqualTo("Johnathan");
    }

    @Test
    @DisplayName("when Student exists with given name, findAll should return list of Students")
    public void whenStudentExistsWithGivenName_findAllShouldReturnListOfStudents() {
        student1 = service.save(student1);
        student2 = service.save(student2);

        StudentRequest request1 = new StudentRequest();
        request1.setName("john");
        List<StudentDto> studentsWithJohnInName = service.findAll(request1);

        assertThat(studentsWithJohnInName).containsExactly(student1);

        StudentRequest request2 = new StudentRequest();
        request2.setName("doe");
        List<StudentDto> studentsWithDoeInName = service.findAll(request2);

        assertThat(studentsWithDoeInName).containsExactly(student1, student2);

        StudentRequest request3 = new StudentRequest();
        request3.setName("john doe");
        List<StudentDto> studentsWithJohnDoeInName = service.findAll(request3);

        assertThat(studentsWithJohnDoeInName).containsExactly(student1);
    }

    @Test
    @DisplayName("when Student does not exist with given name, findAll should return empty list")
    public void whenStudentDoesNotExistWithGivenName_findAllShouldReturnEmtpyList() {
        student1 = service.save(student1);
        student2 = service.save(student2);

        StudentRequest request = new StudentRequest();
        request.setName("jim");
        List<StudentDto> studentsWithJimInName = service.findAll(request);

        assertThat(studentsWithJimInName).isEmpty();
    }

    @Test
    @DisplayName("when Student exists with given gradeLevel, findAll should return list of Students")
    public void whenStudentExistsWithGivenGradeLevel_findAllShouldReturnListOfStudents() {
        student1 = service.save(student1);
        student2 = service.save(student2);

        StudentRequest request1 = new StudentRequest();
        request1.setGradeLevel(2);
        List<StudentDto> studentsFromSecondGrade = service.findAll(request1);

        assertThat(studentsFromSecondGrade).containsExactly(student1);

        StudentRequest request2 = new StudentRequest();
        request2.setGradeLevel(3);
        List<StudentDto> studentsFromThirdGrade = service.findAll(request2);

        assertThat(studentsFromThirdGrade).containsExactly(student2);
    }

    @Test
    @DisplayName("when Student does not exist with given gradeLevel, findAll should return empty list")
    public void whenStudentDoesNotExistWithGivenGradeLevel_findAllShouldReturnEmptyList() {
        student1 = service.save(student1);
        student2 = service.save(student2);

        StudentRequest request = new StudentRequest();
        request.setGradeLevel(4);
        List<StudentDto> studentsFromFourthGrade = service.findAll(request);

        assertThat(studentsFromFourthGrade).isEmpty();
    }

    @Test
    @DisplayName("when Student exists with given name and gradeLevel, findAll should return list of Students")
    public void whenStudentExistsWithGivenNameAndGradeLevel_findAllShouldReturnListOfStudents() {
        student1 = service.save(student1);
        student2 = service.save(student2);

        StudentRequest request1 = new StudentRequest();
        request1.setName("doe");
        request1.setGradeLevel(2);
        List<StudentDto> studentsFromSecondGradeWithNameDoe = service.findAll(request1);

        assertThat(studentsFromSecondGradeWithNameDoe).containsExactly(student1);

        StudentRequest request2 = new StudentRequest();
        request2.setName("doe");
        request2.setGradeLevel(3);
        List<StudentDto> studentsFromThirdGradeWithNameDoe = service.findAll(request2);

        assertThat(studentsFromThirdGradeWithNameDoe).containsExactly(student2);
    }

    @Test
    @DisplayName("when Student does not exist with given name and gradeLevel, findAll should return empty list")
    public void whenStudentDoesNotExistWithGivenNameAndGradeLevel_findAllShouldReturnEmptyList() {
        student1 = service.save(student1);
        student2 = service.save(student2);

        StudentRequest request2 = new StudentRequest();
        request2.setName("jane");
        request2.setGradeLevel(2);
        List<StudentDto> studentsFromSecondGradeWithNameJane = service.findAll(request2);

        assertThat(studentsFromSecondGradeWithNameJane).isEmpty();

        StudentRequest request1 = new StudentRequest();
        request1.setName("john");
        request1.setGradeLevel(3);
        List<StudentDto> studentsFromThirdGradeWithNameJohn = service.findAll(request1);

        assertThat(studentsFromThirdGradeWithNameJohn).isEmpty();
    }

}
