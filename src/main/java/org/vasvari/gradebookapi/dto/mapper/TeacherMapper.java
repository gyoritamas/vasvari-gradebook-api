package org.vasvari.gradebookapi.dto.mapper;

import org.vasvari.gradebookapi.dto.TeacherDto;
import org.vasvari.gradebookapi.model.Teacher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeacherMapper {
    public Teacher map(TeacherDto teacherDto) {
        return Teacher.builder()
                .id(teacherDto.getId())
                .firstname(teacherDto.getFirstname())
                .lastname(teacherDto.getLastname())
                .email(teacherDto.getEmail())
                .address(teacherDto.getAddress())
                .phone(teacherDto.getPhone())
                .birthdate(teacherDto.getBirthdate())
                .build();
    }

    public TeacherDto map(Teacher teacher) {
        return TeacherDto.builder()
                .id(teacher.getId())
                .firstname(teacher.getFirstname())
                .lastname(teacher.getLastname())
                .email(teacher.getEmail())
                .address(teacher.getAddress())
                .phone(teacher.getPhone())
                .birthdate(teacher.getBirthdate())
                .build();
    }

    public List<TeacherDto> mapAll(List<Teacher> teachers) {
        return teachers.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

}
