package org.vasvari.gradebookapi.unit.service;

import org.vasvari.gradebookapi.dto.TeacherDto;
import org.vasvari.gradebookapi.model.request.TeacherRequest;
import org.vasvari.gradebookapi.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
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
                .firstname("Darrell")
                .lastname("Bowen")
                .email("darrellbowen@email.com")
                .address("3982 Turnpike Drive, Birmingham, AL 35203")
                .phone("619-446-8496")
                .birthdate(LocalDate.of(1984, 2, 1))
                .build();

        teacher2 = TeacherDto.builder()
                .firstname("Lilian")
                .lastname("Stafford")
                .email("lilianstafford@email.com")
                .address("4498 Sugar Camp Road, Vernon Center, MN 56090")
                .phone("507-549-1665")
                .birthdate(LocalDate.of(1985, 4, 13))
                .build();
    }

    @Test
    @DisplayName("save should return saved Teacher")
    public void saveShouldReturnSavedTeacher() {
        TeacherDto teacherSaved = service.save(teacher1);

        assertThat(teacherSaved.getName()).isEqualTo(teacher1.getName());
        assertThat(teacherSaved.getEmail()).isEqualTo(teacher1.getEmail());
        assertThat(teacherSaved.getAddress()).isEqualTo(teacher1.getAddress());
        assertThat(teacherSaved.getPhone()).isEqualTo(teacher1.getPhone());
        assertThat(teacherSaved.getBirthdate()).isEqualTo(teacher1.getBirthdate());
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
        TeacherDto teacher = service.save(teacher2);

        teacher.setLastname("Bowen");
        service.save(teacher);
        Optional<TeacherDto> updatedTeacher = service.findById(teacher.getId());

        assertThat(updatedTeacher).isPresent();
        assertThat(updatedTeacher.get().getLastname()).isEqualTo("Bowen");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("when Teacher exists with given criteria, findTeachers should return list of Teachers")
    public void whenTeacherExistsWithGivenCriteria_findTeachersShouldReturnListOfTeachers() {
        TeacherDto darrelBowen = service.save(teacher1);
        TeacherDto lilianStafford = service.save(teacher2);

        TeacherRequest findLilian = new TeacherRequest();
        findLilian.setName("lilian");

        List<TeacherDto> teachersWithNameLilian = service.findTeachers(findLilian);

        assertThat(teachersWithNameLilian).containsExactly(lilianStafford);
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("when Teacher does not exist with given criteria, findTeachers should return empty list")
    public void whenTeacherDoesNotExistWithGivenCriteria_findTeachersShouldReturnEmptyList() {
        TeacherDto darrelBowen = service.save(teacher1);

        TeacherRequest findLilian = new TeacherRequest();
        findLilian.setName("lilian");

        List<TeacherDto> teachersWithNameLilian = service.findTeachers(findLilian);

        assertThat(teachersWithNameLilian).isEmpty();
    }
}

