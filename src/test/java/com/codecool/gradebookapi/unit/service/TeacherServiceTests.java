package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

@SpringBootTest

public class TeacherServiceTests {

    @Autowired
    private TeacherService service;

    private TeacherDto teacher1;
    private TeacherDto teacher2;

    @BeforeEach
    public void setUp() {
        teacher1 = TeacherDto.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate("1960-12-01")
                .build();

        teacher2 = TeacherDto.builder()
                .firstname("Jane")
                .lastname("Doe")
                .email("janedoe@email.com")
                .address("9351 Morris St., Reisterstown, MD 21136")
                .phone("202-555-0198")
                .birthdate("1962-04-13")
                .build();
    }

    @Test
    @DisplayName("save should return saved Teacher")
    public void saveShouldReturnSavedTeacher() {
        TeacherDto teacherSaved = service.save(teacher1);

        assertThat(teacherSaved.getName()).isEqualTo("John Doe");
        assertThat(teacherSaved.getEmail()).isEqualTo("johndoe@email.com");
        assertThat(teacherSaved.getAddress()).isEqualTo("666 Armstrong St., Mesa, AZ 85203");
        assertThat(teacherSaved.getPhone()).isEqualTo("202-555-0198");
        assertThat(teacherSaved.getBirthdate()).isEqualTo("1960-12-01");
    }

    @Test
    @DisplayName("findAll should return list of Teachers")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void findAll_shouldReturnListOfTeachers() {
        teacher1 = service.save(teacher1);
        teacher2 = service.save(teacher2);

        List<TeacherDto> actualListOfTeachers = service.findAll();

        assertThat(actualListOfTeachers).containsExactly(teacher1, teacher2);
    }

    @Test
    @DisplayName("when Teacher with given ID exists, findById should return Teacher")
    public void whenTeacherWithGivenIdExists_findByIdShouldReturnTeacher() {
        teacher1 = service.save(teacher1);

        Optional<TeacherDto> teacherFound = service.findById(teacher1.getId());

        assertThat(teacherFound).isPresent();
        assertThat(teacherFound.get()).isEqualTo(teacher1);
    }

    @Test
    @DisplayName("when Teacher with given ID does not exist, findById should return empty Optional")
    public void whenTeacherWithGivenIdDoesNotExist_findByIdShouldReturnEmptyOptional() {
        Long id = service.save(teacher2).getId();

        Optional<TeacherDto> teacherFound = service.findById(id + 1);

        assertThat(teacherFound).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("deleteById should delete Teacher with given ID")
    public void deleteById_shouldDeleteTeacherWithGivenId() {
        long id = service.save(teacher1).getId();

        service.deleteById(id);
        Optional<TeacherDto> teacherFound = service.findById(id);

        assertThat(teacherFound).isEmpty();
    }

    @Test
    @DisplayName("when Teacher with given ID already exists, save should update existing Teacher")
    public void whenTeacherWithGivenIdAlreadyExists_saveShouldUpdateExistingTeacher() {
        TeacherDto teacher = service.save(teacher1);

        teacher.setFirstname("Johnathan");
        service.save(teacher);
        Optional<TeacherDto> updatedTeacher = service.findById(teacher.getId());

        assertThat(updatedTeacher).isPresent();
        assertThat(updatedTeacher.get().getFirstname()).isEqualTo("Johnathan");
    }
}

