package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.*;
import com.codecool.gradebookapi.dto.*;
import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import com.codecool.gradebookapi.integration.util.AuthorizationManager;
import com.codecool.gradebookapi.model.AssignmentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.time.LocalDate;

import static com.codecool.gradebookapi.security.ApplicationUserRole.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(AuthorizationManager.class)
public class AssignmentIntegrationTests {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private AuthorizationManager auth;

    @LocalServerPort
    private int port;

    private Link linkToAssignments;

    private AssignmentInput assignmentInput1;
    private AssignmentInput assignmentInput2;
    private StudentDto student;
    private TeacherDto teacher;
    private CourseInput courseInput;

    @BeforeEach
    public void setUp() {
        linkToAssignments = linkTo(AssignmentController.class).withSelfRel();

        auth.setRole(ADMIN);

        assignmentInput1 = AssignmentInput.builder()
                .name("Homework 1")
                .type(AssignmentType.HOMEWORK)
                .description("Read chapters 1 to 5")
                .deadline(LocalDate.of(2051, 1, 1))
                .build();
        assignmentInput2 = AssignmentInput.builder()
                .name("Homework 2")
                .type(AssignmentType.HOMEWORK)
                .description("Read Chapters 6 and 9.")
                .deadline(LocalDate.of(2052, 1, 1))
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
        teacher = TeacherDto.builder()
                .firstname("Darrell")
                .lastname("Bowen")
                .email("darrellbowen@email.com")
                .address("3982 Turnpike Drive, Birmingham, AL 35203")
                .phone("619-446-8496")
                .birthdate("1984-02-01")
                .build();
        courseInput = CourseInput.builder()
                .name("Algebra")
                .build();
    }

    @Nested
    @DisplayName("POST methods")
    class PostMethodTests {
        @Test
        @DisplayName("when Assignment posted with valid parameters, should return created Assignment")
        public void whenAssignmentPostedWithValidParameters_shouldReturnCreatedAssignment() {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            assignmentInput1.setCourseId(courseId);
            ResponseEntity<AssignmentOutput> response = template.exchange(
                    linkToAssignments.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(assignmentInput1),
                    AssignmentOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            AssignmentOutput expected = AssignmentOutput.builder()
                    .id(response.getBody().getId())
                    .name(assignmentInput1.getName())
                    .type(assignmentInput1.getType())
                    .description(assignmentInput1.getDescription())
                    .deadline(assignmentInput1.getDeadline())
                    .course(new SimpleData(courseId, courseInput.getName()))
                    .build();

            assertThat(response.getBody()).isEqualTo(expected);
        }

        @Test
        @DisplayName("when Assignment posted with invalid parameter, should return response 'Bad Request'")
        public void whenAssignmentPostedWithInvalidParameter_shouldReturnResponseBadRequest() {
            givenAssignmentWithEmptyName_postAssignment_shouldReturnWithBadRequest();
            givenAssignmentWithPastDeadlineDate_postAssignment_shouldReturnWithBadRequest();
            givenAssignmentWithInvalidTeacherId_postAssignment_shouldReturnWithTeacherNotFound();
        }

        private void givenAssignmentWithEmptyName_postAssignment_shouldReturnWithBadRequest() {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId= postCourse(courseInput).getId();

            AssignmentInput inputWithBlankName = AssignmentInput.builder()
                    .name(" ")
                    .type(AssignmentType.TEST)
                    .deadline(LocalDate.of(2051, 1, 1))
                    .courseId(courseId)
                    .build();
            ResponseEntity<?> response = template.exchange(
                    linkToAssignments.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(inputWithBlankName),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        private void givenAssignmentWithPastDeadlineDate_postAssignment_shouldReturnWithBadRequest() {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            AssignmentInput inputWithWrongType = AssignmentInput.builder()
                    .name("Test")
                    .type(AssignmentType.TEST)
                    .deadline(LocalDate.of(1991, 1, 1))
                    .courseId(courseId)
                    .build();
            ResponseEntity<?> response = template.exchange(
                    linkToAssignments.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(inputWithWrongType),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        private void givenAssignmentWithInvalidTeacherId_postAssignment_shouldReturnWithTeacherNotFound() {
            AssignmentInput inputWithWrongType = AssignmentInput.builder()
                    .name("Test")
                    .type(AssignmentType.TEST)
                    .deadline(LocalDate.of(2051, 1, 1))
                    .courseId(99L)
                    .build();
            ResponseEntity<?> response = template.exchange(
                    linkToAssignments.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(inputWithWrongType),
                    String.class
            );

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
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(assignmentResource).isNotNull();
            assertThat(assignmentResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when Assignments posted, getAll should return list of Assignments")
        public void whenAssignmentsPosted_getAllShouldReturnListOfAssignments() {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            assignmentInput1.setCourseId(courseId);
            assignmentInput2.setCourseId(courseId);
            AssignmentOutput assignment1 = postAssignment(assignmentInput1);
            AssignmentOutput assignment2 = postAssignment(assignmentInput2);

            String urlToAssignments = String.format("http://localhost:%d/api/assignments", port);
            Traverson traverson = new Traverson(URI.create(urlToAssignments), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<AssignmentOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<AssignmentOutput> resources = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(resources).isNotNull();
            assertThat(resources.getContent()).isNotNull();
            assertThat(resources.getContent()).containsExactly(assignment1, assignment2);
        }

        @Test
        @DisplayName("when Assignment exists with given ID, getById should return Assignment")
        public void whenAssignmentExistsWithGivenId_getByIdShouldReturnAssignment() {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            assignmentInput1.setCourseId(courseId);
            long assignmentId = postAssignment(assignmentInput1).getId();

            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(assignmentId)).withSelfRel();
            ResponseEntity<AssignmentOutput> assignmentGetResponse = template.exchange(
                    linkToAssignment.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    AssignmentOutput.class
            );

            assertThat(assignmentGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(assignmentGetResponse.getBody()).isNotNull();
            AssignmentOutput expected = AssignmentOutput.builder()
                    .id(assignmentId)
                    .name(assignmentInput1.getName())
                    .type(assignmentInput1.getType())
                    .description(assignmentInput1.getDescription())
                    .deadline(assignmentInput1.getDeadline())
                    .course(new SimpleData(courseId, courseInput.getName()))
                    .build();
            assertThat(assignmentGetResponse.getBody()).isEqualTo(expected);
        }

        @Test
        @DisplayName("when Assignment does not exist with given ID, getById should return response 'Not Found'")
        public void whenAssignmentDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToAssignment.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PUT methods")
    @DirtiesContext(classMode = BEFORE_CLASS)
    class UpdateMethodTests {
        @Test
        @DisplayName("when Assignment exists with given ID, update should return updated Assignment")
        public void whenAssignmentExistsWithGivenId_updateShouldReturnUpdatedAssignment() {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            assignmentInput1.setCourseId(courseId);
            long assignmentId = postAssignment(assignmentInput1).getId();

            // update assignment
            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(assignmentId)).withSelfRel();
            AssignmentInput update = AssignmentInput.builder()
                    .name("Homework III")
                    .type(AssignmentType.HOMEWORK)
                    .deadline(LocalDate.of(2051, 1, 1))
                    .courseId(courseId)
                    .build();
            ResponseEntity<AssignmentOutput> response = template.exchange(
                    linkToAssignment.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(update),
                    AssignmentOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(assignmentId);
            assertThat(response.getBody().getName()).isEqualTo("Homework III");
        }

        @Test
        @DisplayName("when Assignment does not exist with given ID, update should return response 'Not Found'")
        public void whenAssignmentDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            assignmentInput1.setCourseId(courseId);
            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToAssignment.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(assignmentInput1),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Assignment updated with invalid parameter, update should return response 'Bad Request'")
        public void whenAssignmentUpdatedWithInvalidParameter_shouldReturnResponseBadRequest() {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            assignmentInput1.setCourseId(courseId
            );
            long assignmentId = postAssignment(assignmentInput1).getId();

            givenAssignmentWithEmptyName_updateAssignment_shouldReturnWithBadRequest(assignmentId);
            givenAssignmentWithPastDeadlineDate_updateAssignment_shouldReturnWithBadRequest(assignmentId);
            givenAssignmentWithInvalidTeacherId_updateAssignment_shouldReturnWithTeacherNotFound(assignmentId);
        }

        private void givenAssignmentWithEmptyName_updateAssignment_shouldReturnWithBadRequest(Long id) {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            assignmentInput1.setCourseId(courseId);
            AssignmentInput updateWithBlankName = AssignmentInput.builder()
                    .name(" ")
                    .type(AssignmentType.TEST)
                    .deadline(LocalDate.of(2051, 1, 1))
                    .courseId(courseId)
                    .build();

            Link linkToUpdate =
                    linkTo(methodOn(AssignmentController.class).update(updateWithBlankName, id)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToUpdate.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(updateWithBlankName),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        private void givenAssignmentWithPastDeadlineDate_updateAssignment_shouldReturnWithBadRequest(Long id) {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            assignmentInput1.setCourseId(courseId);
            AssignmentInput updateWithWrongType = AssignmentInput.builder()
                    .name("Test")
                    .type(AssignmentType.TEST)
                    .deadline(LocalDate.of(1991, 1, 1))
                    .courseId(courseId)
                    .build();
            Link linkToUpdate =
                    linkTo(methodOn(AssignmentController.class).update(updateWithWrongType, id)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToUpdate.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(updateWithWrongType),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        private void givenAssignmentWithInvalidTeacherId_updateAssignment_shouldReturnWithTeacherNotFound(Long id) {
            AssignmentInput updateWithWrongType = AssignmentInput.builder()
                    .name("Test")
                    .type(AssignmentType.TEST)
                    .deadline(LocalDate.of(2051, 1, 1))
                    .courseId(99L)
                    .build();
            Link linkToUpdate =
                    linkTo(methodOn(AssignmentController.class).update(updateWithWrongType, id)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToUpdate.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(updateWithWrongType),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DELETE methods")
    @DirtiesContext(classMode = BEFORE_CLASS)
    class DeleteMethodTests {
        @Test
        @DisplayName("when Assignment exists with given ID, delete should remove Assignment")
        public void whenAssignmentExistsWithGivenId_deleteShouldRemoveAssignment() {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            assignmentInput1.setCourseId(courseId);
            long id = postAssignment(assignmentInput1).getId();

            // delete assignment
            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(id)).withSelfRel();
            template.exchange(linkToAssignment.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    AssignmentOutput.class
            );

            // get assignment
            ResponseEntity<?> response = template.exchange(
                    linkToAssignment.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Assignment does not exist with given ID, delete should return response 'Not Found'")
        public void whenAssignmentDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() {
            Link linkToAssignment = linkTo(methodOn(AssignmentController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToAssignment.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Assignment is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
        public void whenAssignmentIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() {
            long teacherId = postTeacher(teacher).getId();
            courseInput.setTeacherId(teacherId);
            long courseId = postCourse(courseInput).getId();
            assignmentInput1.setCourseId(courseId);
            AssignmentOutput assignment = postAssignment(assignmentInput1);
            postEntryRelatedToAssignment(assignment);

            Link linkToAssignment =
                    linkTo(methodOn(AssignmentController.class).getById(assignment.getId())).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToAssignment.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    private TeacherDto postTeacher(TeacherDto teacher) {
        Link linkToTeachers = linkTo(TeacherController.class).withSelfRel();
        ResponseEntity<TeacherDto> postTeacherResponse = template.exchange(
                linkToTeachers.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(teacher),
                TeacherDto.class
        );

        assertThat(postTeacherResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postTeacherResponse.getBody()).isNotNull();

        return postTeacherResponse.getBody();
    }

    private CourseOutput postCourse(CourseInput course) {
        Link linkToCourses = linkTo(CourseController.class).withSelfRel();
        ResponseEntity<CourseOutput> coursePostResponse = template.exchange(
                linkToCourses.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(course),
                CourseOutput.class
        );
        assertThat(coursePostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(coursePostResponse.getBody()).isNotNull();

        return coursePostResponse.getBody();
    }

    private AssignmentOutput postAssignment(AssignmentInput assignment) {
        ResponseEntity<AssignmentOutput> assignmentPostResponse = template.exchange(
                linkToAssignments.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(assignment),
                AssignmentOutput.class
        );

        assertThat(assignmentPostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(assignmentPostResponse.getBody()).isNotNull();

        return assignmentPostResponse.getBody();
    }

    private void postEntryRelatedToAssignment(AssignmentOutput assignment) {
        // post student
        Link linkToStudents = linkTo(StudentController.class).withSelfRel();
        ResponseEntity<StudentDto> studentPostResponse = template.exchange(
                linkToStudents.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(student),
                StudentDto.class
        );
        assertThat(studentPostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(studentPostResponse.getBody()).isNotNull();
        student = studentPostResponse.getBody();

        // post course
        CourseOutput course = postCourse(courseInput);

        // add student to course
        Link linkToClassEnrollment =
                linkTo(methodOn(CourseController.class).addStudentToClass(course.getId(), student.getId())).withSelfRel();
        ResponseEntity<CourseOutput> courseAddedStudentPostResponse = template.exchange(
                linkToClassEnrollment.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(null),
                CourseOutput.class
        );
        assertThat(courseAddedStudentPostResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // post gradebook entry
        GradebookInput gradebookInput = GradebookInput.builder()
                .studentId(student.getId())
                .courseId(course.getId())
                .assignmentId(assignment.getId())
                .grade(2)
                .build();
        Link linkToGradeAssignment =
                linkTo(methodOn(GradebookController.class).gradeAssignment(gradebookInput)).withSelfRel();
        ResponseEntity<GradebookOutput> gradebookEntryPostResponse = template.exchange(
                linkToGradeAssignment.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(gradebookInput),
                GradebookOutput.class
        );

        assertThat(gradebookEntryPostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(gradebookEntryPostResponse.getBody()).isNotNull();
    }

}
