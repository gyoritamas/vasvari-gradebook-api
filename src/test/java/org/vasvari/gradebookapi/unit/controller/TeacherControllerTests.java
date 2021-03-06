package org.vasvari.gradebookapi.unit.controller;

import org.vasvari.gradebookapi.controller.TeacherController;
import org.vasvari.gradebookapi.dto.TeacherDto;
import org.vasvari.gradebookapi.dto.assembler.TeacherModelAssembler;
import org.vasvari.gradebookapi.jwt.JwtAuthenticationEntryPoint;
import org.vasvari.gradebookapi.jwt.JwtTokenUtil;
import org.vasvari.gradebookapi.security.PasswordConfig;
import org.vasvari.gradebookapi.service.TeacherService;
import org.vasvari.gradebookapi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherController.class)
@Import({TeacherModelAssembler.class, PasswordConfig.class, JwtAuthenticationEntryPoint.class})
public class TeacherControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeacherService service;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    private static ObjectMapper mapper;

    private TeacherDto teacher1;
    private TeacherDto teacher2;

    @BeforeAll
    public static void init() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @BeforeEach
    public void setUp() {
        teacher1 = TeacherDto.builder()
                .id(1L)
                .firstname("Darrell")
                .lastname("Bowen")
                .email("darrellbowen@email.com")
                .address("3982 Turnpike Drive, Birmingham, AL 35203")
                .phone("619-446-8496")
                .birthdate(LocalDate.of(1984, 2, 1))
                .build();

        teacher2 = TeacherDto.builder()
                .id(2L)
                .firstname("Lilian")
                .lastname("Stafford")
                .email("lilianstafford@email.com")
                .address("4498 Sugar Camp Road, Vernon Center, MN 56090")
                .phone("507-549-1665")
                .birthdate(LocalDate.of(1985, 4, 13))
                .build();
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given empty database, getAll should return empty list")
    public void givenEmptyDatabase_getAllShouldReturnEmptyList() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        this.mockMvc
                .perform(get("/api/teachers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.teachers").doesNotExist());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Teachers posted, getAll should return list of Teachers")
    public void whenTeachersPosted_getAllShouldReturnListOfTeachers() throws Exception {
        when(service.findAll()).thenReturn(List.of(teacher1, teacher2));

        this.mockMvc
                .perform(get("/api/teachers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.teachers", hasSize(2)))
                .andExpect(jsonPath("$._embedded.teachers[0].firstname", is("Darrell")))
                .andExpect(jsonPath("$._embedded.teachers[1].firstname", is("Lilian")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Teacher with given ID exists, getById should return Teacher")
    public void whenTeacherWithGivenIdExists_getByIdShouldReturnTeacher() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.of(teacher1));

        this.mockMvc
                .perform(get("/api/teachers/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("Darrell")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Teacher with given ID does not exist, getById should return response 'Not Found'")
    public void whenTeacherWithGivenIdDoesNotExist_getByIdShouldReturnResponseNotFound() throws Exception {
        when(service.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/teachers/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given valid Teacher parameters, add should return created Teacher")
    public void givenValidTeacherParameters_addShouldReturnCreatedTeacher() throws Exception {
        when(service.save(teacher2)).thenReturn(teacher2);

        String teacher2AsString = mapper.writeValueAsString(teacher2);

        this.mockMvc
                .perform(
                        post("/api/teachers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(teacher2AsString)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(teacher2AsString));
    }

    @ParameterizedTest
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @CsvFileSource(resources = "/invalid_teacher_data.csv", numLinesToSkip = 1, delimiter = ';')
    @DisplayName("when Teacher has invalid parameters, add should return response 'Bad Request'")
    public void whenTeacherHasInvalidParameters_addShouldReturnResponseBadRequest(
            @AggregateWith(TeacherControllerTests.TeacherAggregator.class) TeacherDto teacher) throws Exception {

        String teacherAsString = mapper.writeValueAsString(teacher);

        this.mockMvc
                .perform(
                        post("/api/teachers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(teacherAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Teacher does not exist with given ID, update should return response 'Not Found'")
    public void whenTeacherDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() throws Exception {
        when(service.findById(99L)).thenReturn(Optional.empty());
        String teacher1AsString = mapper.writeValueAsString(teacher1);

        this.mockMvc
                .perform(
                        put("/api/teachers/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(teacher1AsString)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @CsvFileSource(resources = "/invalid_teacher_data.csv", numLinesToSkip = 1, delimiter = ';')
    @DisplayName("when Teacher has invalid parameters, update should return response 'Bad Request'")
    public void whenTeacherHasInvalidParameters_updateShouldReturnResponseBadRequest(
            @AggregateWith(TeacherControllerTests.TeacherAggregator.class) TeacherDto teacher) throws Exception {
        when(service.findById(1L)).thenReturn(Optional.of(teacher1));
        String teacherAsString = mapper.writeValueAsString(teacher);

        this.mockMvc
                .perform(
                        put("/api/teachers/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(teacherAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Teacher exists with given ID, update should return updated Teacher")
    public void whenTeacherExistsWithGivenId_updateShouldReturnUpdatedTeacher() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.of(teacher1));
        when(service.save(teacher1)).thenReturn(teacher1);

        teacher1.setId(1L);
        String teacher1AsString = mapper.writeValueAsString(teacher1);

        this.mockMvc
                .perform(
                        put("/api/teachers/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(teacher1AsString)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(teacher1AsString));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Teacher does not exist with given ID, delete should return response 'Not Found'")
    public void whenTeacherDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() throws Exception {
        when(service.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/teachers/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Teacher exists with given ID, delete should return response 'No Content'")
    public void whenTeacherExistsWithGivenId_deleteShouldReturnResponseNoContent() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.of(teacher1));

        this.mockMvc
                .perform(delete("/api/teachers/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    private static class TeacherAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
                throws ArgumentsAggregationException {
            return TeacherDto.builder()
                    .firstname(accessor.getString(0))
                    .lastname(accessor.getString(1))
                    .email(accessor.getString(2))
                    .address(accessor.getString(3))
                    .phone(accessor.getString(4))
                    .birthdate(LocalDate.parse(accessor.getString(5)))
                    .build();
        }
    }
}
