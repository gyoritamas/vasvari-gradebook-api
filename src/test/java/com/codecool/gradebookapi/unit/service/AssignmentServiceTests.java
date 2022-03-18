package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.model.AssignmentType;
import com.codecool.gradebookapi.service.AssignmentService;
import com.codecool.gradebookapi.service.CourseService;
import com.codecool.gradebookapi.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

@SpringBootTest
public class AssignmentServiceTests {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CourseService courseService;

    private AssignmentInput assignmentInput1;
    private AssignmentInput assignmentInput2;

    @BeforeEach
    public void setUp() {
        TeacherDto teacher = TeacherDto.builder()
                .firstname("Darrell")
                .lastname("Bowen")
                .email("darrellbowen@email.com")
                .address("3982 Turnpike Drive, Birmingham, AL 35203")
                .phone("619-446-8496")
                .birthdate("1984-02-01")
                .build();

        long teacherId = teacherService.save(teacher).getId();

        CourseInput course = CourseInput.builder()
                .name("Algebra")
                .teacherId(teacherId)
                .build();

        long courseId = courseService.save(course).getId();

        assignmentInput1 = AssignmentInput.builder()
                .name("Homework 1")
                .type(AssignmentType.HOMEWORK)
                .description("Read chapters 1 to 5")
                .deadline(LocalDate.of(2051, 1, 1))
                .courseId(courseId)
                .build();
        assignmentInput2 = AssignmentInput.builder()
                .name("Homework 2")
                .type(AssignmentType.HOMEWORK)
                .description("Read chapters 6 to 9")
                .deadline(LocalDate.of(2052, 1, 1))
                .courseId(courseId)
                .build();
    }

    @Test
    @Transactional
    @DisplayName("save should return saved Assignment")
    public void saveShouldReturnAssignment() {
        AssignmentOutput assignmentSaved = assignmentService.save(assignmentInput1);

        assertThat(assignmentSaved.getId()).isNotNull();
        assertThat(assignmentSaved.getName()).isEqualTo("Homework 1");
        assertThat(assignmentSaved.getType()).isEqualTo(AssignmentType.HOMEWORK);
        assertThat(assignmentSaved.getDescription()).isEqualTo("Read chapters 1 to 5");
        assertThat(assignmentSaved.getDeadline()).isNotNull();
    }

    @Test
    @Transactional
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("findAll should return list of Assignments")
    public void findAll_shouldReturnListOfAssignments() {
        assignmentService.save(assignmentInput1);
        assignmentService.save(assignmentInput2);

        List<AssignmentOutput> actualListOfAssignments = assignmentService.findAll();

        assertThat(actualListOfAssignments).hasSize(2);
        assertThat(actualListOfAssignments.get(0).getName()).isEqualTo("Homework 1");
        assertThat(actualListOfAssignments.get(1).getName()).isEqualTo("Homework 2");
    }

    @Test
    @Transactional
    @DisplayName("when Assignment with given ID exists, findById should return Assignment")
    public void whenAssignmentWithGivenIdExists_findByIdShouldReturnAssignment() {
        AssignmentOutput saved = assignmentService.save(assignmentInput1);

        Optional<AssignmentOutput> assignmentFound = assignmentService.findById(saved.getId());

        assertThat(assignmentFound).isPresent();
        assertThat(assignmentFound.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @Transactional
    @DisplayName("when Assignment with given ID does not exist, findById should return empty Optional")
    public void whenAssignmentWithGivenIdDoesNotExist_findByIdShouldReturnEmptyOptional() {
        Long id = assignmentService.save(assignmentInput2).getId();

        Optional<AssignmentOutput> assignmentFound = assignmentService.findById(id + 1);

        assertThat(assignmentFound).isEqualTo(Optional.empty());
    }

    @Test
    @Transactional
    @DisplayName("deleteById should delete Assignment with given ID")
    public void deleteById_shouldDeleteAssignmentWithGivenId() {
        long id = assignmentService.save(assignmentInput1).getId();

        assignmentService.deleteById(id);
        Optional<AssignmentOutput> assignmentFound = assignmentService.findById(id);

        assertThat(assignmentFound).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when Assignment with given ID already exists, save should update existing Assignment")
    public void whenAssignmentWithGivenIdAlreadyExists_saveShouldUpdateExistingAssignment() {
        long id = assignmentService.save(assignmentInput1).getId();
        AssignmentInput update = assignmentInput1;
        update.setName("Updated name");
        assignmentService.update(id, update);

        AssignmentOutput updatedAssignment = assignmentService.findById(id).orElse(null);

        assertThat(updatedAssignment).isNotNull();
        assertThat(updatedAssignment.getName()).isEqualTo("Updated name");
    }

}
