package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import com.codecool.gradebookapi.model.AssignmentType;
import com.codecool.gradebookapi.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

@SpringBootTest
public class AssignmentServiceTests {

    @Autowired
    private AssignmentService service;

    private AssignmentInput assignmentInput1;
    private AssignmentInput assignmentInput2;

    @BeforeEach
    public void setUp() {
        assignmentInput1 = AssignmentInput.builder()
                .name("Homework 1")
                .type("HOMEWORK")
                .description("Read chapters 1 to 5")
                .build();
        assignmentInput2 = AssignmentInput.builder()
                .name("Homework 2")
                .type("HOMEWORK")
                .description("Read chapters 6 to 9")
                .build();
    }

    @Test
    @DisplayName("save should return saved Assignment")
    public void saveShouldReturnAssignment() {
        AssignmentOutput assignmentSaved = service.save(assignmentInput1);

        assertThat(assignmentSaved.getId()).isNotNull();
        assertThat(assignmentSaved.getName()).isEqualTo("Homework 1");
        assertThat(assignmentSaved.getType()).isEqualTo(AssignmentType.HOMEWORK);
        assertThat(assignmentSaved.getDescription()).isEqualTo("Read chapters 1 to 5");
        assertThat(assignmentSaved.getCreatedAt()).isNotNull();
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("findAll should return list of Assignments")
    public void findAll_shouldReturnListOfAssignments() {
        service.save(assignmentInput1);
        service.save(assignmentInput2);

        List<AssignmentOutput> actualListOfAssignments = service.findAll();

        assertThat(actualListOfAssignments).hasSize(2);
        assertThat(actualListOfAssignments.get(0).getName()).isEqualTo("Homework 1");
        assertThat(actualListOfAssignments.get(1).getName()).isEqualTo("Homework 2");
    }

    @Test
    @DisplayName("when Assignment with given ID exists, findById should return Assignment")
    public void whenAssignmentWithGivenIdExists_findByIdShouldReturnAssignment() {
        AssignmentOutput saved = service.save(assignmentInput1);

        Optional<AssignmentOutput> assignmentFound = service.findById(saved.getId());

        assertThat(assignmentFound).isPresent();
        assertThat(assignmentFound.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("when Assignment with given ID does not exist, findById should return empty Optional")
    public void whenAssignmentWithGivenIdDoesNotExist_findByIdShouldReturnEmptyOptional() {
        Long id = service.save(assignmentInput2).getId();

        Optional<AssignmentOutput> assignmentFound = service.findById(id + 1);

        assertThat(assignmentFound).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("deleteById should delete Assignment with given ID")
    public void deleteById_shouldDeleteAssignmentWithGivenId() {
        long id = service.save(assignmentInput1).getId();

        service.deleteById(id);
        Optional<AssignmentOutput> assignmentFound = service.findById(id);

        assertThat(assignmentFound).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when Assignment with given ID already exists, save should update existing Assignment")
    public void whenAssignmentWithGivenIdAlreadyExists_saveShouldUpdateExistingAssignment() {
        long id = service.save(assignmentInput1).getId();
        AssignmentInput update = AssignmentInput.builder().name("Updated name").type("HOMEWORK").build();
        service.update(id, update);

        AssignmentOutput updatedAssignment = service.findById(id).orElse(null);

        assertThat(updatedAssignment).isNotNull();
        assertThat(updatedAssignment.getName()).isEqualTo("Updated name");
    }

}
