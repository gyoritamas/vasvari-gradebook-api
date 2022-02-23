package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.AssignmentController;
import com.codecool.gradebookapi.controller.CourseController;
import com.codecool.gradebookapi.controller.GradebookController;
import com.codecool.gradebookapi.controller.StudentController;
import com.codecool.gradebookapi.dto.*;
import com.codecool.gradebookapi.testmodel.CourseOutput;
import org.junit.jupiter.api.*;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CourseIntegrationTests {
    @Autowired
    private TestRestTemplate template;

    @LocalServerPort
    private int port;

    private Link linkToClasses;
    private Link linkToStudents;

    private CourseInput courseInput1;
    private CourseInput courseInput2;
    private CourseOutput courseOutput1;
    private CourseOutput courseOutput2;
    private StudentDto student;

    @BeforeEach
    public void setUp() {
        linkToClasses = linkTo(CourseController.class).withSelfRel();
        linkToStudents = linkTo(StudentController.class).withSelfRel();

        courseInput1 = CourseInput.builder()
                .name("Algebra")
                .build();
        courseInput2 = CourseInput.builder()
                .name("Biology")
                .build();
        courseOutput1 = CourseOutput.builder()
                .id(1L)
                .name("Algebra")
                .students(new ArrayList<>())
                .build();
        courseOutput2 = CourseOutput.builder()
                .id(2L)
                .name("Biology")
                .students(new ArrayList<>())
                .build();

        student = StudentDto.builder()
                .firstname("John")
                .lastname("Doe")
                .gradeLevel(9)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate("2005-12-01")
                .build();
    }

    @Nested
    @DisplayName("POST methods")
    class PostMethodTests {
        @Test
        @DisplayName("when Class posted with valid parameters, should return created Class")
        public void whenClassPostedWithValidParameters_shouldReturnCreatedClass() {
            ResponseEntity<CourseOutput> response =
                    template.postForEntity(linkToClasses.getHref(), courseInput1, CourseOutput.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(courseOutput1);
        }

        @Test
        @DisplayName("when Class posted with invalid parameter, should return response 'Bad Request'")
        public void whenClassPostedWithInvalidParameter_shouldReturnResponseBadRequest() {
            CourseInput inputWithBlankName = CourseInput.builder().name("  ").build();
            ResponseEntity<?> response =
                    template.postForEntity(linkToClasses.getHref(), inputWithBlankName, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("add Student to Class should return Class with added Student")
        public void addStudentToClass_shouldReturnClassWithAddedStudent() {
            long studentId = template.postForObject(linkToStudents.getHref(), student, StudentDto.class).getId();
            long classId = template.postForObject(linkToClasses.getHref(), courseInput1, CourseOutput.class).getId();

            Link linkToClassEnrollment =
                    linkTo(methodOn(CourseController.class).addStudentToClass(classId, studentId)).withSelfRel();
            ResponseEntity<CourseOutput> response =
                    template.postForEntity(linkToClassEnrollment.getHref(), null, CourseOutput.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStudents()).isEqualTo(List.of("John Doe"));
        }

        @Test
        @DisplayName("when Student does not exist with given ID, should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_shouldReturnResponseNotFound() {
            long classId = template.postForObject(linkToClasses.getHref(), courseInput1, CourseOutput.class).getId();

            Link linkToClassEnrollment =
                    linkTo(methodOn(CourseController.class).addStudentToClass(classId, 99L)).withSelfRel();
            ResponseEntity<?> response =
                    template.postForEntity(linkToClassEnrollment.getHref(), null, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Class does not exist with given ID, should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_shouldReturnResponseNotFound() {
            long studentId = template.postForObject(linkToStudents.getHref(), student, StudentDto.class).getId();

            Link linkToClassEnrollment =
                    linkTo(methodOn(CourseController.class).addStudentToClass(99L, studentId)).withSelfRel();
            ResponseEntity<?> response =
                    template.postForEntity(linkToClassEnrollment.getHref(), null, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET methods")
    @DirtiesContext(classMode = BEFORE_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetMethodTests {
        @Test
        @Order(1)
        @DisplayName("given empty database, getAll should return empty list")
        public void givenEmptyDatabase_getAllShouldReturnEmptyList() {
            String urlToClasses = String.format("http://localhost:%d/api/classes", port);
            Traverson traverson = new Traverson(URI.create(urlToClasses), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<CourseOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<CourseOutput> classResource = traverson
                    .follow("$._links.self.href")
                    .toObject(collectionModelType);

            assertThat(classResource).isNotNull();
            assertThat(classResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when Classes posted, getAll should return list of Classes")
        public void whenClassesPosted_getAllShouldReturnListOfClasses() {
            CourseOutput class1 = template.postForObject(linkToClasses.getHref(), courseInput1, CourseOutput.class);
            CourseOutput class2 = template.postForObject(linkToClasses.getHref(), courseInput2, CourseOutput.class);

            String urlToClasses = String.format("http://localhost:%d/api/classes", port);
            Traverson traverson = new Traverson(URI.create(urlToClasses), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<CourseOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<CourseOutput> classResource = traverson
                    .follow("$._links.self.href")
                    .toObject(collectionModelType);

            assertThat(classResource).isNotNull();
            assertThat(classResource.getContent()).containsExactly(class1, class2);
        }

        @Test
        @DisplayName("when Class exists with given ID, getById should return Class")
        public void whenClassExistsWithGivenId_getByIdShouldReturnClass() {
            CourseOutput classPosted = template.postForObject(linkToClasses.getHref(), courseInput1, CourseOutput.class);

            Link linkToClass = linkTo(methodOn(CourseController.class).getById(classPosted.getId())).withSelfRel();
            ResponseEntity<CourseOutput> response = template.getForEntity(linkToClass.getHref(), CourseOutput.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(classPosted);
        }

        @Test
        @DisplayName("when Class does not exist with given ID, getById should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.getForEntity(linkToClass.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PUT methods")
    class UpdateMethodTests {
        @Test
        @DisplayName("when Class exists with given ID and ClassInput parameters are valid, update should return updated Class")
        public void whenClassExistsWithGivenIdAndClassInputParametersAreValid_updateShouldReturnUpdatedClass() {
            long classId = template.postForObject(linkToClasses.getHref(), courseInput1, CourseOutput.class).getId();
            CourseInput update = CourseInput.builder().name("Algebra II").build();
            HttpEntity<CourseInput> classHttpEntity = createHttpEntityWithMediaTypeJson(update);

            Link linkToClass = linkTo(methodOn(CourseController.class).getById(classId)).withSelfRel();
            ResponseEntity<CourseOutput> response =
                    template.exchange(linkToClass.getHref(), HttpMethod.PUT, classHttpEntity, CourseOutput.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(classId);
            assertThat(response.getBody().getName()).isEqualTo("Algebra II");
        }

        @Test
        @DisplayName("when Class does not exist with given ID, update should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() {
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(99L)).withSelfRel();
            HttpEntity<CourseInput> classHttpEntity = createHttpEntityWithMediaTypeJson(courseInput1);
            ResponseEntity<?> response =
                    template.exchange(linkToClass.getHref(), HttpMethod.PUT, classHttpEntity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when ClassInput has invalid parameters, update should return response 'Bad Request'")
        public void whenClassInputHasInvalidParameters_updateShouldReturnResponseBadRequest() {
            long classId = template.postForObject(linkToClasses.getHref(), courseInput1, CourseOutput.class).getId();
            CourseInput updateWithBlankName = CourseInput.builder().name(" ").build();
            HttpEntity<CourseInput> classHttpEntity = createHttpEntityWithMediaTypeJson(updateWithBlankName);

            Link linkToClass = linkTo(methodOn(CourseController.class).getById(classId)).withSelfRel();
            ResponseEntity<?> response =
                    template.exchange(linkToClass.getHref(), HttpMethod.PUT, classHttpEntity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DELETE methods")
    class DeleteMethodTests {
        @Test
        @DisplayName("when Class exists with given ID, delete should remove Class")
        public void whenClassExistsWithGivenId_deleteShouldRemoveClass() {
            long classId = template.postForObject(linkToClasses.getHref(), courseInput1, CourseOutput.class).getId();
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(classId)).withSelfRel();

            template.exchange(linkToClass.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, CourseOutput.class);
            ResponseEntity<?> response = template.getForEntity(linkToClass.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Class does not exist with given ID, delete should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() {
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response =
                    template.exchange(linkToClass.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Class is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
        public void whenClassIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() {
            CourseOutput course = template.postForObject(linkToClasses.getHref(), courseInput1, CourseOutput.class);

            postEntryRelatedToClass(course);
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(course.getId())).withSelfRel();
            ResponseEntity<?> response =
                    template.exchange(linkToClass.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    private void postEntryRelatedToClass(CourseOutput clazz) {
        AssignmentInput assignmentInput = AssignmentInput.builder().name("Homework 1").type("HOMEWORK").build();
        Link linkToAssignments = linkTo(AssignmentController.class).withSelfRel();
        AssignmentOutput assignment = template.postForObject(linkToAssignments.getHref(), assignmentInput, AssignmentOutput.class);
        student = template.postForObject(linkToStudents.getHref(), student, StudentDto.class);

        Link linkToClassEnrollment =
                linkTo(methodOn(CourseController.class).addStudentToClass(clazz.getId(), student.getId())).withSelfRel();
        template.postForObject(linkToClassEnrollment.getHref(), null, CourseOutput.class);

        GradebookInput gradebookInput = GradebookInput.builder()
                .studentId(student.getId())
                .courseId(clazz.getId())
                .assignmentId(assignment.getId())
                .grade(3)
                .build();
        Link linkToGradeAssignment =
                linkTo(methodOn(GradebookController.class).gradeAssignment(gradebookInput)).withSelfRel();
        ResponseEntity<GradebookOutput> response =
                template.postForEntity(linkToGradeAssignment.getHref(), gradebookInput, GradebookOutput.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
    }

    private HttpEntity<CourseInput> createHttpEntityWithMediaTypeJson(CourseInput clazz) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(clazz, headers);
    }
}
