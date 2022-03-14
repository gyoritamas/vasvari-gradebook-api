package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.mapper.CourseMapper;
import com.codecool.gradebookapi.dto.mapper.StudentMapper;
import com.codecool.gradebookapi.dto.mapper.TeacherMapper;
import com.codecool.gradebookapi.model.Course;
import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.model.Teacher;
import com.codecool.gradebookapi.repository.CourseRepository;
import com.codecool.gradebookapi.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;

//    @Autowired
//    public TeacherService(TeacherRepository teacherRepository,
//                          CourseRepository courseRepository,
//                          TeacherMapper teacherMapper,
//                          StudentMapper studentMapper,
//                          CourseMapper courseMapper) {
//        this.teacherRepository = teacherRepository;
//        this.courseRepository = courseRepository;
//        this.teacherMapper = teacherMapper;
//        this.studentMapper = studentMapper;
//        this.courseMapper = courseMapper;
//    }

    public List<TeacherDto> findAll() {
        return teacherMapper.mapAll(teacherRepository.findAll());
    }

    public TeacherDto save(TeacherDto teacherDto) {
        Teacher teacherToSave = teacherMapper.map(teacherDto);
        Teacher saved = teacherRepository.save(teacherToSave);

        return teacherMapper.map(saved);
    }

    public Optional<TeacherDto> findById(Long id) {
        return teacherRepository.findById(id).map(teacherMapper::map);
    }

    public void deleteById(Long id) {
        teacherRepository.deleteById(id);
    }

//    public List<CourseOutput> findCoursesOfTeacher(TeacherDto teacherDto) {
//        Teacher teacher = teacherMapper.map(teacherDto);
//        List<Course> courses = courseRepository.findCoursesByTeacher(teacher);
//
//        return courseMapper.mapAll(courses);
//    }
//
//    public List<StudentDto> findStudentsOfTeacher(TeacherDto teacherDto) {
//        Teacher teacher = teacherMapper.map(teacherDto);
//        List<Course> courses = courseRepository.findCoursesByTeacher(teacher);
//        Set<Student> studentsSet = new HashSet<>();
//        for (Course course : courses) {
//            studentsSet.addAll(course.getStudents());
//        }
//        List<Student> studentsList = studentsSet.stream()
//                .sorted(Comparator.comparing(Student::getId))
//                .collect(Collectors.toList());
//
//        return studentMapper.mapAll(studentsList);
//    }
}
