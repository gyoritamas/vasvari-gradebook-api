package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.service.CourseService;
import com.codecool.gradebookapi.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

@SpringBootTest
public class CourseServiceTests {

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    private CourseInput class1;
    private CourseInput class2;
    private StudentDto student;

    @BeforeEach
    public void setUp() {
        class1 = CourseInput.builder()
                .course("Algebra")
                .build();
        class2 = CourseInput.builder()
                .course("Biology")
                .build();
        student = StudentDto.builder()
                .firstname("John")
                .lastname("Doe")
                .gradeLevel(2)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate("1990-12-01")
                .build();
    }

    @Test
    @DisplayName("save should return saved Class")
    public void saveShouldReturnSavedClass() {
        CourseOutput classSaved = courseService.save(class1);

        assertThat(classSaved.getId()).isNotNull();
        assertThat(classSaved.getCourse()).isEqualTo("Algebra");
        assertThat(classSaved.getStudents()).isEmpty();
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("findAll should return list of Classes")
    public void findAll_shouldReturnListOfClasses() {
        CourseOutput output1 = courseService.save(class1);
        CourseOutput output2 = courseService.save(class2);

        List<CourseOutput> classes = courseService.findAll();

        assertThat(classes).hasSize(2);
        assertThat(classes).containsExactly(output1, output2);
    }

    @Test
    @DisplayName("when Class with given ID exists, findById should return Class")
    public void whenClassWithGivenIdExists_findByIdShouldReturnClass() {
        Long id = courseService.save(class1).getId();

        Optional<CourseOutput> classFound = courseService.findById(id);

        assertThat(classFound).isPresent();
        assertThat(classFound.get().getCourse()).isEqualTo("Algebra");
    }

    @Test
    @DisplayName("when Class with given ID does not exist, findById should return empty Optional")
    public void whenClassWithGivenIdDoesNotExist_findByIdShouldReturnEmptyOptional() {
        Long id = courseService.save(class2).getId();

        Optional<CourseOutput> classFound = courseService.findById(id + 1);

        assertThat(classFound).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("deleteById should delete Class with given ID")
    public void deleteById_shouldDeleteClassWithGivenId() {
        long id = courseService.save(class1).getId();

        courseService.deleteById(id);
        Optional<CourseOutput> classFound = courseService.findById(id);

        assertThat(classFound).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when Class with given ID exists, save should update existing Class")
    public void whenClassWithGivenIdExists_saveShouldUpdateExistingClass() {
        long id = courseService.save(class1).getId();
        class1.setCourse("Algebra II");
        courseService.update(id, class1);

        CourseOutput updatedClass = courseService.findById(id).orElse(null);

        assertThat(updatedClass).isNotNull();
        assertThat(updatedClass.getCourse()).isEqualTo("Algebra II");
    }

    @Test
    @Transactional
    @DisplayName("when Student exists with given ID, findClassesOfStudent should return list of Classes")
    public void whenStudentExistsWithGivenId_findClassesOfStudentShouldReturnListOfClasses() {
        student = studentService.save(student);
        CourseOutput class1Saved = courseService.save(class1);
        CourseOutput class2Saved = courseService.save(class2);
        class1Saved = courseService.addStudentToClass(student.getId(), class1Saved.getId());
        class2Saved = courseService.addStudentToClass(student.getId(), class2Saved.getId());

        List<CourseOutput> listOfClasses = courseService.findClassesOfStudent(student);

        assertThat(listOfClasses).containsExactly(class1Saved, class2Saved);
    }

    @Test
    @Transactional
    @DisplayName("when Student is enrolled in given Class, isStudentInClass should return true")
    public void whenStudentIsEnrolledInGivenClass_isStudentInClassShouldReturnTrue() {
        long studentId = studentService.save(student).getId();
        long classId = courseService.save(class1).getId();
        courseService.addStudentToClass(studentId, classId);

        assertTrue(courseService.isStudentInClass(studentId, classId));
    }

    @Test
    @DisplayName("when Student is not enrolled in given Class, isStudentInClass should return false")
    public void whenStudentIsNotEnrolledInGivenClass_isStudentInClassShouldReturnFalse() {
        long studentId = studentService.save(student).getId();
        long classId = courseService.save(class1).getId();

        assertFalse(courseService.isStudentInClass(studentId, classId));
    }

}
