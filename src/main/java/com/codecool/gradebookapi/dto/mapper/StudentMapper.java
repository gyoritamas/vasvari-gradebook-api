package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.model.Student;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudentMapper {
    public Student map(StudentDto studentDto) {
        return Student.builder()
                .id(studentDto.getId())
                .firstname(studentDto.getFirstname())
                .lastname(studentDto.getLastname())
                .gradeLevel(studentDto.getGradeLevel())
                .email(studentDto.getEmail())
                .address(studentDto.getAddress())
                .phone(studentDto.getPhone())
                .birthdate(studentDto.getBirthdate())
                .build();
    }

    public StudentDto map(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .firstname(student.getFirstname())
                .lastname(student.getLastname())
                .gradeLevel(student.getGradeLevel())
                .email(student.getEmail())
                .address(student.getAddress())
                .phone(student.getPhone())
                .birthdate(student.getBirthdate())
                .build();
    }

    public List<StudentDto> mapAll(Collection<Student> students){
        return students.stream()
                .map(this::map)
                .sorted(Comparator.comparing(StudentDto::getId))
                .collect(Collectors.toList());
    }
}
