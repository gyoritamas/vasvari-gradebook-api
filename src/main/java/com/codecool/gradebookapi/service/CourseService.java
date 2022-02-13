package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.mapper.CourseMapper;
import com.codecool.gradebookapi.dto.mapper.StudentMapper;
import com.codecool.gradebookapi.model.Course;
import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.repository.CourseRepository;
import com.codecool.gradebookapi.repository.StudentRepository;
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
    private CourseMapper courseMapper;

    @Autowired
    private StudentMapper studentMapper;

    public List<CourseOutput> findAll() {
        return courseMapper.mapAll(courseRepository.findAll());
    }

    public CourseOutput save(CourseInput courseInput) {
        Course course = Course.builder()
                .name(courseInput.getCourse())
                .students(new HashSet<>())
                .build();

        Course courseCreated = courseRepository.save(course);

        return courseMapper.map(courseCreated);
    }

    public CourseOutput update(Long id, CourseInput courseInput) {
        Course clazz = Course.builder()
                .id(id)
                .name(courseInput.getCourse())
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

    public CourseOutput addStudentToClass(Long studentId, Long classId) {
        Student student = studentRepository.getById(studentId);
        Course clazz = courseRepository.getById(classId);
        clazz.addStudent(student);

        return courseMapper.map(courseRepository.save(clazz));
    }

    public List<CourseOutput> findClassesOfStudent(StudentDto studentDto) {
        Student student = studentMapper.map(studentDto);
        List<Course> courses = courseRepository.findClassByStudentsContaining(student);

        return courseMapper.mapAll(courses);
    }

    public boolean isStudentInClass(Long studentId, Long classId) {
        Student student = studentRepository.getById(studentId);

        return courseRepository.findClassByStudentsContainingAndId(student, classId).isPresent();
    }
}
