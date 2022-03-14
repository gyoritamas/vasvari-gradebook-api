package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.mapper.CourseMapper;
import com.codecool.gradebookapi.dto.mapper.StudentMapper;
import com.codecool.gradebookapi.model.Course;
import com.codecool.gradebookapi.model.Student;
import com.codecool.gradebookapi.repository.CourseRepository;
import com.codecool.gradebookapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;

    public List<StudentDto> findAll() {
        return studentMapper.mapAll(studentRepository.findAll());
    }

    public StudentDto save(StudentDto studentDto) {
        Student studentToSave = studentMapper.map(studentDto);
        Student saved = studentRepository.save(studentToSave);

        return studentMapper.map(saved);
    }

    public Optional<StudentDto> findById(Long id) {
        return studentRepository.findById(id).map(studentMapper::map);
    }

    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    public List<CourseOutput> findCoursesOfStudent(StudentDto studentDto) {
        Student student = studentMapper.map(studentDto);
        List<Course> courses = courseRepository.findCoursesByStudentsContaining(student);

        return courseMapper.mapAll(courses);
    }

}
