package com.codecool.gradebookapi.unit.controller;

import com.codecool.gradebookapi.controller.CourseController;
import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.assembler.CourseModelAssembler;
import com.codecool.gradebookapi.service.CourseService;
import com.codecool.gradebookapi.service.GradebookService;
import com.codecool.gradebookapi.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import(CourseModelAssembler.class)
public class CourseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private StudentService studentService;

    @MockBean
    private GradebookService gradebookService;

    private static ObjectMapper mapper;

    private CourseInput courseInput1;
    private CourseInput courseInput2;
    private CourseOutput courseOutput1;
    private CourseOutput courseOutput2;

    public CourseControllerTests() {
        mapper = new ObjectMapper();
    }

    @BeforeEach
    public void setUp() {
        courseInput1 = CourseInput.builder()
                .course("Algebra")
                .build();
        courseInput2 = CourseInput.builder()
                .course("Biology")
                .build();
        courseOutput1 = CourseOutput.builder()
                .id(1L)
                .course("Algebra")
                .students(List.of("Diophantus", "Brahmagupta"))
                .build();
        courseOutput2 = CourseOutput.builder()
                .id(2L)
                .course("Biology")
                .students(List.of("Charles Darwin", "Gregor Mendel"))
                .build();
    }

    @Test
    @DisplayName("given empty database, getAll should return empty list")
    public void givenEmptyDatabase_getAllShouldReturnEmptyList() throws Exception {
        when(courseService.findAll()).thenReturn(List.of());

        this.mockMvc
                .perform(get("/api/classes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.classes").doesNotExist());
    }

    @Test
    @DisplayName("when Classes posted, getAll should return list of Classes")
    public void whenClassesPosted_getAllShouldReturnListOfClasses() throws Exception {
        when(courseService.findAll()).thenReturn(List.of(courseOutput1, courseOutput2));

        this.mockMvc
                .perform(get("/api/classes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.classes", hasSize(2)))
                .andExpect(jsonPath("$._embedded.classes[0].course", is("Algebra")))
                .andExpect(jsonPath("$._embedded.classes[1].course", is("Biology")));
    }

    @Test
    @DisplayName("when Class with given ID exists, getById should return Class")
    public void whenClassWithGivenIdExists_getByIdShouldReturnClass() throws Exception {
        when(courseService.findById(1L)).thenReturn(Optional.of(courseOutput1));

        this.mockMvc
                .perform(get("/api/classes/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.course", is("Algebra")));
    }

    @Test
    @DisplayName("when Class with given ID does not exist, getById should return response 'Not Found'")
    public void whenClassWithGivenIdDoesNotExist_getByIdShouldReturnResponseNotFound() throws Exception {
        when(courseService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/classes/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("given ClassInput parameters are valid, add should return created Class")
    public void givenClassInputParametersAreValid_addShouldReturnCreatedClass() throws Exception {
        when(courseService.save(courseInput1)).thenReturn(courseOutput1);

        String inputAsString = mapper.writeValueAsString(courseInput1);

        this.mockMvc
                .perform(
                        post("/api/classes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.course", is("Algebra")));
    }

    @Test
    @DisplayName("given ClassInput has invalid parameters, add should return response 'Bad Request")
    public void givenClassInputHasInvalidParameters_addShouldReturnResponseBadRequest() throws Exception {
        CourseInput inputWithBlankName = CourseInput.builder().course("  ").build();

        String inputAsString = mapper.writeValueAsString(inputWithBlankName);

        this.mockMvc
                .perform(
                        post("/api/classes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("when Class does not exists with given ID, update should return response 'Not Found'")
    public void whenClassDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() throws Exception {
        when(courseService.findById(99L)).thenReturn(Optional.empty());

        String class2AsString = mapper.writeValueAsString(courseInput2);

        this.mockMvc
                .perform(
                        put("/api/classes/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(class2AsString)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when Class exists with given ID and ClassInput parameters are valid, update should return updated Class")
    public void whenClassExistsWithGivenIdAndClassInputParametersAreValid_updateShouldReturnUpdatedClass() throws Exception {
        when(courseService.findById(1L)).thenReturn(Optional.of(courseOutput1));
        CourseInput updateInput = CourseInput.builder().course("Algebra II").build();
        CourseOutput classUpdated = CourseOutput.builder().id(1L).course("Algebra II").build();
        when(courseService.update(1L, updateInput)).thenReturn(classUpdated);

        String inputAsString = mapper.writeValueAsString(updateInput);
        String outputAsString = mapper.writeValueAsString(classUpdated);

        this.mockMvc
                .perform(
                        put("/api/classes/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(outputAsString));
    }

    @Test
    @DisplayName("given ClassInput has invalid parameters, update should return response 'Bad Request")
    public void givenClassInputHasInvalidParameters_updateShouldReturnResponseBadRequest() throws Exception {
        CourseInput inputWithBlankName = CourseInput.builder().course("  ").build();
        when(courseService.findById(1L)).thenReturn(Optional.of(courseOutput1));

        String inputAsString = mapper.writeValueAsString(inputWithBlankName);

        this.mockMvc
                .perform(
                        put("/api/classes/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inputAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("when Class exists with given ID, delete should return response 'No Content'")
    public void whenClassExistsWithGivenId_deleteShouldReturnResponseNoContent() throws Exception {
        when(courseService.findById(2L)).thenReturn(Optional.of(courseOutput2));

        this.mockMvc
                .perform(delete("/api/classes/2"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("when Class does not exist with given ID, delete should return response 'Not Found'")
    public void whenClassDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() throws Exception {
        when(courseService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/classes/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when Class is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
    public void whenClassIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() throws Exception {
        GradebookOutput savedEntry = GradebookOutput.builder().id(1L).classId(1L).build();
        when(courseService.findById(1L)).thenReturn(Optional.of(courseOutput1));
        when(gradebookService.findByClassId(1L)).thenReturn(List.of(savedEntry));

        this.mockMvc
                .perform(delete("/api/classes/1"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("when entities exist with given IDs, addStudentToClass should return Class with added Student")
    public void whenEntitiesExistWithGivenIds_addStudentToClass_shouldReturnClassWithAddedStudent() throws Exception {
        StudentDto student = StudentDto.builder().id(1L).firstname("John").lastname("Doe").build();
        CourseOutput clazz = CourseOutput.builder().id(1L).course("Algebra").students(List.of("John Doe")).build();

        when(studentService.findById(1L)).thenReturn(Optional.of(student));
        when(courseService.findById(1L)).thenReturn(Optional.of(clazz));
        when(courseService.addStudentToClass(1L, 1L)).thenReturn(clazz);

        this.mockMvc
                .perform(post("/api/classes/1/class_enrollment/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.course", is("Algebra")))
                .andExpect(jsonPath("$.students", hasSize(1)))
                .andExpect(jsonPath("$.students[0]", is("John Doe")));
    }

    @Test
    @DisplayName("when Student does not exist with given ID, addStudentToClass should return response 'Not Found'")
    public void whenStudentDoesNotExistWithGivenId_addStudentToClass_shouldReturnResponseNotFound() throws Exception {
        when(studentService.findById(99L)).thenReturn(Optional.empty());
        when(courseService.findById(1L)).thenReturn(Optional.of(courseOutput1));

        this.mockMvc
                .perform(post("/api/classes/1/class_enrollment/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when Class does not exist with given ID, addStudentToClass should return response 'Not Found'")
    public void whenClassDoesNotExistWithGivenId_addStudentToClass_shouldReturnResponseNotFound() throws Exception {
        when(courseService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(post("/api/classes/99/class_enrollment/1"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
