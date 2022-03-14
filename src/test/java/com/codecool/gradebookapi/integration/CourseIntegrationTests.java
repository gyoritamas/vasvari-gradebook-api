package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.*;
import com.codecool.gradebookapi.dto.*;
import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import com.codecool.gradebookapi.integration.util.AuthorizationManager;
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
import java.util.Collections;

import static com.codecool.gradebookapi.security.ApplicationUserRole.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(AuthorizationManager.class)
public class CourseIntegrationTests {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private AuthorizationManager auth;

    @LocalServerPort
    private int port;

    private Link linkToClasses;
    private Link linkToStudents;
    private Link linkToTeachers;

    private CourseInput courseInput1;
    private CourseInput courseInput2;
    private StudentDto student;
    private TeacherDto teacher;

    @BeforeEach
    public void setUp() {
        linkToClasses = linkTo(CourseController.class).withSelfRel();
        linkToStudents = linkTo(StudentController.class).withSelfRel();
        linkToTeachers = linkTo(TeacherController.class).withSelfRel();

        auth.setRole(ADMIN);

        courseInput1 = CourseInput.builder()
                .name("Algebra")
                .build();
        courseInput2 = CourseInput.builder()
                .name("Biology")
                .build();

        student = StudentDto.builder()
                .firstname("John")
                .lastname("Doe")
                .gradeLevel(11)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate("2004-02-01")
                .build();

        teacher = TeacherDto.builder()
                .firstname("Darrell")
                .lastname("Bowen")
                .email("darrellbowen@email.com")
                .address("3982 Turnpike Drive, Birmingham, AL 35203")
                .phone("619-446-8496")
                .birthdate("1984-02-01")
                .build();
    }

    @Nested
    @DisplayName("POST methods")
    class PostMethodTests {
        @Test
        @DisplayName("when Class posted with valid parameters, should return created Class")
        public void whenClassPostedWithValidParameters_shouldReturnCreatedClass() {
            ResponseEntity<CourseOutput> response = template.exchange(
                    linkToClasses.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(courseInput1),
                    CourseOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            CourseOutput expected = CourseOutput.builder()
                    .id(response.getBody().getId())
                    .name(courseInput1.getName())
                    .students(Collections.emptyList())
                    .build();
            assertThat(response.getBody()).isEqualTo(expected);
        }

        @Test
        @DisplayName("when Class posted with invalid parameter, should return response 'Bad Request'")
        public void whenClassPostedWithInvalidParameter_shouldReturnResponseBadRequest() {
            CourseInput inputWithBlankName = CourseInput.builder().name("  ").build();
            ResponseEntity<?> response = template.exchange(
                    linkToClasses.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(inputWithBlankName),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("add Student to Class should return Class with added Student")
        public void addStudentToClass_shouldReturnClassWithAddedStudent() {
            long studentId = postStudent(student).getId();
            long courseId = postCourse(courseInput1).getId();

            // add student to course
            Link linkToClassEnrollment =
                    linkTo(methodOn(CourseController.class).addStudentToClass(courseId, studentId)).withSelfRel();
            ResponseEntity<CourseOutput> response = template.exchange(
                    linkToClassEnrollment.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(null),
                    CourseOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStudents()).containsExactly(new SimpleData(studentId, student.getName()));
        }

        @Test
        @DisplayName("when Student does not exist with given ID, should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_shouldReturnResponseNotFound() {
            long courseId = postCourse(courseInput1).getId();

            // add nonexistent student to course
            Link linkToClassEnrollment =
                    linkTo(methodOn(CourseController.class).addStudentToClass(courseId, 99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClassEnrollment.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Class does not exist with given ID, should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_shouldReturnResponseNotFound() {
            long studentId = postStudent(student).getId();

            // add student to nonexistent course
            Link linkToClassEnrollment =
                    linkTo(methodOn(CourseController.class).addStudentToClass(99L, studentId)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClassEnrollment.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("setTeacherOfCourse should return Course with set teacher")
        public void setTeacherOfCourse_shouldReturnCourseWithGivenTeacherSetAsParameter() {
            long teacherId = postTeacher(teacher).getId();
            long courseId = postCourse(courseInput1).getId();

            // set teacher as teacher of course
            Link linkToSetTeacher =
                    linkTo(methodOn(CourseController.class).setTeacherOfCourse(courseId, teacherId)).withSelfRel();
            ResponseEntity<CourseOutput> response = template.exchange(
                    linkToSetTeacher.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(null),
                    CourseOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTeacher().getId()).isEqualTo(teacherId);
            assertThat(response.getBody().getTeacher().getName()).isEqualTo(teacher.getName());
        }

        @Test
        @DisplayName("when Teacher does not exist with given ID, should return response 'Not Found'")
        public void whenTeacherDoesNotExistWithGivenId_shouldReturnResponseNotFound() {
            long courseId = postCourse(courseInput1).getId();

            // set nonexistent teacher as teacher of course
            Link linkToSetTeacher =
                    linkTo(methodOn(CourseController.class).setTeacherOfCourse(courseId, 99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToSetTeacher.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Course does not exist with given ID, should return response 'Not Found'")
        public void whenCourseDoesNotExistWithGivenId_shouldReturnResponseNotFound() {
            long teacherId = postTeacher(teacher).getId();

            // set teacher as teacher of nonexistent course
            Link linkToSetTeacher =
                    linkTo(methodOn(CourseController.class).setTeacherOfCourse(99L, teacherId)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToSetTeacher.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

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
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(classResource).isNotNull();
            assertThat(classResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when Classes posted, getAll should return list of Classes")
        public void whenClassesPosted_getAllShouldReturnListOfClasses() {
            CourseOutput course1 = postCourse(courseInput1);
            CourseOutput course2 = postCourse(courseInput2);

            String urlToClasses = String.format("http://localhost:%d/api/classes", port);
            Traverson traverson = new Traverson(URI.create(urlToClasses), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<CourseOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<CourseOutput> classResource = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(classResource).isNotNull();
            assertThat(classResource.getContent()).containsExactly(course1, course2);
        }

        @Test
        @DisplayName("when Class exists with given ID, getById should return Class")
        public void whenClassExistsWithGivenId_getByIdShouldReturnClass() {
            CourseOutput coursePosted = postCourse(courseInput1);

            Link linkToClass = linkTo(methodOn(CourseController.class).getById(coursePosted.getId())).withSelfRel();
            ResponseEntity<CourseOutput> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    CourseOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(coursePosted);
        }

        @Test
        @DisplayName("when Class does not exist with given ID, getById should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClass.getHref(),
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
        @DisplayName("when Class exists with given ID and ClassInput parameters are valid, update should return updated Class")
        public void whenClassExistsWithGivenIdAndClassInputParametersAreValid_updateShouldReturnUpdatedClass() {
            long courseId = postCourse(courseInput1).getId();

            // update course
            CourseInput update = CourseInput.builder().name("Algebra II").build();
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(courseId)).withSelfRel();
            ResponseEntity<CourseOutput> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(update),
                    CourseOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(courseId);
            assertThat(response.getBody().getName()).isEqualTo("Algebra II");
        }

        @Test
        @DisplayName("when Class does not exist with given ID, update should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() {
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(courseInput1),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when ClassInput has invalid parameters, update should return response 'Bad Request'")
        public void whenClassInputHasInvalidParameters_updateShouldReturnResponseBadRequest() {
            long courseId = postCourse(courseInput1).getId();

            // update course
            CourseInput updateWithBlankName = CourseInput.builder().name(" ").build();
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(courseId)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(updateWithBlankName),
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
        @DisplayName("when Class exists with given ID, delete should remove Class")
        public void whenClassExistsWithGivenId_deleteShouldRemoveClass() {
            long courseId = postCourse(courseInput1).getId();

            // delete course
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(courseId)).withSelfRel();
            template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    CourseOutput.class
            );

            // get course
            ResponseEntity<?> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Class does not exist with given ID, delete should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() {
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Class is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
        public void whenClassIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() {
            CourseOutput course = postCourse(courseInput1);
            postEntryRelatedToClass(course);

            // delete course
            Link linkToClass = linkTo(methodOn(CourseController.class).getById(course.getId())).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    private StudentDto postStudent(StudentDto student) {
        ResponseEntity<StudentDto> postStudentResponse = template.exchange(
                linkToStudents.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(student),
                StudentDto.class
        );

        assertThat(postStudentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postStudentResponse.getBody()).isNotNull();

        return postStudentResponse.getBody();
    }

    private TeacherDto postTeacher(TeacherDto teacher) {
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
        ResponseEntity<CourseOutput> postCourseResponse = template.exchange(
                linkToClasses.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(course),
                CourseOutput.class
        );

        assertThat(postCourseResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postCourseResponse.getBody()).isNotNull();

        return postCourseResponse.getBody();
    }

    private void postEntryRelatedToClass(CourseOutput course) {
        // post assignment
        AssignmentInput assignmentInput = AssignmentInput.builder().name("Homework 1").type("HOMEWORK").build();
        Link linkToAssignments = linkTo(AssignmentController.class).withSelfRel();
        ResponseEntity<AssignmentOutput> assignmentPostResponse = template.exchange(
                linkToAssignments.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(assignmentInput),
                AssignmentOutput.class
        );
        assertThat(assignmentPostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(assignmentPostResponse.getBody()).isNotNull();
        AssignmentOutput assignment = assignmentPostResponse.getBody();

        student = postStudent(student);

        // add student to course
        Link linkToClassEnrollment =
                linkTo(methodOn(CourseController.class).addStudentToClass(course.getId(), student.getId())).withSelfRel();
        template.exchange(
                linkToClassEnrollment.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(null),
                CourseOutput.class
        );

        // post gradebook entry
        GradebookInput gradebookInput = GradebookInput.builder()
                .studentId(student.getId())
                .courseId(course.getId())
                .assignmentId(assignment.getId())
                .grade(3)
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
