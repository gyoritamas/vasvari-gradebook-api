package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.mapper.CourseMapper;
import com.codecool.gradebookapi.dto.mapper.StudentMapper;
import com.codecool.gradebookapi.dto.mapper.TeacherMapper;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
import com.codecool.gradebookapi.model.Course;
import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.model.Teacher;
import com.codecool.gradebookapi.repository.CourseRepository;
import com.codecool.gradebookapi.repository.StudentRepository;
import com.codecool.gradebookapi.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final CourseMapper courseMapper;

    public List<CourseOutput> findAll() {
        return courseMapper.mapAll(courseRepository.findAll());
    }

    public CourseOutput save(CourseInput courseInput) {
        Course course = Course.builder()
                .name(courseInput.getName())
                .teacher(
                        teacherRepository.getById(courseInput.getTeacherId())
                )
                .students(new HashSet<>())
                .build();

        Course courseCreated = courseRepository.save(course);

        return courseMapper.map(courseCreated);
    }

    public CourseOutput update(Long id, CourseInput courseInput) {
        Course course = Course.builder()
                .id(id)
                .name(courseInput.getName())
                .teacher(
                        teacherRepository.getById(courseInput.getTeacherId())
                )
                .students(courseRepository.getById(id).getStudents())
                .build();

        Course courseUpdated = courseRepository.save(course);

        return courseMapper.map(courseUpdated);
    }

    public Optional<CourseOutput> findById(Long id) {
        return courseRepository.findById(id).map(courseMapper::map);
    }

    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    public CourseOutput addStudentToCourse(Long studentId, Long courseId) {
        Student student = studentRepository.getById(studentId);
        Course course = courseRepository.getById(courseId);
        course.addStudent(student);

        return courseMapper.map(courseRepository.save(course));
    }

    public boolean isStudentInCourse(Long studentId, Long courseId) {
        Student student = studentRepository.getById(studentId);

        return courseRepository.findCoursesByStudentsContainingAndId(student, courseId).isPresent();
    }

    public List<StudentDto> getStudentsOfCourse(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

        return studentMapper.mapAll(course.getStudents());
    }

    public List<CourseOutput> findCoursesOfTeacher(TeacherDto teacherDto) {
        return courseMapper.mapAll(findCoursesByTeacher(teacherDto));
    }

    public List<StudentDto> findStudentsOfTeacher(TeacherDto teacherDto) {
        List<Course> courses = findCoursesByTeacher(teacherDto);
        Set<Student> studentsSet = new HashSet<>();
        for (Course course : courses) {
            studentsSet.addAll(course.getStudents());
        }
        List<Student> studentsList = studentsSet.stream()
                .sorted(Comparator.comparing(Student::getId))
                .collect(Collectors.toList());

        return studentMapper.mapAll(studentsList);
    }

    private List<Course> findCoursesByTeacher(TeacherDto teacherDto) {
        Teacher teacher = teacherMapper.map(teacherDto);
        return courseRepository.findCoursesByTeacher(teacher);
    }
}
