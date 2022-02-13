package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.AssignmentController;
import com.codecool.gradebookapi.controller.CourseController;
import com.codecool.gradebookapi.controller.GradebookController;
import com.codecool.gradebookapi.controller.StudentController;
import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.testmodel.AssignmentOutput;
import com.codecool.gradebookapi.testmodel.ClassOutput;
import com.codecool.gradebookapi.testmodel.StudentDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class StudentIntegrationTests {
    @Autowired
    private TestRestTemplate template;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private Link baseLink;

    private StudentDto student1;
    private StudentDto student2;

    @BeforeEach
    public void setUp() {
        this.baseUrl = "http://localhost:" + port + "/api/students";
        baseLink = linkTo(StudentController.class).withRel("students");
        student1 = StudentDto.builder()
                .firstname("John")
                .lastname("Doe")
                .gradeLevel(2)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate("1990-12-01")
                .build();
        student2 = StudentDto.builder()
                .firstname("Jane")
                .lastname("Doe")
                .gradeLevel(2)
                .email("janedoe@email.com")
                .address("9351 Morris St., Reisterstown, MD 21136")
                .phone("202-555-9810")
                .birthdate("1990-04-13")
                .build();
    }

    @Nested
    @DisplayName("POST methods")
    class PostMethods {
        @Test
        @DisplayName("when Student posted with valid parameters, should return created Student")
        public void whenStudentPostedWithValidParameters_shouldReturnCreatedStudent() {
            ResponseEntity<StudentDto> response = template.postForEntity(baseLink.getHref(), student1, StudentDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(student1);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/invalid_student_data.csv", numLinesToSkip = 1, delimiter = ';')
        @DisplayName("when Student posted with invalid parameters, should return response 'Bad Request'")
        public void whenStudentPostedWithInvalidParameters_shouldReturnResponseBadRequest(
                @AggregateWith(StudentAggregator.class) StudentDto student) {
            ResponseEntity<?> response = template.postForEntity(baseLink.getHref(), student, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET methods")
    @DirtiesContext(classMode = BEFORE_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetMethods {
        @Test
        @Order(1)
        @DisplayName("given empty database, getAll should return empty list")
        public void givenEmptyDatabase_getAllShouldReturnEmptyList() {
            Traverson traverson = new Traverson(URI.create(baseUrl), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<StudentDto> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<StudentDto> studentResource = traverson
                    .follow("$._links.self.href")
                    .toObject(collectionModelType);

            assertThat(studentResource).isNotNull();
            assertThat(studentResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when Students posted, getAll should return list of Students")
        public void whenStudentsPosted_getAllShouldReturnListOfStudents() {
            template.postForObject(baseLink.getHref(), student1, StudentDto.class);
            template.postForObject(baseLink.getHref(), student2, StudentDto.class);

            Traverson traverson = new Traverson(URI.create(baseUrl), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<StudentDto> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<StudentDto> studentResource = traverson
                    .follow("$._links.self.href")
                    .toObject(collectionModelType);

            assertThat(studentResource).isNotNull();
            assertThat(studentResource.getContent()).isNotNull();
            assertThat(studentResource.getContent()).hasSize(2);
            assertThat(studentResource.getContent()).containsExactly(student1, student2);
        }

        @Test
        @DisplayName("when Student exists with given ID, getById should return Student")
        public void whenStudentExistsWithGivenId_getByIdShouldReturnStudent() {
            long studentId = template.postForObject(baseLink.getHref(), student1, StudentDto.class).getId();

            Link link = linkTo(methodOn(StudentController.class).getById(studentId)).withSelfRel();
            ResponseEntity<StudentDto> response = template.getForEntity(link.getHref(), StudentDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEqualTo(student1);
        }

        @Test
        @DisplayName("when Student does not exist with given ID, getById should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link link = linkTo(methodOn(StudentController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.getForEntity(link.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Student exists with given ID, getClassesOfStudent should return list of Classes")
        public void whenStudentExistsWithGivenId_getClassesOfStudentShouldReturnListOfClasses() {
            student1 = template.postForObject(baseUrl, student1, StudentDto.class);
            student2 = template.postForObject(baseUrl, student2, StudentDto.class);

            CourseInput courseInput1 = CourseInput.builder().course("Algebra").build();
            CourseInput courseInput2 = CourseInput.builder().course("Biology").build();

            Link linkToClasses = linkTo(CourseController.class).withSelfRel();
            ClassOutput classOutput1 =
                    template.postForObject(linkToClasses.getHref(), courseInput1, ClassOutput.class);
            ClassOutput classOutput2 =
                    template.postForObject(linkToClasses.getHref(), courseInput2, ClassOutput.class);

            Link linkToClassEnrollment1 = linkTo(methodOn(CourseController.class)
                    .addStudentToClass(classOutput1.getId(), student1.getId()))
                    .withSelfRel();
            Link linkToClassEnrollment2 = linkTo(methodOn(CourseController.class)
                    .addStudentToClass(classOutput2.getId(), student1.getId()))
                    .withSelfRel();

            classOutput1 = template.postForObject(linkToClassEnrollment1.getHref(), null, ClassOutput.class);
            classOutput2 = template.postForObject(linkToClassEnrollment2.getHref(), null, ClassOutput.class);

            String urlToClassesOfStudent = String.format("http://localhost:%d/api/students/%d/classes", port, student1.getId());
            Traverson traverson = new Traverson(URI.create(urlToClassesOfStudent), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<ClassOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<ClassOutput> classResource = traverson
                    .follow("$._links.self.href")
                    .toObject(collectionModelType);

            assertThat(classResource).isNotNull();
            assertThat(classResource.getContent()).isNotNull();
            assertThat(classResource.getContent()).containsExactly(classOutput1, classOutput2);
        }

        @Test
        @DisplayName("when student does not exist with given ID, getClassesOfStudent should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_getClassesOfStudentShouldReturnResponseNotFound() {
            Link linkToClassesOfStudent = linkTo(methodOn(StudentController.class)
                    .getClassesOfStudent(99L))
                    .withSelfRel();
            ResponseEntity<?> response = template.getForEntity(linkToClassesOfStudent.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PUT methods")
    class PutMethods {
        @Test
        @DisplayName("when Student exists with given ID, update should return updated Student")
        public void whenStudentExistsWithGivenId_updateShouldReturnUpdatedStudent() {
            StudentDto studentPosted = template.postForObject(baseUrl, student1, StudentDto.class);
            studentPosted.setFirstname("James");
            HttpEntity<StudentDto> studentHttpEntity = createHttpEntityWithMediaTypeJson(studentPosted);

            Link linkToUpdateStudent = linkTo(methodOn(StudentController.class)
                    .getById(studentPosted.getId()))
                    .withSelfRel();
            ResponseEntity<StudentDto> response =
                    template.exchange(linkToUpdateStudent.getHref(), HttpMethod.PUT, studentHttpEntity, StudentDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(studentPosted.getId());
            assertThat(response.getBody().getFirstname()).isEqualTo("James");
        }

        @Test
        @DisplayName("when Student does not exist with given ID, update should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() {
            Link linkToStudent = linkTo(methodOn(StudentController.class)
                    .getById(99L))
                    .withSelfRel();
            HttpEntity<StudentDto> studentHttpEntity = createHttpEntityWithMediaTypeJson(student1);
            ResponseEntity<?> response =
                    template.exchange(linkToStudent.getHref(), HttpMethod.PUT, studentHttpEntity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/invalid_student_data.csv", numLinesToSkip = 1, delimiter = ';')
        @DisplayName("when Student updated with invalid parameters, should return response 'Bad Request'")
        public void whenStudentUpdatedWithInvalidParameters_shouldReturnResponseBadRequest(
                @AggregateWith(StudentAggregator.class) StudentDto student) {
            long studentId = template.postForObject(baseUrl, student2, StudentDto.class).getId();

            Link linkToStudent = linkTo(methodOn(StudentController.class).getById(studentId)).withSelfRel();
            HttpEntity<StudentDto> httpEntity = createHttpEntityWithMediaTypeJson(student);
            ResponseEntity<?> response =
                    template.exchange(linkToStudent.getHref(), HttpMethod.PUT, httpEntity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DELETE methods")
    class DeleteMethods {
        @Test
        @DisplayName("when Student exists with given ID, delete should remove Student")
        public void whenStudentExistsWithGivenId_deleteShouldRemoveStudent() {
            long studentId = template.postForObject(baseUrl, student1, StudentDto.class).getId();

            Link linkToStudent = linkTo(methodOn(StudentController.class).getById(studentId)).withSelfRel();
            template.exchange(linkToStudent.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
            ResponseEntity<?> response = template.getForEntity(linkToStudent.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Student does not exist with given ID, delete should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() {
            Link linkToStudent = linkTo(methodOn(StudentController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response =
                    template.exchange(linkToStudent.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Student is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
        public void whenStudentIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() {
            StudentDto studentPosted = template.postForObject(baseUrl, student1, StudentDto.class);
            Link linkToStudent = linkTo(methodOn(StudentController.class).getById(studentPosted.getId())).withSelfRel();

            postEntryRelatedToStudent(studentPosted);
            ResponseEntity<?> response =
                    template.exchange(linkToStudent.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    private void postEntryRelatedToStudent(StudentDto student) {
        CourseInput clazz = CourseInput.builder().course("Algebra").build();
        Link linkToClasses = linkTo(CourseController.class).withSelfRel();
        ClassOutput classPosted =
                template.postForObject(linkToClasses.getHref(), clazz, ClassOutput.class);

        AssignmentInput assignment = AssignmentInput.builder().name("Homework 1").type("HOMEWORK").build();
        Link linkToAssignments = linkTo(AssignmentController.class).withSelfRel();
        AssignmentOutput assignmentPosted =
                template.postForObject(linkToAssignments.getHref(), assignment, AssignmentOutput.class);

        Link linkToClassEnrollment = linkTo(methodOn(CourseController.class)
                .addStudentToClass(classPosted.getId(), student.getId()))
                .withSelfRel();
        template.postForObject(linkToClassEnrollment.getHref(), null, ClassOutput.class);

        GradebookInput gradebookInput = GradebookInput.builder()
                .studentId(student.getId())
                .classId(classPosted.getId())
                .assignmentId(assignmentPosted.getId())
                .grade(5)
                .build();
        Link linkToGradeAssignment = linkTo(methodOn(GradebookController.class)
                .gradeAssignment(gradebookInput))
                .withSelfRel();
        ResponseEntity<GradebookOutput> response =
                template.postForEntity(linkToGradeAssignment.getHref(), gradebookInput, GradebookOutput.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
    }

    private HttpEntity<StudentDto> createHttpEntityWithMediaTypeJson(StudentDto student) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(student, headers);
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
                    .birthdate(accessor.getString(6))
                    .build();
        }
    }
}
