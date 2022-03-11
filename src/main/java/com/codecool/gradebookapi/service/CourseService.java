package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.mapper.CourseMapper;
import com.codecool.gradebookapi.dto.mapper.StudentMapper;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
import com.codecool.gradebookapi.model.Course;
import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.model.Teacher;
import com.codecool.gradebookapi.repository.CourseRepository;
import com.codecool.gradebookapi.repository.StudentRepository;
import com.codecool.gradebookapi.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private StudentMapper studentMapper;

    public List<CourseOutput> findAll() {
        return courseMapper.mapAll(courseRepository.findAll());
    }

    public CourseOutput save(CourseInput courseInput) {
        Course course = Course.builder()
                .name(courseInput.getName())
                .students(new HashSet<>())
                .build();

        Course courseCreated = courseRepository.save(course);

        return courseMapper.map(courseCreated);
    }

    public CourseOutput update(Long id, CourseInput courseInput) {
        Course clazz = Course.builder()
                .id(id)
                .name(courseInput.getName())
                .students(courseRepository.getById(id).getStudents())
                .build();

        Course courseUpdated = courseRepository.save(clazz);

        return courseMapper.map(courseUpdated);
    }

    public Optional<CourseOutput> findById(Long id) {
        return courseRepository.findById(id).map(clazz -> courseMapper.map(clazz));
    }

    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    public CourseOutput setTeacherOfCourse(Long teacherId, Long courseId) {
        Teacher teacher = teacherRepository.getById(teacherId);
        Course course = courseRepository.getById(courseId);
        course.setTeacher(teacher);

        return courseMapper.map(courseRepository.save(course));
    }

    public CourseOutput addStudentToCourse(Long studentId, Long classId) {
        Student student = studentRepository.getById(studentId);
        Course clazz = courseRepository.getById(classId);
        clazz.addStudent(student);

        return courseMapper.map(courseRepository.save(clazz));
    }

    public boolean isStudentInCourse(Long studentId, Long classId) {
        Student student = studentRepository.getById(studentId);

        return courseRepository.findCoursesByStudentsContainingAndId(student, classId).isPresent();
    }

    public List<StudentDto> getStudentsOfCourse(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

        return studentMapper.mapAll(course.getStudents());
    }
}
