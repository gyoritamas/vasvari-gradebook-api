package org.vasvari.gradebookapi.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.vasvari.gradebookapi.controller.GradebookController;
import org.vasvari.gradebookapi.dto.*;
import org.vasvari.gradebookapi.dto.assembler.GradebookModelAssembler;
import org.vasvari.gradebookapi.dto.simpleTypes.SimpleData;
import org.vasvari.gradebookapi.dto.simpleTypes.SimpleStudent;
import org.vasvari.gradebookapi.exception.DuplicateEntryException;
import org.vasvari.gradebookapi.jwt.JwtAuthenticationEntryPoint;
import org.vasvari.gradebookapi.jwt.JwtTokenUtil;
import org.vasvari.gradebookapi.security.PasswordConfig;
import org.vasvari.gradebookapi.service.*;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GradebookController.class)
@Import({GradebookModelAssembler.class, PasswordConfig.class, JwtAuthenticationEntryPoint.class})
public class GradebookControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GradebookService gradebookService;
    @MockBean
    private StudentService studentService;
    @MockBean
    private SubjectService subjectService;
    @MockBean
    private TeacherService teacherService;
    @MockBean
    private AssignmentService assignmentService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    private static ObjectMapper mapper;

    private StudentDto student1;
    private StudentDto student2;
    private SubjectOutput subject;
    private AssignmentOutput assignment;
    private GradebookInput entry1;
    private GradebookInput entry2;
    private GradebookOutput savedEntry1;
    private GradebookOutput savedEntry2;

    public GradebookControllerTests() {
        mapper = new ObjectMapper();
    }

    @BeforeEach
    public void setUp() {
        student1 = StudentDto.builder()
                .firstname("John")
                .lastname("Doe")
                .build();
        student2 = StudentDto.builder()
                .firstname("Jane")
                .lastname("Doe")
                .build();
        subject = SubjectOutput.builder()
                .name("Algebra")
                .build();
        assignment = AssignmentOutput.builder()
                .name("Homework 1")
                .build();
        entry1 = GradebookInput.builder()
                .studentId(1L)
                .subjectId(1L)
                .assignmentId(1L)
                .grade(4)
                .build();
        entry2 = GradebookInput.builder()
                .studentId(2L)
                .subjectId(1L)
                .assignmentId(1L)
                .grade(5)
                .build();
        SimpleStudent simpleStudent1 = SimpleStudent.builder()
                .id(1L)
                .firstname(student1.getFirstname())
                .lastname(student1.getLastname())
                .build();
        SimpleStudent simpleStudent2 = SimpleStudent.builder()
                .id(2L)
                .firstname(student2.getFirstname())
                .lastname(student2.getLastname())
                .build();
        savedEntry1 = GradebookOutput.builder()
                .id(1L)
                .student(simpleStudent1)
                .subject(new SimpleData(1L, "Algebra"))
                .assignment(new SimpleData(1L, "Homework 1"))
                .grade(4)
                .build();
        savedEntry2 = GradebookOutput.builder()
                .id(1L)
                .student(simpleStudent2)
                .subject(new SimpleData(1L, "Algebra"))
                .assignment(new SimpleData(1L, "Homework 1"))
                .grade(5)
                .build();
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given empty database, getAll should return empty list")
    public void givenEmptyDatabase_getAllShouldReturnEmptyList() throws Exception {
        when(gradebookService.findAll()).thenReturn(List.of());

        this.mockMvc
                .perform(get("/api/gradebook"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.entries").doesNotExist());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when entries posted, getAll should return list of entries")
    public void whenEntriesPosted_getAllShouldReturnListOfEntries() throws Exception {
        when(gradebookService.findAll()).thenReturn(List.of(savedEntry1, savedEntry2));

        this.mockMvc
                .perform(get("/api/gradebook"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.entries", hasSize(2)))
                .andExpect(jsonPath("$._embedded.entries[0].student.id", is(1)))
                .andExpect(jsonPath("$._embedded.entries[1].student.id", is(2)));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when entry with given ID exists, getById should return entry")
    public void whenEntryWithGivenIdExists_getByIdShouldReturnEntry() throws Exception {
        when(gradebookService.findById(1L)).thenReturn(Optional.of(savedEntry1));

        this.mockMvc
                .perform(get("/api/gradebook/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student.id", is(1)))
                .andExpect(jsonPath("$.subject.id", is(1)))
                .andExpect(jsonPath("$.assignment.id", is(1)))
                .andExpect(jsonPath("$.grade", is(4)));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when entry with given ID does not exist, getById should return response 'Not Found'")
    public void whenEntryWithGivenIdDoesNotExist_getByIdShouldReturnResponseNotFound() throws Exception {
        when(gradebookService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/gradebook/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student does not exist with given ID, gradeAssignment should return response 'Not Found'")
    public void whenStudentDoesNotExistWithGivenId_gradeAssignmentShouldReturnResponseNotFound() throws Exception {
        when(studentService.findById(99L)).thenReturn(Optional.empty());
        when(subjectService.findById(1L)).thenReturn(Optional.of(subject));
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignment));

        GradebookInput entry = GradebookInput.builder().studentId(99L).subjectId(1L).assignmentId(1L).grade(4).build();
        String entryAsString = mapper.writeValueAsString(entry);

        this.mockMvc
                .perform(
                        post("/api/gradebook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(entryAsString)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Subject does not exist with given ID, gradeAssignment should return response 'Not Found'")
    public void whenSubjectDoesNotExistWithGivenId_gradeAssignmentShouldReturnResponseNotFound() throws Exception {
        when(studentService.findById(1L)).thenReturn(Optional.of(student1));
        when(subjectService.findById(99L)).thenReturn(Optional.empty());
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignment));

        GradebookInput entry = GradebookInput.builder().studentId(1L).subjectId(99L).assignmentId(1L).grade(5).build();
        String entryAsString = mapper.writeValueAsString(entry);

        this.mockMvc
                .perform(
                        post("/api/gradebook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(entryAsString)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Assignment does not exist with given ID, gradeAssignment should return response 'Not Found'")
    public void whenAssignmentDoesNotExistWithGivenId_gradeAssignmentShouldReturnResponseNotFound() throws Exception {
        when(studentService.findById(1L)).thenReturn(Optional.of(student1));
        when(subjectService.findById(1L)).thenReturn(Optional.of(subject));
        when(assignmentService.findById(99L)).thenReturn(Optional.empty());

        GradebookInput entry = GradebookInput.builder().studentId(1L).subjectId(1L).assignmentId(99L).grade(4).build();
        String entryAsString = mapper.writeValueAsString(entry);

        this.mockMvc
                .perform(
                        post("/api/gradebook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(entryAsString)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when an entry exists with the given IDs, gradeAssignment should return response 'Conflict'")
    public void whenAnEntryExistsWithTheGivenIds_gradeAssignmentShouldReturnResponseConflict() throws Exception {
        when(studentService.findById(2L)).thenReturn(Optional.of(student2));
        when(subjectService.findById(1L)).thenReturn(Optional.of(subject));
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignment));
        when(subjectService.isStudentAddedToSubject(2L, 1L)).thenReturn(true);
        when(gradebookService.save(entry2)).thenThrow(DuplicateEntryException.class);

        String entry2AsString = mapper.writeValueAsString(entry2);

        this.mockMvc
                .perform(
                        post("/api/gradebook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(entry2AsString)
                )
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student not enrolled in given Subject, gradeAssignment should return response 'Bad Request'")
    public void whenStudentNotEnrolledInGivenSubject_gradeAssignmentShouldReturnResponseBadRequest() throws Exception {
        when(studentService.findById(2L)).thenReturn(Optional.of(student2));
        when(subjectService.findById(1L)).thenReturn(Optional.of(subject));
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignment));
        when(subjectService.isStudentAddedToSubject(2L, 1L)).thenReturn(false);

        String entry2AsString = mapper.writeValueAsString(entry2);

        this.mockMvc
                .perform(
                        post("/api/gradebook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(entry2AsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/invalid_entry_data.csv", numLinesToSkip = 1, delimiter = ';')
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when GradebookInput has invalid parameters, gradeAssignment should return response 'Bad Request'")
    public void whenGradebookInputHasInvalidParameters_gradeAssignmentShouldReturnResponseBadRequest(
            @AggregateWith(GradebookControllerTests.GradebookInputAggregator.class) GradebookInput input) throws Exception {

        String inputAsString = mapper.writeValueAsString(input);

        this.mockMvc
                .perform(
                        post("/api/gradebook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when entities found with given IDs, gradeAssignment should return created GradebookEntry")
    public void whenEntitiesFoundWithGivenIds_gradeAssignmentShouldReturnCreatedGradebookEntry() throws Exception {
        when(studentService.findById(1L)).thenReturn(Optional.of(student1));
        when(subjectService.findById(1L)).thenReturn(Optional.of(subject));
        when(assignmentService.findById(1L)).thenReturn(Optional.of(assignment));
        when(gradebookService.save(entry1)).thenReturn(savedEntry1);
        when(subjectService.isStudentAddedToSubject(1L, 1L)).thenReturn(true);

        String entry1AsString = mapper.writeValueAsString(entry1);

        this.mockMvc
                .perform(
                        post("/api/gradebook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(entry1AsString)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.student.id", is(1)))
                .andExpect(jsonPath("$.subject.id", is(1)))
                .andExpect(jsonPath("$.assignment.id", is(1)))
                .andExpect(jsonPath("$.grade", is(4)));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when User exists with given ID, delete should return response 'No Content'")
    public void whenUserExistsWithGivenId_delete_shouldReturnResponseNoContent() throws Exception {
        when(gradebookService.findById(1L)).thenReturn(Optional.of(savedEntry1));
        doNothing().when(gradebookService).deleteById(1L);

        this.mockMvc
                .perform(
                        delete("/api/gradebook/1")
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(gradebookService).deleteById(1L);
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when User does not exist with given ID, delete should return response 'Not Found'")
    public void whenUserDoesNotExistWithGivenId_delete_shouldReturnResponseNotFound() throws Exception {
        when(gradebookService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(
                        delete("/api/gradebook/99")
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(gradebookService, times(0)).deleteById(99L);
    }

    private static class GradebookInputAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
                throws ArgumentsAggregationException {
            return GradebookInput.builder()
                    .studentId(accessor.getLong(0))
                    .subjectId(accessor.getLong(1))
                    .assignmentId(accessor.getLong(2))
                    .grade(accessor.getInteger(3))
                    .build();
        }
    }
}
