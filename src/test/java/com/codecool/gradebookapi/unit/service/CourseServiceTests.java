package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private TeacherDto teacher1;
    private TeacherDto teacher2;

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
                .gradeLevel(11)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate("2004-02-01")
                .build();
        teacher1 = TeacherDto.builder()
                .firstname("Darrell")
                .lastname("Bowen")
                .email("darrellbowen@email.com")
                .address("3982 Turnpike Drive, Birmingham, AL 35203")
                .phone("619-446-8496")
                .birthdate("1984-02-01")
                .build();
        teacher2 = TeacherDto.builder()
                .firstname("Lilian")
                .lastname("Stafford")
                .email("lilianstafford@email.com")
                .address("4498 Sugar Camp Road, Vernon Center, MN 56090")
                .phone("507-549-1665")
                .birthdate("1985-04-13")
                .build();
    }

    @Test
    @Transactional
    @DisplayName("save should return saved Course")
    public void saveShouldReturnSavedCourse() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        CourseOutput courseSaved = courseService.save(course1);

        assertThat(courseSaved.getId()).isNotNull();
        assertThat(courseSaved.getName()).isEqualTo("Algebra");
        assertThat(courseSaved.getStudents()).isEmpty();
    }

    @Test
    @Transactional
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("findAll should return list of Courses")
    public void findAll_shouldReturnListOfCourses() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        course2.setTeacherId(teacherId);
        CourseOutput output1 = courseService.save(course1);
        CourseOutput output2 = courseService.save(course2);

        List<CourseOutput> courses = courseService.findAll();

        assertThat(courses).hasSize(2);
        assertThat(courses).containsExactly(output1, output2);
    }

    @Test
    @Transactional
    @DisplayName("when Course with given ID exists, findById should return Course")
    public void whenCourseWithGivenIdExists_findByIdShouldReturnCourse() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        Long id = courseService.save(course1).getId();

        Optional<CourseOutput> courseFound = courseService.findById(id);

        assertThat(courseFound).isPresent();
        assertThat(courseFound.get().getName()).isEqualTo("Algebra");
    }

    @Test
    @Transactional
    @DisplayName("when Course with given ID does not exist, findById should return empty Optional")
    public void whenCourseWithGivenIdDoesNotExist_findByIdShouldReturnEmptyOptional() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        course2.setTeacherId(teacherId);
        Long id = courseService.save(course2).getId();

        Optional<CourseOutput> courseFound = courseService.findById(id + 1);

        assertThat(courseFound).isEqualTo(Optional.empty());
    }

    @Test
    @Transactional
    @DisplayName("deleteById should delete Course with given ID")
    public void deleteById_shouldDeleteCourseWithGivenId() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        long id = courseService.save(course1).getId();

        courseService.deleteById(id);
        Optional<CourseOutput> courseFound = courseService.findById(id);

        assertThat(courseFound).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when Course with given ID exists, save should update existing Course")
    public void whenCourseWithGivenIdExists_saveShouldUpdateExistingCourse() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        long id = courseService.save(course1).getId();
        course1.setName("Algebra II");
        CourseOutput updatedCourse = courseService.update(id, course1);

        assertThat(updatedCourse).isNotNull();
        assertThat(updatedCourse.getName()).isEqualTo("Algebra II");
    }

    @Test
    @Transactional
    @DisplayName("addStudentToCourse should add Student to list of students of Course")
    public void addStudentToCourse_shouldAddStudentToListOfStudentsOfCourse() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        student = studentService.save(student);
        CourseOutput courseSaved = courseService.save(course1);

        courseSaved = courseService.addStudentToCourse(student.getId(), courseSaved.getId());
        List<SimpleData> listOfStudents = courseSaved.getStudents();
        SimpleData simpleStudentData = new SimpleData(student.getId(), student.getName());

        assertThat(listOfStudents).containsExactly(simpleStudentData);
    }

    @Test
    @Transactional
    @DisplayName("when Student exists with given ID, findCoursesOfStudent should return list of Courses")
    public void whenStudentExistsWithGivenId_findCoursesOfStudentShouldReturnListOfCourses() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        course2.setTeacherId(teacherId);
        student = studentService.save(student);
        CourseOutput course1Saved = courseService.save(course1);
        CourseOutput course2Saved = courseService.save(course2);
        course1Saved = courseService.addStudentToCourse(student.getId(), course1Saved.getId());
        course2Saved = courseService.addStudentToCourse(student.getId(), course2Saved.getId());

        List<CourseOutput> listOfCourses = studentService.findCoursesOfStudent(student);

        assertThat(listOfCourses).containsExactly(course1Saved, course2Saved);
    }

    @Test
    @Transactional
    @DisplayName("when Student is enrolled in given Course, isStudentInCourse should return true")
    public void whenStudentIsEnrolledInGivenCourse_isStudentInCourseShouldReturnTrue() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        long studentId = studentService.save(student).getId();
        long courseId = courseService.save(course1).getId();
        courseService.addStudentToCourse(studentId, courseId);

        assertTrue(courseService.isStudentInCourse(studentId, courseId));
    }

    @Test
    @Transactional
    @DisplayName("when Student is not enrolled in given Course, isStudentInCourse should return false")
    public void whenStudentIsNotEnrolledInGivenCourse_isStudentInCourseShouldReturnFalse() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        long studentId = studentService.save(student).getId();
        long courseId = courseService.save(course1).getId();

        assertFalse(courseService.isStudentInCourse(studentId, courseId));
    }

    @Test
    @Transactional
    @DisplayName("given Course exists with ID, getStudentOfCourse should return list of Students")
    public void givenCourseExistsWithId_getStudentsOfCourse_shouldReturnListOfStudents() {
        long teacherId = teacherService.save(teacher1).getId();
        course1.setTeacherId(teacherId);
        StudentDto studentSaved = studentService.save(student);
        CourseOutput courseSaved = courseService.save(course1);
        courseService.addStudentToCourse(studentSaved.getId(), courseSaved.getId());

        List<StudentDto> studentsOfCourse = courseService.getStudentsOfCourse(courseSaved.getId());

        assertThat(studentsOfCourse).containsExactly(studentSaved);
    }

    @Test
    @DisplayName("given Course does not exist with ID, getStudentsOfCourse should throw exception")
    public void givenCourseDoesNotExistWithId_getStudentsOfCourse_shouldThrowException() {
        assertThatThrownBy(() -> courseService.getStudentsOfCourse(99L))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessage(CourseNotFoundException.ERROR_MESSAGE, 99L);
    }

    @Test
    @DisplayName("given Teacher is not set as teacher of any Courses, findCoursesOfTeacher should return empty list")
    public void givenTeacherIsNotSetAsTeacherOfAnyCourses_findCoursesOfTeacher_shouldReturnListOfCourses() {
        TeacherDto teacherSaved = teacherService.save(teacher1);
        List<CourseOutput> coursesOfTeacher = courseService.findCoursesOfTeacher(teacherSaved);

        assertThat(coursesOfTeacher).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("given Teacher is set as teacher of Courses, findCoursesOfTeacher should return list of Courses")
    public void givenTeacherIsSetAsTeacherOfCourses_findCoursesOfTeacher_shouldReturnListOfCourses() {
        teacher1 = teacherService.save(teacher1);
        course1.setTeacherId(teacher1.getId());
        course2.setTeacherId(teacher1.getId());
        CourseOutput course1Saved = courseService.save(course1);
        CourseOutput course2Saved = courseService.save(course2);

        List<CourseOutput> coursesOfTeacher = courseService.findCoursesOfTeacher(teacher1);

        assertThat(coursesOfTeacher).containsExactly(course1Saved, course2Saved);
    }

    @Test
    @Transactional
    @DisplayName("findStudentsOfTeacher")
    public void findStudentsOfTeacher() {
        // save students
        StudentDto johnDoe = StudentDto.builder().firstname("John").lastname("Doe").gradeLevel(11).email("johndoe@gmail.com").address("").phone("").birthdate("2005-01-01").build();
        StudentDto janeDoe = StudentDto.builder().firstname("Jane").lastname("Doe").gradeLevel(11).email("janedoe@gmail.com").address("").phone("").birthdate("2004-11-21").build();
        StudentDto jimDoe = StudentDto.builder().firstname("Jim").lastname("Doe").gradeLevel(11).email("jimdoe@gmail.com").address("").phone("").birthdate("2005-03-09").build();
        StudentDto jackDoe = StudentDto.builder().firstname("Jack").lastname("Doe").gradeLevel(11).email("jackdoe@gmail.com").address("").phone("").birthdate("2005-02-27").build();
        johnDoe = studentService.save(johnDoe);
        janeDoe = studentService.save(janeDoe);
        jimDoe = studentService.save(jimDoe);
        jackDoe = studentService.save(jackDoe);

        // save teacher
        TeacherDto darrelBowen = teacherService.save(teacher1);
        TeacherDto lilianStafford = teacherService.save(teacher2);

        course1.setTeacherId(darrelBowen.getId());
        course2.setTeacherId(lilianStafford.getId());

        // save courses
        CourseOutput algebra = courseService.save(course1);
        CourseOutput biology = courseService.save(course2);
        CourseOutput physics = courseService.save(
                CourseInput.builder().name("Physics").teacherId(darrelBowen.getId()).build()
        );

        // add students to courses
        algebra = courseService.addStudentToCourse(johnDoe.getId(), algebra.getId());
        algebra = courseService.addStudentToCourse(janeDoe.getId(), algebra.getId());
        physics = courseService.addStudentToCourse(jimDoe.getId(), physics.getId());
        physics = courseService.addStudentToCourse(johnDoe.getId(), physics.getId());
        biology = courseService.addStudentToCourse(jackDoe.getId(), biology.getId());

        List<StudentDto> studentsOfTeacher = courseService.findStudentsOfTeacher(darrelBowen);

        assertThat(studentsOfTeacher).containsExactlyInAnyOrder(johnDoe, janeDoe, jimDoe);
    }
}
