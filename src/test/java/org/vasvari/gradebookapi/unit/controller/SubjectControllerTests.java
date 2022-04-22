package org.vasvari.gradebookapi.unit.controller;

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
import org.vasvari.gradebookapi.controller.SubjectController;
import org.vasvari.gradebookapi.dto.*;
import org.vasvari.gradebookapi.dto.assembler.StudentModelAssembler;
import org.vasvari.gradebookapi.dto.assembler.SubjectModelAssembler;
import org.vasvari.gradebookapi.dto.simpleTypes.SimpleData;
import org.vasvari.gradebookapi.dto.simpleTypes.SimpleStudent;
import org.vasvari.gradebookapi.dto.simpleTypes.SimpleTeacher;
import org.vasvari.gradebookapi.jwt.JwtAuthenticationEntryPoint;
import org.vasvari.gradebookapi.jwt.JwtTokenUtil;
import org.vasvari.gradebookapi.model.request.GradebookRequest;
import org.vasvari.gradebookapi.security.PasswordConfig;
import org.vasvari.gradebookapi.service.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubjectController.class)
@Import({SubjectModelAssembler.class,
        StudentModelAssembler.class,
        JwtAuthenticationEntryPoint.class,
        PasswordConfig.class})
public class SubjectControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubjectService subjectService;

    @MockBean
    private StudentService studentService;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private GradebookService gradebookService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    private static ObjectMapper mapper;

    private SubjectInput subjectInput1;
    private SubjectInput subjectInput2;
    private SubjectOutput subjectOutput1;
    private SubjectOutput subjectOutput2;

    public SubjectControllerTests() {
        mapper = new ObjectMapper();
    }

    @BeforeEach
    public void setUp() {
        subjectInput1 = SubjectInput.builder()
                .name("Algebra")
                .teacherId(1L)
                .build();
        subjectInput2 = SubjectInput.builder()
                .name("Biology")
                .teacherId(2L)
                .build();
        subjectOutput1 = SubjectOutput.builder()
                .id(1L)
                .name("Algebra")
                .students(List.of(
                        SimpleStudent.builder().id(1L).firstname("John").lastname("Doe").build(),
                        SimpleStudent.builder().id(2L).firstname("Jane").lastname("Doe").build()
                ))
                .teacher(
                        SimpleTeacher.builder().id(1L).firstname("Darrell").lastname("Bowen").build()
                )
                .build();
        subjectOutput2 = SubjectOutput.builder()
                .id(2L)
                .name("Biology")
                .students(List.of(
                        SimpleStudent.builder().id(3L).firstname("John").lastname("Smith").build(),
                        SimpleStudent.builder().id(4L).firstname("Jane").lastname("Smith").build()
                ))
                .teacher(
                        SimpleTeacher.builder().id(2L).firstname("Lilian").lastname("Stafford").build()
                )
                .build();
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given empty database, getAll should return empty list")
    public void givenEmptyDatabase_getAllShouldReturnEmptyList() throws Exception {
        when(subjectService.findAll()).thenReturn(List.of());

        this.mockMvc
                .perform(get("/api/subjects"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.subjects").doesNotExist());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Subjects posted, getAll should return list of Subjects")
    public void whenSubjectsPosted_getAllShouldReturnListOfSubjects() throws Exception {
        when(subjectService.findAll()).thenReturn(List.of(subjectOutput1, subjectOutput2));

        this.mockMvc
                .perform(get("/api/subjects"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.subjects", hasSize(2)))
                .andExpect(jsonPath("$._embedded.subjects[0].name", is("Algebra")))
                .andExpect(jsonPath("$._embedded.subjects[1].name", is("Biology")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Subject with given ID exists, getById should return Subject")
    public void whenSubjectWithGivenIdExists_getByIdShouldReturnSubject() throws Exception {
        when(subjectService.findById(1L)).thenReturn(Optional.of(subjectOutput1));

        this.mockMvc
                .perform(get("/api/subjects/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Algebra")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Subject with given ID does not exist, getById should return response 'Not Found'")
    public void whenSubjectWithGivenIdDoesNotExist_getByIdShouldReturnResponseNotFound() throws Exception {
        when(subjectService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/subjects/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given SubjectInput parameters are valid, add should return created Subject")
    public void givenSubjectInputParametersAreValid_addShouldReturnCreatedSubject() throws Exception {
        when(subjectService.save(subjectInput1)).thenReturn(subjectOutput1);

        String inputAsString = mapper.writeValueAsString(subjectInput1);

        this.mockMvc
                .perform(
                        post("/api/subjects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Algebra")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given SubjectInput has invalid parameters, add should return response 'Bad Request")
    public void givenSubjectInputHasInvalidParameters_addShouldReturnResponseBadRequest() throws Exception {
        SubjectInput inputWithBlankName = SubjectInput.builder().name("  ").build();

        String inputAsString = mapper.writeValueAsString(inputWithBlankName);

        this.mockMvc
                .perform(
                        post("/api/subjects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Subject does not exists with given ID, update should return response 'Not Found'")
    public void whenSubjectDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() throws Exception {
        when(subjectService.findById(99L)).thenReturn(Optional.empty());

        String subject2AsString = mapper.writeValueAsString(subjectInput2);

        this.mockMvc
                .perform(
                        put("/api/subjects/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(subject2AsString)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Subject exists with given ID and SubjectInput parameters are valid, update should return updated Subject")
    public void whenSubjectExistsWithGivenIdAndSubjectInputParametersAreValid_updateShouldReturnUpdatedSubject() throws Exception {
        when(subjectService.findById(1L)).thenReturn(Optional.of(subjectOutput1));
        SubjectInput updateInput = SubjectInput.builder()
                .name("Algebra II")
                .teacherId(subjectInput1.getTeacherId())
                .build();
        SubjectOutput subjectUpdated = SubjectOutput.builder()
                .id(1L)
                .name("Algebra II")
                .teacher(subjectOutput1.getTeacher())
                .build();
        when(subjectService.update(1L, updateInput)).thenReturn(subjectUpdated);

        String inputAsString = mapper.writeValueAsString(updateInput);
        String outputAsString = mapper.writeValueAsString(subjectUpdated);

        this.mockMvc
                .perform(
                        put("/api/subjects/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(outputAsString));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("given SubjectInput has invalid parameters, update should return response 'Bad Request")
    public void givenSubjectInputHasInvalidParameters_updateShouldReturnResponseBadRequest() throws Exception {
        SubjectInput inputWithBlankName = SubjectInput.builder().name("  ").build();
        when(subjectService.findById(1L)).thenReturn(Optional.of(subjectOutput1));

        String inputAsString = mapper.writeValueAsString(inputWithBlankName);

        this.mockMvc
                .perform(
                        put("/api/subjects/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Subject exists with given ID, delete should return response 'No Content'")
    public void whenSubjectExistsWithGivenId_deleteShouldReturnResponseNoContent() throws Exception {
        when(subjectService.findById(2L)).thenReturn(Optional.of(subjectOutput2));

        this.mockMvc
                .perform(delete("/api/subjects/2"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Subject does not exist with given ID, delete should return response 'Not Found'")
    public void whenSubjectDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() throws Exception {
        when(subjectService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/subjects/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Subject is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
    public void whenSubjectIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() throws Exception {
        GradebookOutput savedEntry = GradebookOutput.builder()
                .id(1L)
                .subject(new SimpleData(1L, "Algebra"))
                .build();
        when(subjectService.findById(1L)).thenReturn(Optional.of(subjectOutput1));
        GradebookRequest request = GradebookRequest.builder().subjectId(1L).build();
        when(gradebookService.findGradebookEntries(request)).thenReturn(List.of(savedEntry));

        this.mockMvc
                .perform(delete("/api/subjects/1"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when entities exist with given IDs, addStudentToSubject should return Subject with added Student")
    public void whenEntitiesExistWithGivenIds_addStudentToSubject_shouldReturnSubjectWithAddedStudent() throws Exception {
        StudentDto johnDoe = StudentDto.builder().id(1L).firstname("John").lastname("Doe").build();
        TeacherDto darrellBowen = TeacherDto.builder().id(1L).firstname("Darrell").lastname("Bowen").build();
        SubjectOutput algebra = SubjectOutput.builder()
                .id(1L)
                .name("Algebra")
                .teacher(SimpleTeacher.builder().id(1L).firstname("Darrell").lastname("Bowen").build())
                .students(Collections.singletonList(SimpleStudent.builder().id(1L).firstname("John").lastname("Doe").build()))
                .build();

        when(studentService.findById(1L)).thenReturn(Optional.of(johnDoe));
        when(teacherService.findById(1L)).thenReturn(Optional.of(darrellBowen));
        when(subjectService.findById(1L)).thenReturn(Optional.of(algebra));
        when(subjectService.addStudentToSubject(1L, 1L)).thenReturn(algebra);

        this.mockMvc
                .perform(post("/api/subjects/1/add_student/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Algebra")))
                .andExpect(jsonPath("$.students", hasSize(1)))
                .andExpect(jsonPath("$.students[0].id", is(1)))
                .andExpect(jsonPath("$.students[0].firstname", is("John")))
                .andExpect(jsonPath("$.students[0].lastname", is("Doe")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Student does not exist with given ID, addStudentToSubject should return response 'Not Found'")
    public void whenStudentDoesNotExistWithGivenId_addStudentToSubject_shouldReturnResponseNotFound() throws Exception {
        when(studentService.findById(99L)).thenReturn(Optional.empty());
        when(subjectService.findById(1L)).thenReturn(Optional.of(subjectOutput1));

        this.mockMvc
                .perform(post("/api/subjects/1/add_student/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    @DisplayName("when Subject does not exist with given ID, addStudentToSubject should return response 'Not Found'")
    public void whenSubjectDoesNotExistWithGivenId_addStudentToSubject_shouldReturnResponseNotFound() throws Exception {
        when(subjectService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(post("/api/subjects/99/add_student/1"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
