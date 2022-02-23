package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.service.CourseService;
import com.codecool.gradebookapi.service.StudentService;
import com.codecool.gradebookapi.service.TeacherService;
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

    @Autowired
    private TeacherService teacherService;

    private CourseInput course1;
    private CourseInput course2;
    private StudentDto student;
    private TeacherDto teacher;

    @BeforeEach
    public void setUp() {
        course1 = CourseInput.builder()
                .name("Algebra")
                .build();
        course2 = CourseInput.builder()
                .name("Biology")
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
        teacher = TeacherDto.builder()
                .firstname("Jane")
                .lastname("Doe")
                .email("janedoe@email.com")
                .address("9351 Morris St., Reisterstown, MD 21136")
                .phone("202-555-0198")
                .birthdate("1969-04-13")
                .build();
    }

    @Test
    @DisplayName("save should return saved Course")
    public void saveShouldReturnSavedCourse() {
        CourseOutput courseSaved = courseService.save(course1);

        assertThat(courseSaved.getId()).isNotNull();
        assertThat(courseSaved.getName()).isEqualTo("Algebra");
        assertThat(courseSaved.getStudents()).isEmpty();
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("findAll should return list of Courses")
    public void findAll_shouldReturnListOfCourses() {
        CourseOutput output1 = courseService.save(course1);
        CourseOutput output2 = courseService.save(course2);

        List<CourseOutput> courses = courseService.findAll();

        assertThat(courses).hasSize(2);
        assertThat(courses).containsExactly(output1, output2);
    }

    @Test
    @DisplayName("when Course with given ID exists, findById should return Course")
    public void whenCourseWithGivenIdExists_findByIdShouldReturnCourse() {
        Long id = courseService.save(course1).getId();

        Optional<CourseOutput> courseFound = courseService.findById(id);

        assertThat(courseFound).isPresent();
        assertThat(courseFound.get().getName()).isEqualTo("Algebra");
    }

    @Test
    @DisplayName("when Course with given ID does not exist, findById should return empty Optional")
    public void whenCourseWithGivenIdDoesNotExist_findByIdShouldReturnEmptyOptional() {
        Long id = courseService.save(course2).getId();

        Optional<CourseOutput> courseFound = courseService.findById(id + 1);

        assertThat(courseFound).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("deleteById should delete Course with given ID")
    public void deleteById_shouldDeleteCourseWithGivenId() {
        long id = courseService.save(course1).getId();

        courseService.deleteById(id);
        Optional<CourseOutput> courseFound = courseService.findById(id);

        assertThat(courseFound).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when Course with given ID exists, save should update existing Course")
    public void whenCourseWithGivenIdExists_saveShouldUpdateExistingCourse() {
        long id = courseService.save(course1).getId();
        course1.setName("Algebra II");
        courseService.update(id, course1);

        CourseOutput updatedCourse = courseService.findById(id).orElse(null);

        assertThat(updatedCourse).isNotNull();
        assertThat(updatedCourse.getName()).isEqualTo("Algebra II");
    }

    @Test
    @Transactional
    @DisplayName("addStudentToCourse should add Student to list of students of Course")
    public void addStudentToCourse_shouldAddStudentToListOfStudentsOfCourse() {
        student = studentService.save(student);
        CourseOutput courseSaved = courseService.save(course1);

        courseSaved = courseService.addStudentToCourse(student.getId(), courseSaved.getId());
        List<String> listOfStudents = courseSaved.getStudents();

        assertThat(listOfStudents).isEqualTo(List.of(student.getName()));
    }

    @Test
    @Transactional
    @DisplayName("setTeacherOfCourse should set Teacher field of Course")
    public void setTeacherOfCourse_shouldSetTeacherFieldOfCourse() {
        TeacherDto teacherSaved = teacherService.save(teacher);
        CourseOutput courseSaved = courseService.save(course1);

        courseSaved = courseService.setTeacherOfCourse(teacherSaved.getId(), courseSaved.getId());

        assertThat(courseSaved.getTeacherId()).isEqualTo(teacherSaved.getId());
    }

    @Test
    @Transactional
    @DisplayName("when Student exists with given ID, findCoursesOfStudent should return list of Courses")
    public void whenStudentExistsWithGivenId_findCoursesOfStudentShouldReturnListOfCourses() {
        student = studentService.save(student);
        CourseOutput course1Saved = courseService.save(course1);
        CourseOutput course2Saved = courseService.save(course2);
        course1Saved = courseService.addStudentToCourse(student.getId(), course1Saved.getId());
        course2Saved = courseService.addStudentToCourse(student.getId(), course2Saved.getId());

        List<CourseOutput> listOfCourses = courseService.findCoursesOfStudent(student);

        assertThat(listOfCourses).containsExactly(course1Saved, course2Saved);
    }

    @Test
    @Transactional
    @DisplayName("when Student is enrolled in given Course, isStudentInCourse should return true")
    public void whenStudentIsEnrolledInGivenCourse_isStudentInCourseShouldReturnTrue() {
        long studentId = studentService.save(student).getId();
        long courseId = courseService.save(course1).getId();
        courseService.addStudentToCourse(studentId, courseId);

        assertTrue(courseService.isStudentInCourse(studentId, courseId));
    }

    @Test
    @DisplayName("when Student is not enrolled in given Course, isStudentInCourse should return false")
    public void whenStudentIsNotEnrolledInGivenCourse_isStudentInCourseShouldReturnFalse() {
        long studentId = studentService.save(student).getId();
        long courseId = courseService.save(course1).getId();

        assertFalse(courseService.isStudentInCourse(studentId, courseId));
    }

}
