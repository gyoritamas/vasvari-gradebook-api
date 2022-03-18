package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.*;
import com.codecool.gradebookapi.dto.*;
import com.codecool.gradebookapi.integration.util.AuthorizationManager;
import com.codecool.gradebookapi.model.AssignmentType;
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
public class StudentIntegrationTests {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private AuthorizationManager auth;

    @LocalServerPort
    private int port;

    private String baseUrl;

    private StudentDto student1;
    private StudentDto student2;

    private TeacherDto teacher;

    @BeforeEach
    public void setUp() {
        this.baseUrl = "http://localhost:" + port + "/api/students";

        auth.setRole(ADMIN);

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
        @DisplayName("when Student posted with valid parameters, should return created Student")
        public void whenStudentPostedWithValidParameters_shouldReturnCreatedStudent() {
            Link linkToStudents = linkTo(methodOn(StudentController.class).add(student1)).withSelfRel();
            ResponseEntity<StudentDto> response = template.exchange(
                    linkToStudents.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(student1),
                    StudentDto.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            StudentDto expected = student1;
            expected.setId(response.getBody().getId());

            assertThat(response.getBody()).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/invalid_student_data.csv", numLinesToSkip = 1, delimiter = ';')
        @DisplayName("when Student posted with invalid parameters, should return response 'Bad Request'")
        public void whenStudentPostedWithInvalidParameters_shouldReturnResponseBadRequest(
                @AggregateWith(StudentAggregator.class) StudentDto student) {
            Link linkToStudents = linkTo(methodOn(StudentController.class).add(student)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToStudents.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(student),
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
            Traverson traverson = new Traverson(URI.create(baseUrl), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<StudentDto> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<StudentDto> studentResource = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(studentResource).isNotNull();
            assertThat(studentResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when Students posted, getAll should return list of Students")
        public void whenStudentsPosted_getAllShouldReturnListOfStudents() {
            student1 = postStudent(student1);
            student2 = postStudent(student2);

            Traverson traverson = new Traverson(URI.create(baseUrl), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<StudentDto> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<StudentDto> studentResource = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(studentResource).isNotNull();
            assertThat(studentResource.getContent()).isNotNull();
            assertThat(studentResource.getContent()).hasSize(2);
            assertThat(studentResource.getContent()).containsExactly(student1, student2);
        }

        @Test
        @DisplayName("when Student exists with given ID, getById should return Student")
        public void whenStudentExistsWithGivenId_getByIdShouldReturnStudent() {
            student1 = postStudent(student1);

            Link link = linkTo(methodOn(StudentController.class).getById(student1.getId())).withSelfRel();
            ResponseEntity<StudentDto> getResponse = template.exchange(
                    link.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    StudentDto.class
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).isNotNull();
            assertThat(getResponse.getBody()).isEqualTo(student1);
        }

        @Test
        @DisplayName("when Student does not exist with given ID, getById should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link link = linkTo(methodOn(StudentController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    link.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Student exists with given ID, getClassesOfStudent should return list of Classes")
        public void whenStudentExistsWithGivenId_getClassesOfStudentShouldReturnListOfClasses() {
            student1 = postStudent(student1);
            student2 = postStudent(student2);
            long teacherId = postTeacher(teacher).getId();

            CourseInput courseInput1 = CourseInput.builder().name("Algebra").teacherId(teacherId).build();
            CourseInput courseInput2 = CourseInput.builder().name("Biology").teacherId(teacherId).build();
            CourseOutput courseOutput1 = postCourse(courseInput1);
            CourseOutput courseOutput2 = postCourse(courseInput2);

            Link linkToClassEnrollment1 = linkTo(methodOn(CourseController.class)
                    .addStudentToClass(courseOutput1.getId(), student1.getId()))
                    .withSelfRel();
            Link linkToClassEnrollment2 = linkTo(methodOn(CourseController.class)
                    .addStudentToClass(courseOutput2.getId(), student1.getId()))
                    .withSelfRel();

            ResponseEntity<CourseOutput> course1Student1AddedPostResponse = template.exchange(
                    linkToClassEnrollment1.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(null),
                    CourseOutput.class
            );
            ResponseEntity<CourseOutput> course2Student1AddedPostResponse = template.exchange(
                    linkToClassEnrollment2.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(null),
                    CourseOutput.class
            );

            assertThat(course1Student1AddedPostResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(course1Student1AddedPostResponse.getBody()).isNotNull();
            assertThat(course2Student1AddedPostResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(course2Student1AddedPostResponse.getBody()).isNotNull();

            courseOutput1 = course1Student1AddedPostResponse.getBody();
            courseOutput2 = course2Student1AddedPostResponse.getBody();

            String urlToClassesOfStudent = String.format("http://localhost:%d/api/students/%d/classes", port, student1.getId());
            Traverson traverson = new Traverson(URI.create(urlToClassesOfStudent), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<CourseOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<CourseOutput> classResource = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(classResource).isNotNull();
            assertThat(classResource.getContent()).isNotNull();
            assertThat(classResource.getContent()).containsExactly(courseOutput1, courseOutput2);
        }

        @Test
        @DisplayName("when student does not exist with given ID, getClassesOfStudent should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_getClassesOfStudentShouldReturnResponseNotFound() {
            Link linkToClassesOfStudent = linkTo(methodOn(StudentController.class)
                    .getClassesOfStudent(99L))
                    .withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClassesOfStudent.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PUT methods")
    class PutMethodTests {
        @Test
        @DisplayName("when Student exists with given ID, update should return updated Student")
        public void whenStudentExistsWithGivenId_updateShouldReturnUpdatedStudent() {
            student1 = postStudent(student1);

            // update student
            student1.setFirstname("James");
            Link linkToUpdateStudent = linkTo(methodOn(StudentController.class)
                    .getById(student1.getId()))
                    .withSelfRel();
            ResponseEntity<StudentDto> response = template.exchange(
                    linkToUpdateStudent.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(student1),
                    StudentDto.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(student1.getId());
            assertThat(response.getBody().getFirstname()).isEqualTo("James");
        }

        @Test
        @DisplayName("when Student does not exist with given ID, update should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() {
            Link linkToStudent = linkTo(methodOn(StudentController.class)
                    .getById(99L))
                    .withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToStudent.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(student1),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/invalid_student_data.csv", numLinesToSkip = 1, delimiter = ';')
        @DisplayName("when Student updated with invalid parameters, should return response 'Bad Request'")
        public void whenStudentUpdatedWithInvalidParameters_shouldReturnResponseBadRequest(
                @AggregateWith(StudentAggregator.class) StudentDto student) {
            long studentId = postStudent(student2).getId();

            // update student
            Link linkToStudent = linkTo(methodOn(StudentController.class).getById(studentId)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToStudent.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(student),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DELETE methods")
    class DeleteMethodTests {
        @Test
        @DisplayName("when Student exists with given ID, delete should remove Student")
        public void whenStudentExistsWithGivenId_deleteShouldRemoveStudent() {
            long studentId = postStudent(student1).getId();

            // delete student
            Link linkToStudent = linkTo(methodOn(StudentController.class).getById(studentId)).withSelfRel();
            template.exchange(
                    linkToStudent.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            // get student
            ResponseEntity<?> getResponse = template.exchange(
                    linkToStudent.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Student does not exist with given ID, delete should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() {
            Link linkToStudent = linkTo(methodOn(StudentController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> deleteResponse = template.exchange(
                    linkToStudent.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Student is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
        public void whenStudentIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() {
            student1 = postStudent(student1);
            postEntryRelatedToStudent(student1);

            // delete student
            Link linkToStudent = linkTo(methodOn(StudentController.class).getById(student1.getId())).withSelfRel();
            ResponseEntity<?> deleteResponse = template.exchange(
                    linkToStudent.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    private TeacherDto postTeacher(TeacherDto teacher) {
        Link linkToTeachers = linkTo(TeacherController.class).withSelfRel();
        ResponseEntity<TeacherDto> postResponse = template.exchange(
                linkToTeachers.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(teacher),
                TeacherDto.class
        );

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody()).isNotNull();

        return postResponse.getBody();
    }

    private StudentDto postStudent(StudentDto student) {
        Link linkToStudents = linkTo(methodOn(StudentController.class).add(student)).withSelfRel();
        ResponseEntity<StudentDto> postResponse = template.exchange(
                linkToStudents.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(student),
                StudentDto.class
        );

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody()).isNotNull();

        return postResponse.getBody();
    }

    private CourseOutput postCourse(CourseInput course) {
        Link linkToClasses = linkTo(methodOn(CourseController.class).add(course)).withSelfRel();
        ResponseEntity<CourseOutput> postResponse = template.exchange(
                linkToClasses.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(course),
                CourseOutput.class
        );

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody()).isNotNull();

        return postResponse.getBody();
    }

    private void postEntryRelatedToStudent(StudentDto student) {
        // post teacher
        long teacherId = postTeacher(teacher).getId();

        // post course
        CourseInput course = CourseInput.builder().name("Algebra").teacherId(teacherId).build();
        CourseOutput coursePosted = postCourse(course);

        // post assignment
        AssignmentInput assignment = AssignmentInput.builder()
                .name("Homework 1")
                .type(AssignmentType.HOMEWORK)
                .deadline(LocalDate.of(2051, 1, 1))
                .courseId(coursePosted.getId())
                .build();
        Link linkToAssignments = linkTo(AssignmentController.class).withSelfRel();
        ResponseEntity<AssignmentOutput> assignmentPostResponse = template.exchange(
                linkToAssignments.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(assignment),
                AssignmentOutput.class
        );
        assertThat(assignmentPostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(assignmentPostResponse.getBody()).isNotNull();
        AssignmentOutput assignmentPosted = assignmentPostResponse.getBody();

        // add student to course
        Link linkToClassEnrollment = linkTo(methodOn(CourseController.class)
                .addStudentToClass(coursePosted.getId(), student.getId()))
                .withSelfRel();
        template.exchange(
                linkToClassEnrollment.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(null),
                CourseOutput.class
        );

        // post gradebook entry
        GradebookInput gradebookInput = GradebookInput.builder()
                .studentId(student.getId())
                .courseId(coursePosted.getId())
                .assignmentId(assignmentPosted.getId())
                .grade(5)
                .build();
        Link linkToGradeAssignment = linkTo(methodOn(GradebookController.class)
                .gradeAssignment(gradebookInput))
                .withSelfRel();
        ResponseEntity<GradebookOutput> entryPostResponse = template.exchange(
                linkToGradeAssignment.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(gradebookInput),
                GradebookOutput.class
        );

        assertThat(entryPostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(entryPostResponse.getBody()).isNotNull();
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
