package com.codecool.gradebookapi.unit.controller;

import com.codecool.gradebookapi.controller.AssignmentController;
import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.assembler.AssignmentModelAssembler;
import com.codecool.gradebookapi.model.AssignmentType;
import com.codecool.gradebookapi.service.AssignmentService;
import com.codecool.gradebookapi.service.GradebookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssignmentController.class)
@Import(AssignmentModelAssembler.class)
public class AssignmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssignmentService assignmentService;

    @MockBean
    private GradebookService gradebookService;

    private static ObjectMapper mapper;

    private AssignmentInput assignmentInput1;
    private AssignmentInput assignmentInput2;
    private AssignmentOutput assignmentOutput1;
    private AssignmentOutput assignmentOutput2;

    public AssignmentControllerTests() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

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

        assignmentOutput1 = AssignmentOutput.builder()
                .id(1L)
                .name("Homework 1")
                .type(AssignmentType.HOMEWORK)
                .description("Read chapters 1 to 5")
                .createdAt(ZonedDateTime.now())
                .build();
        assignmentOutput2 = AssignmentOutput.builder()
                .id(2L)
                .name("Homework 2")
                .type(AssignmentType.HOMEWORK)
                .description("Read Chapters 6 and 9.")
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Test
    @DisplayName("given empty database, getAll should return empty list")
    public void givenEmptyDatabase_getAllShouldReturnEmptyList() throws Exception {
        when(assignmentService.findAll()).thenReturn(List.of());

        this.mockMvc
                .perform(get("/api/assignments"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.assignments").doesNotExist());
    }

    @Test
    @DisplayName("when Assignments posted, getAll should return list of Assignments")
    public void whenAssignmentsPosted_getAllShouldReturnListOfAssignments() throws Exception {
        when(assignmentService.findAll()).thenReturn(List.of(assignmentOutput1, assignmentOutput2));

        this.mockMvc
                .perform(get("/api/assignments"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.assignments", hasSize(2)))
                .andExpect(jsonPath("$._embedded.assignments[0].name", is("Homework 1")))
                .andExpect(jsonPath("$._embedded.assignments[1].name", is("Homework 2")));
    }

    @Test
    @DisplayName("when Assignment with given ID exists, getById should return Assignment")
    public void whenAssignmentWithGivenIdExists_getByIdShouldReturnAssignment() throws Exception {
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignmentOutput1));

        String assignmentAsString = mapper.writeValueAsString(assignmentOutput1);

        this.mockMvc
                .perform(get("/api/assignments/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(assignmentAsString));
    }

    @Test
    @DisplayName("when Assignment with given ID does not exist, getById should return response 'Not Found'")
    public void whenAssignmentWithGivenIdDoesNotExist_getByIdShouldReturnResponseNotFound() throws Exception {
        when(assignmentService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/assignments/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("given AssignmentInput parameters are valid, add should return created assignment")
    public void givenClassInputParametersAreValid_addShouldReturnCreatedClass() throws Exception {
        when(assignmentService.save(assignmentInput1)).thenReturn(assignmentOutput1);

        String inputAsString = mapper.writeValueAsString(assignmentInput1);
        String outputAsString = mapper.writeValueAsString(assignmentOutput1);

        this.mockMvc
                .perform(
                        post("/api/assignments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(outputAsString));
    }

    @Test
    @DisplayName("given AssignmentInput has invalid parameters, add should return response 'Bad Request")
    public void givenAssignmentInputHasInvalidParameters_addShouldReturnResponseBadRequest() throws Exception {
        givenAssignmentWithEmptyName_addAssignment_shouldReturnWithBadRequest();
        givenAssignmentWithWrongType_addAssignment_shouldReturnWithBadRequest();
    }

    private void givenAssignmentWithEmptyName_addAssignment_shouldReturnWithBadRequest() throws Exception {
        AssignmentInput inputWithEmptyName = AssignmentInput.builder().name(" ").type("TEST").build();
        String inputAsString = mapper.writeValueAsString(inputWithEmptyName);

        this.mockMvc
                .perform(
                        post("/api/assignments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private void givenAssignmentWithWrongType_addAssignment_shouldReturnWithBadRequest() throws Exception {
        AssignmentInput inputWithWrongType = AssignmentInput.builder().name("Test II").type("BAD_TYPE").build();
        String inputAsString = mapper.writeValueAsString(inputWithWrongType);

        this.mockMvc
                .perform(
                        post("/api/assignments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("when Assignment does not exist with given ID, update should return response 'Not Found'")
    public void whenAssignmentDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() throws Exception {
        when(assignmentService.findById(99L)).thenReturn(Optional.empty());

        String inputAsString = mapper.writeValueAsString(assignmentInput1);

        this.mockMvc
                .perform(
                        put("/api/assignments/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when Assignment exists with given ID, update should return updated Assignment")
    public void whenAssignmentExistsWithGivenId_updateShouldReturnUpdatedAssignment() throws Exception {
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignmentOutput1));
        when(assignmentService.update(1L, assignmentInput1)).thenReturn(assignmentOutput1);

        String inputAsString = mapper.writeValueAsString(assignmentInput1);
        String outputAsString = mapper.writeValueAsString(assignmentOutput1);

        this.mockMvc
                .perform(
                        put("/api/assignments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(outputAsString));
    }

    @Test
    @DisplayName("given AssignmentInput has invalid parameter, update should return response 'Bad Request'")
    public void givenAssignmentInputHasInvalidParameter_updateShouldReturnResponseBadRequest() throws Exception {
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignmentOutput1));

        givenAssignmentWithEmptyName_updateAssignment_shouldReturnWithBadRequest();
        givenAssignmentWithWrongType_updateAssignment_shouldReturnWithBadRequest();
    }

    private void givenAssignmentWithEmptyName_updateAssignment_shouldReturnWithBadRequest() throws Exception {
        AssignmentInput inputWithEmptyName = AssignmentInput.builder().name(" ").type("TEST").build();
        String inputAsString = mapper.writeValueAsString(inputWithEmptyName);

        this.mockMvc
                .perform(
                        put("/api/assignments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private void givenAssignmentWithWrongType_updateAssignment_shouldReturnWithBadRequest() throws Exception {
        AssignmentInput inputWithWrongType = AssignmentInput.builder().name("Final test").type("BAD_TYPE").build();
        String inputAsString = mapper.writeValueAsString(inputWithWrongType);

        this.mockMvc
                .perform(
                        put("/api/assignments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("when Assignment exists with given ID, delete should return response 'No Content'")
    public void whenAssignmentExistsWithGivenId_deleteShouldReturnResponseNoContent() throws Exception {
        when(assignmentService.findById(2L)).thenReturn(Optional.of(assignmentOutput2));

        this.mockMvc
                .perform(delete("/api/assignments/2"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("when Assignment does not exist with given ID, delete should return response 'Not Found'")
    public void whenAssignmentDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() throws Exception {
        when(assignmentService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/assignments/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when Assignment is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
    public void whenAssignmentIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() throws Exception {
        GradebookOutput entry = GradebookOutput.builder().assignmentId(assignmentOutput1.getId()).build();
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignmentOutput1));
        when(gradebookService.findByAssignmentId(1L)).thenReturn(List.of(entry));

        this.mockMvc
                .perform(delete("/api/assignments/1"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }
}
