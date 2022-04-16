package com.codecool.gradebookapi.unit.controller;

import com.codecool.gradebookapi.controller.StudentController;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.SubjectOutput;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.assembler.StudentModelAssembler;
import com.codecool.gradebookapi.dto.assembler.SubjectModelAssembler;
import com.codecool.gradebookapi.dto.dataTypes.SimpleStudent;
import com.codecool.gradebookapi.dto.dataTypes.SimpleTeacher;
import com.codecool.gradebookapi.jwt.JwtAuthenticationEntryPoint;
import com.codecool.gradebookapi.jwt.JwtTokenUtil;
import com.codecool.gradebookapi.model.request.GradebookRequest;
import com.codecool.gradebookapi.security.PasswordConfig;
import com.codecool.gradebookapi.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

@WebMvcTest(StudentController.class)
@Import({StudentModelAssembler.class, SubjectModelAssembler.class, PasswordConfig.class, JwtAuthenticationEntryPoint.class})
public class StudentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @MockBean
    private SubjectService subjectService;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private GradebookService gradebookService;

    private static ObjectMapper mapper;

    private StudentDto student1;
    private StudentDto student2;

    public StudentControllerTests() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @BeforeEach
    public void setUp() {
        student1 = StudentDto.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .gradeLevel(9)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate(LocalDate.of(2005, 12, 1))
                .build();

        student2 = StudentDto.builder()
                .id(2L)
                .firstname("Jane")
                .lastname("Doe")
                .gradeLevel(11)
                .email("janedoe@email.com")
                .address("9351 Morris St., Reisterstown, MD 21136")
                .phone("202-555-0198")
                .birthdate(LocalDate.of(2007, 4, 13))
                .build();
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given empty database, getAll should return empty list")
    public void givenEmptyDatabase_getAllShouldReturnEmptyList() throws Exception {
        when(studentService.findAll()).thenReturn(List.of());

        this.mockMvc
                .perform(get("/api/students"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.students").doesNotExist());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Students posted, getAll should return list of Students")
    public void whenStudentsPosted_getAllShouldReturnListOfStudents() throws Exception {
        when(studentService.findAll()).thenReturn(List.of(student1, student2));

        this.mockMvc
                .perform(get("/api/students"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.students", hasSize(2)))
                .andExpect(jsonPath("$._embedded.students[0].firstname", is("John")))
                .andExpect(jsonPath("$._embedded.students[1].firstname", is("Jane")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student with given ID exists, getById should return Student")
    public void whenStudentWithGivenIdExists_getByIdShouldReturnStudent() throws Exception {
        when(studentService.findById(1L)).thenReturn(Optional.of(student1));

        this.mockMvc
                .perform(get("/api/students/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("John")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student with given ID does not exist, getById should return response 'Not Found'")
    public void whenStudentWithGivenIdDoesNotExist_getByIdShouldReturnResponseNotFound() throws Exception {
        when(studentService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/students/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given valid Student parameters, add should return create Student")
    public void givenValidStudentParameters_addShouldReturnCreatedStudent() throws Exception {
        when(studentService.save(student2)).thenReturn(student2);

        String student2AsString = mapper.writeValueAsString(student2);

        this.mockMvc
                .perform(
                        post("/api/students")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(student2AsString)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(student2AsString));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/invalid_student_data.csv", numLinesToSkip = 1, delimiter = ';')
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student has invalid parameters, add should return response 'Bad Request'")
    public void whenStudentHasInvalidParameters_addShouldReturnResponseBadRequest(
            @AggregateWith(StudentAggregator.class) StudentDto student) throws Exception {

        String studentAsString = mapper.writeValueAsString(student);

        this.mockMvc
                .perform(
                        post("/api/students")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(studentAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student does not exist with given ID, update should return response 'Not Found'")
    public void whenStudentDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() throws Exception {
        when(studentService.findById(99L)).thenReturn(Optional.empty());
        String student1AsString = mapper.writeValueAsString(student1);

        this.mockMvc
                .perform(
                        put("/api/students/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(student1AsString)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/invalid_student_data.csv", numLinesToSkip = 1, delimiter = ';')
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student has invalid parameters, update should return response 'Bad Request'")
    public void whenStudentHasInvalidParameters_updateShouldReturnResponseBadRequest(
            @AggregateWith(StudentAggregator.class) StudentDto student) throws Exception {
        when(studentService.findById(1L)).thenReturn(Optional.of(student1));
        String studentAsString = mapper.writeValueAsString(student);

        this.mockMvc
                .perform(
                        put("/api/students/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(studentAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student exists with given ID, update should return updated Student")
    public void whenStudentExistsWithGivenId_updateShouldReturnUpdatedStudent() throws Exception {
        when(studentService.findById(1L)).thenReturn(Optional.of(student1));
        when(studentService.save(student1)).thenReturn(student1);

        student1.setId(1L);
        String student1AsString = mapper.writeValueAsString(student1);

        this.mockMvc
                .perform(
                        put("/api/students/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(student1AsString)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(student1AsString));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student does not exist with given ID, delete should return response 'Not Found'")
    public void whenStudentDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() throws Exception {
        when(studentService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/students/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student exists with given ID, delete should return response 'No Content'")
    public void whenStudentExistsWithGivenId_deleteShouldReturnResponseNoContent() throws Exception {
        when(studentService.findById(1L)).thenReturn(Optional.of(student1));

        this.mockMvc
                .perform(delete("/api/students/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student does not exist with given ID, getSubjectsOfStudent should return response 'Not Found'")
    public void whenStudentDoesNotExistWithGivenId_getSubjectsOfStudentShouldReturnResponseNotFound() throws Exception {
        when(studentService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/students/99/subjects"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
    public void whenStudentIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() throws Exception {
        SimpleStudent simpleStudent = SimpleStudent.builder()
                .id(student1.getId())
                .firstname(student1.getFirstname())
                .lastname(student1.getLastname())
                .build();
        GradebookOutput savedEntry = GradebookOutput.builder()
                .student(simpleStudent)
                .build();
        when(studentService.findById(1L)).thenReturn(Optional.of(student1));
        GradebookRequest request = GradebookRequest.builder().studentId(1L).build();
        when(gradebookService.findGradebookEntries(request)).thenReturn(List.of(savedEntry));

        this.mockMvc
                .perform(delete("/api/students/1"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student exists with given ID, getSubjectsOfStudent should return list of Subjects")
    public void whenStudentExistsWithGivenId_getSubjectsOfStudentShouldReturnListOfSubjects() throws Exception {
        SimpleStudent simpleStudent1 = new SimpleStudent(student1.getId(), student1.getFirstname(), student1.getLastname());
        SimpleStudent simpleStudent2 = new SimpleStudent(student2.getId(), student2.getFirstname(), student2.getLastname());
        TeacherDto teacher = TeacherDto.builder().id(1L).firstname("Darrell").lastname("Bowen").build();
        SimpleTeacher simpleTeacher = new SimpleTeacher(1L, "Darrell", "Bowen");
        SubjectOutput subject1 = SubjectOutput.builder()
                .name("Biology")
                .teacher(simpleTeacher)
                .students(List.of(simpleStudent1))
                .build();
        SubjectOutput subject2 = SubjectOutput.builder()
                .name("Social science")
                .teacher(simpleTeacher)
                .students(List.of(simpleStudent1, simpleStudent2))
                .build();
        List<SubjectOutput> subjectsOfStudent1 = List.of(subject1, subject2);

        when(studentService.findById(1L)).thenReturn(Optional.of(student1));
        when(teacherService.findById(1L)).thenReturn(Optional.of(teacher));
        when(studentService.findSubjectsOfStudent(student1)).thenReturn(subjectsOfStudent1);

        this.mockMvc
                .perform(get("/api/students/1/subjects"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.subjects", hasSize(2)))
                .andExpect(jsonPath("$._embedded.subjects[0].name", is("Biology")))
                .andExpect(jsonPath("$._embedded.subjects[1].name", is("Social science")));
    }

    private static class StudentAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
                throws ArgumentsAggregationException {
            return StudentDto.builder()
                    .firstname(accessor.getString(0))
                    .lastname(accessor.getString(1))
                    .gradeLevel(accessor.getInteger(2))
                    .email(accessor.getString(3))
                    .address(accessor.getString(4))
                    .phone(accessor.getString(5))
                    .birthdate(LocalDate.parse(accessor.getString(6)))
                    .build();
        }
    }
}
