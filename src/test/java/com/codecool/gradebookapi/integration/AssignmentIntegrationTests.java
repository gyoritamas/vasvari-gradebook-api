package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.AssignmentController;
import com.codecool.gradebookapi.controller.CourseController;
import com.codecool.gradebookapi.controller.GradebookController;
import com.codecool.gradebookapi.controller.StudentController;
import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.model.AssignmentType;
import com.codecool.gradebookapi.testmodel.AssignmentOutput;
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
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AssignmentIntegrationTests {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    Jackson2ObjectMapperBuilder objectMapperBuilder;

    @LocalServerPort
    private int port;

    private Link linkToAssignments;

    private AssignmentInput assignmentInput1;
    private AssignmentInput assignmentInput2;
    private AssignmentOutput assignmentOutput1;
    private AssignmentOutput assignmentOutput2;
    private StudentDto student;

    @BeforeEach
    public void setUp() {
        linkToAssignments = linkTo(AssignmentController.class).withSelfRel();

        assignmentInput1 = AssignmentInput.builder()
                .name("Homework 1")
                .type("HOMEWORK")
                .description("Read chapters 1 to 5")
                .build();
        assignmentInput2 = AssignmentInput.builder()
                .name("Homework 2")
                .type("HOMEWORK")
                .description("Read Chapters 6 and 9.")
                .build();
        assignmentOutput1 = AssignmentOutput.builder()
                .name("Homework 1")
                .type(AssignmentType.HOMEWORK)
                .description("Read chapters 1 to 5")
                .build();
        assignmentOutput2 = AssignmentOutput.builder()
                .name("Homework 2")
                .type(AssignmentType.HOMEWORK)
                .description("Read Chapters 6 and 9.")
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
        @DisplayName("when Assignment posted with valid parameters, should return created Assignment")
        public void whenAssignmentPostedWithValidParameters_shouldReturnCreatedAssignment() {
            ResponseEntity<AssignmentOutput> response =
                    template.postForEntity(linkToAssignments.getHref(), assignmentInput1, AssignmentOutput.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(assignmentOutput1);
        }

        @Test
        @DisplayName("when Assignment posted with invalid parameter, should return response 'Bad Request'")
        public void whenAssignmentPostedWithInvalidParameter_shouldReturnResponseBadRequest() {
            givenAssignmentWithEmptyName_postAssignment_shouldReturnWithBadRequest();
            givenAssignmentWithWrongType_postAssignment_shouldReturnWithBadRequest();
        }

        private void givenAssignmentWithEmptyName_postAssignment_shouldReturnWithBadRequest() {
            AssignmentInput inputWithBlankName = AssignmentInput.builder().name(" ").type("TEST").build();
            ResponseEntity<?> response =
                    template.postForEntity(linkToAssignments.getHref(), inputWithBlankName, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        private void givenAssignmentWithWrongType_postAssignment_shouldReturnWithBadRequest() {
            AssignmentInput inputWithWrongType = AssignmentInput.builder().name("Test").type("BAD_TYPE").build();
            ResponseEntity<?> response =
                    template.postForEntity(linkToAssignments.getHref(), inputWithWrongType, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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
            String urlToAssignments = String.format("http://localhost:%d/api/assignments", port);
            Traverson traverson = new Traverson(URI.create(urlToAssignments), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<AssignmentOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<AssignmentOutput> assignmentResource = traverson
                    .follow("$._links.self.href")
                    .toObject(collectionModelType);

            assertThat(assignmentResource).isNotNull();
            assertThat(assignmentResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when Assignments posted, getAll should return list of Assignments")
        public void whenAssignmentsPosted_getAllShouldReturnListOfAssignments() {
            AssignmentOutput assignment1 =
                    template.postForObject(linkToAssignments.getHref(), assignmentInput1, AssignmentOutput.class);
            AssignmentOutput assignment2 =
                    template.postForObject(linkToAssignments.getHref(), assignmentInput2, AssignmentOutput.class);

            String urlToAssignments = String.format("http://localhost:%d/api/assignments", port);
            Traverson traverson = new Traverson(URI.create(urlToAssignments), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<AssignmentOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<AssignmentOutput> resources = traverson
                    .follow("$._links.self.href")
                    .toObject(collectionModelType);

            assertThat(resources).isNotNull();
            assertThat(resources.getContent()).isNotNull();
            assertThat(resources.getContent()).containsExactly(assignment1, assignment2);
        }

        @Test
        @DisplayName("when Assignment exists with given ID, getById should return Assignment")
        public void whenAssignmentExistsWithGivenId_getByIdShouldReturnAssignment() {
            long id = template.postForObject(linkToAssignments.getHref(), assignmentInput1, AssignmentOutput.class).getId();

            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(id)).withSelfRel();
            ResponseEntity<AssignmentOutput> response =
                    template.getForEntity(linkToAssignment.getHref(), AssignmentOutput.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(assignmentOutput1);
        }

        @Test
        @DisplayName("when Assignment does not exist with given ID, getById should return response 'Not Found'")
        public void whenAssignmentDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.getForEntity(linkToAssignment.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PUT methods")
    class UpdateMethodTests {
        @Test
        @DisplayName("when Assignment exists with given ID, update should return updated Assignment")
        public void whenAssignmentExistsWithGivenId_updateShouldReturnUpdatedAssignment() {
            long id = template.postForObject(linkToAssignments.getHref(), assignmentInput1, AssignmentOutput.class).getId();
            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(id)).withSelfRel();
            AssignmentInput update = AssignmentInput.builder().name("Homework III").type("HOMEWORK").build();
            HttpEntity<AssignmentInput> assignmentHttpEntity = createHttpEntityWithMediaTypeJson(update);

            ResponseEntity<AssignmentOutput> response =
                    template.exchange(linkToAssignment.getHref(), HttpMethod.PUT, assignmentHttpEntity, AssignmentOutput.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(id);
            assertThat(response.getBody().getName()).isEqualTo("Homework III");
        }

        @Test
        @DisplayName("when Assignment does not exist with given ID, update should return response 'Not Found'")
        public void whenAssignmentDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() {
            HttpEntity<AssignmentInput> assignmentHttpEntity = createHttpEntityWithMediaTypeJson(assignmentInput1);
            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response =
                    template.exchange(linkToAssignment.getHref(), HttpMethod.PUT, assignmentHttpEntity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Assignment updated with invalid parameter, update should return response 'Bad Request'")
        public void whenAssignmentUpdatedWithInvalidParameter_shouldReturnResponseBadRequest() {
            long assignmentId =
                    template.postForObject(linkToAssignments.getHref(), assignmentInput1, AssignmentOutput.class).getId();

            givenAssignmentWithEmptyName_updateAssignment_shouldReturnWithBadRequest(assignmentId);
            givenAssignmentWithWrongType_updateAssignment_shouldReturnWithBadRequest(assignmentId);
        }

        private void givenAssignmentWithEmptyName_updateAssignment_shouldReturnWithBadRequest(Long id) {
            AssignmentInput updateWithBlankName = AssignmentInput.builder().name(" ").type("TEST").build();
            HttpEntity<?> httpEntity = createHttpEntityWithMediaTypeJson(updateWithBlankName);
            Link linkToUpdate =
                    linkTo(methodOn(AssignmentController.class).update(updateWithBlankName, id)).withSelfRel();
            ResponseEntity<?> response =
                    template.exchange(linkToUpdate.getHref(), HttpMethod.PUT, httpEntity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        private void givenAssignmentWithWrongType_updateAssignment_shouldReturnWithBadRequest(Long id) {
            AssignmentInput updateWithWrongType = AssignmentInput.builder().name("Test").type("BAD_TYPE").build();
            HttpEntity<?> httpEntity = createHttpEntityWithMediaTypeJson(updateWithWrongType);
            Link linkToUpdate =
                    linkTo(methodOn(AssignmentController.class).update(updateWithWrongType, id)).withSelfRel();
            ResponseEntity<?> response =
                    template.exchange(linkToUpdate.getHref(), HttpMethod.PUT, httpEntity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DELETE methods")
    class DeleteMethodTests {
        @Test
        @DisplayName("when Assignment exists with given ID, delete should remove Assignment")
        public void whenAssignmentExistsWithGivenId_deleteShouldRemoveAssignment() {
            long id = template.postForObject(linkToAssignments.getHref(), assignmentInput1, AssignmentOutput.class).getId();
            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(id)).withSelfRel();

            template.exchange(linkToAssignment.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, AssignmentOutput.class);
            ResponseEntity<?> response = template.getForEntity(linkToAssignment.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Assignment does not exist with given ID, delete should return response 'Not Found'")
        public void whenAssignmentDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() {
            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response =
                    template.exchange(linkToAssignment.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Assignment is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
        public void whenAssignmentIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() {
            AssignmentOutput assignment =
                    template.postForObject(linkToAssignments.getHref(), assignmentInput1, AssignmentOutput.class);

            postEntryRelatedToAssignment(assignment);
            Link linkToAssignment =
                    linkTo(methodOn(AssignmentController.class).getById(assignment.getId())).withSelfRel();
            ResponseEntity<?> response =
                    template.exchange(linkToAssignment.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    private void postEntryRelatedToAssignment(AssignmentOutput assignment) {
        CourseOutput course = CourseOutput.builder().name("Algebra").build();
        Link linkToStudents = linkTo(StudentController.class).withSelfRel();
        student = template.postForObject(linkToStudents.getHref(), student, StudentDto.class);
        Link linkToClasses = linkTo(CourseController.class).withSelfRel();
        course = template.postForObject(linkToClasses.getHref(), course, CourseOutput.class);

        Link linkToClassEnrollment =
                linkTo(methodOn(CourseController.class).addStudentToClass(course.getId(), student.getId())).withSelfRel();
        template.postForObject(linkToClassEnrollment.getHref(), null, CourseOutput.class);

        GradebookInput gradebookInput = GradebookInput.builder()
                .studentId(student.getId())
                .courseId(course.getId())
                .assignmentId(assignment.getId())
                .grade(2)
                .build();
        Link linkToGradeAssignment =
                linkTo(methodOn(GradebookController.class).gradeAssignment(gradebookInput)).withSelfRel();
        ResponseEntity<GradebookOutput> response =
                template.postForEntity(linkToGradeAssignment.getHref(), gradebookInput, GradebookOutput.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
    }

    private HttpEntity<AssignmentInput> createHttpEntityWithMediaTypeJson(AssignmentInput assignment) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(assignment, headers);
    }
}
