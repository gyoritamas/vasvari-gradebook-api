package com.codecool.gradebookapi.unit.controller;

import com.codecool.gradebookapi.controller.AssignmentController;
import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.SubjectOutput;
import com.codecool.gradebookapi.dto.assembler.AssignmentModelAssembler;
import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import com.codecool.gradebookapi.dto.dataTypes.SimpleTeacher;
import com.codecool.gradebookapi.jwt.JwtAuthenticationEntryPoint;
import com.codecool.gradebookapi.jwt.JwtTokenUtil;
import com.codecool.gradebookapi.model.AssignmentType;
import com.codecool.gradebookapi.security.PasswordConfig;
import com.codecool.gradebookapi.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssignmentController.class)
@Import({AssignmentModelAssembler.class, PasswordConfig.class, JwtAuthenticationEntryPoint.class})
public class AssignmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssignmentService assignmentService;
    @MockBean
    private GradebookService gradebookService;
    @MockBean
    private SubjectService subjectService;
    @MockBean
    private TeacherService teacherService;
    @MockBean
    private StudentService studentService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper mapper;

    private AssignmentInput assignmentInput1;
    private AssignmentInput assignmentInput2;
    private AssignmentOutput assignmentOutput1;
    private AssignmentOutput assignmentOutput2;

    @BeforeEach
    public void setUp() {
        SubjectOutput subject = SubjectOutput.builder()
                .id(1L)
                .name("Algebra")
                .teacher(new SimpleTeacher(1L, "Darrell", "Bowen"))
                .students(new ArrayList<>())
                .build();

        when(subjectService.findById(1L)).thenReturn(Optional.of(subject));

        assignmentInput1 = AssignmentInput.builder()
                .name("Homework 1")
                .type(AssignmentType.HOMEWORK)
                .description("Read chapters 1 to 5")
                .deadline(LocalDate.of(2051, 1, 1))
                .subjectId(1L)
                .build();
        assignmentInput2 = AssignmentInput.builder()
                .name("Homework 2")
                .type(AssignmentType.HOMEWORK)
                .description("Read chapters 6 to 9")
                .deadline(LocalDate.of(2052, 1, 1))
                .subjectId(1L)
                .build();

        assignmentOutput1 = AssignmentOutput.builder()
                .id(1L)
                .name("Homework 1")
                .type(AssignmentType.HOMEWORK)
                .description("Read chapters 1 to 5")
                .deadline(LocalDate.of(2051, 1, 1))
                .build();
        assignmentOutput2 = AssignmentOutput.builder()
                .id(2L)
                .name("Homework 2")
                .type(AssignmentType.HOMEWORK)
                .description("Read Chapters 6 and 9.")
                .deadline(LocalDate.of(2052, 1, 1))
                .build();
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
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
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
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
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
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
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Assignment with given ID does not exist, getById should return response 'Not Found'")
    public void whenAssignmentWithGivenIdDoesNotExist_getByIdShouldReturnResponseNotFound() throws Exception {
        when(assignmentService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/assignments/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given AssignmentInput parameters are valid, add should return created assignment")
    public void givenAssignmentInputParametersAreValid_addShouldReturnCreatedAssignment() throws Exception {
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
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given AssignmentInput has invalid parameters, add should return response 'Bad Request")
    public void givenAssignmentInputHasInvalidParameters_addShouldReturnResponseBadRequest() throws Exception {
        givenAssignmentWithEmptyName_addAssignment_shouldReturnWithBadRequest();
        givenAssignmentWithWrongType_addAssignment_shouldReturnWithBadRequest();
    }

    private void givenAssignmentWithEmptyName_addAssignment_shouldReturnWithBadRequest() throws Exception {
        AssignmentInput inputWithEmptyName = AssignmentInput.builder()
                .name(" ")
                .type(AssignmentType.TEST)
                .deadline(LocalDate.of(2051, 1, 1))
                .subjectId(1L)
                .build();
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
        AssignmentInput inputWithWrongType = AssignmentInput.builder()
                .name("Test II")
                .type(AssignmentType.valueOf("TEST"))
                .deadline(LocalDate.of(2051, 1, 1))
                .subjectId(1L)
                .build();
        String inputAsString = mapper.writeValueAsString(inputWithWrongType)
                .replaceAll("\"type\":\"(\\w+)\"", "\"type\":\"WRONG_TYPE\"");

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
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
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
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
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
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given AssignmentInput has invalid parameter, update should return response 'Bad Request'")
    public void givenAssignmentInputHasInvalidParameter_updateShouldReturnResponseBadRequest() throws Exception {
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignmentOutput1));

        givenAssignmentWithEmptyName_updateAssignment_shouldReturnWithBadRequest();
        givenAssignmentWithWrongType_updateAssignment_shouldReturnWithBadRequest();
    }

    private void givenAssignmentWithEmptyName_updateAssignment_shouldReturnWithBadRequest() throws Exception {
        AssignmentInput inputWithEmptyName = AssignmentInput.builder()
                .name(" ")
                .type(AssignmentType.TEST)
                .deadline(LocalDate.of(2051, 1, 1))
                .subjectId(1L)
                .build();
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
        AssignmentInput inputWithWrongType = AssignmentInput.builder()
                .name("Final test")
                .type(AssignmentType.HOMEWORK)
                .deadline(LocalDate.of(2051, 1, 1))
                .subjectId(1L)
                .build();
        String inputAsString = mapper.writeValueAsString(inputWithWrongType)
                .replaceAll("\"type\":\"(\\w+)\"", "\"type\":\"WRONG_TYPE\"");

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
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Assignment exists with given ID, delete should return response 'No Content'")
    public void whenAssignmentExistsWithGivenId_deleteShouldReturnResponseNoContent() throws Exception {
        when(assignmentService.findById(2L)).thenReturn(Optional.of(assignmentOutput2));

        this.mockMvc
                .perform(delete("/api/assignments/2"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Assignment does not exist with given ID, delete should return response 'Not Found'")
    public void whenAssignmentDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() throws Exception {
        when(assignmentService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/assignments/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Assignment is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
    public void whenAssignmentIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() throws Exception {
        GradebookOutput entry = GradebookOutput.builder()
                .assignment(new SimpleData(assignmentOutput1.getId(), assignmentOutput1.getName()))
                .build();
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignmentOutput1));
        when(gradebookService.findByAssignmentId(1L)).thenReturn(List.of(entry));

        this.mockMvc
                .perform(delete("/api/assignments/1"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }
}
