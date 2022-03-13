package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.AssignmentController;
import com.codecool.gradebookapi.controller.CourseController;
import com.codecool.gradebookapi.controller.GradebookController;
import com.codecool.gradebookapi.controller.StudentController;
import com.codecool.gradebookapi.dto.*;
import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import com.codecool.gradebookapi.integration.util.AuthorizationManager;
import com.codecool.gradebookapi.testmodel.AssignmentOutput;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static com.codecool.gradebookapi.security.ApplicationUserRole.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = BEFORE_CLASS)
public class GradebookIntegrationTests {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private AuthorizationManager auth;

    @LocalServerPort
    private int port;

    private String baseUrl;

    private StudentDto student1;
    private StudentDto student2;
    private CourseInput course;
    private AssignmentInput assignment;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api";

        auth.setRole(ADMIN);

        student1 = StudentDto.builder()
                .firstname("John")
                .lastname("Doe")
                .gradeLevel(9)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate("2005-12-01")
                .build();
        student2 = StudentDto.builder()
                .firstname("Jane")
                .lastname("Doe")
                .gradeLevel(2)
                .email("janedoe@email.com")
                .address("9351 Morris St., Reisterstown, MD 21136")
                .phone("202-555-0198")
                .birthdate("1990-04-13")
                .build();
        course = CourseInput.builder()
                .name("Algebra")
                .build();
        assignment = AssignmentInput.builder()
                .name("Homework 1")
                .type("HOMEWORK")
                .build();
    }

    @Nested
    @DirtiesContext(classMode = BEFORE_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("GET methods")
    class GetMethodTests {
        @Test
        @Order(1)
        @DisplayName("given empty database, getAll should return empty list")
        public void givenEmptyDatabase_getAllShouldReturnEmptyList() {
            String urlToEntries = String.format("http://localhost:%d/api/gradebook", port);
            Traverson traverson = new Traverson(URI.create(urlToEntries), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<GradebookOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<GradebookOutput> classResource = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(classResource).isNotNull();
            assertThat(classResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when entries posted, getAll should return list of entries")
        public void whenEntriesPosted_getAllShouldReturnListOfEntries() {
            long student1Id = postStudent(student1).getId();
            long student2Id = postStudent(student2).getId();
            long courseId = postCourse(course).getId();
            long assignmentId = postAssignment(assignment).getId();

            courseId = addStudentToClass(student1Id, courseId).getId();
            courseId = addStudentToClass(student2Id, courseId).getId();

            GradebookInput gradebookInput1 = GradebookInput.builder()
                    .studentId(student1Id)
                    .courseId(courseId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            GradebookInput gradebookInput2 = GradebookInput.builder()
                    .studentId(student2Id)
                    .courseId(courseId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            GradebookOutput entry1Posted = postGradebookEntry(gradebookInput1);
            GradebookOutput entry2Posted = postGradebookEntry(gradebookInput2);

            String urlToGradebook = String.format("http://localhost:%d/api/gradebook", port);
            Traverson traverson = new Traverson(URI.create(urlToGradebook), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<GradebookOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<GradebookOutput> gradebookResource = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(gradebookResource).isNotNull();
            assertThat(gradebookResource.getContent()).isNotNull();
            assertThat(gradebookResource.getContent()).containsExactly(entry1Posted, entry2Posted);
        }

        @Test
        @DisplayName("when entry exists with given ID, getById should return entry")
        public void whenEntryExistsWithGivenId_getByIdShouldReturnEntry() {
            long studentId = postStudent(student1).getId();
            long classId = postCourse(course).getId();
            long assignmentId = postAssignment(assignment).getId();

            classId = addStudentToClass(studentId, classId).getId();

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .courseId(classId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            GradebookOutput entryPosted = postGradebookEntry(gradebookInput);

            Link linkToGradebook = linkTo(methodOn(GradebookController.class).getById(entryPosted.getId())).withSelfRel();
            ResponseEntity<GradebookOutput> response = template.exchange(
                    linkToGradebook.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    GradebookOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(entryPosted);
        }

        @Test
        @DisplayName("when entry does not exist with given ID, getById should return response 'Not Found'")
        public void whenEntryDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link linkToGradebook = linkTo(methodOn(GradebookController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToGradebook.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Student exists with given ID, getGradesOfStudent should return list of GradebookEntries")
        public void whenStudentExistsWithGivenId_getGradesOfStudentShouldReturnListOfEntries() {
            long student1Id = postStudent(student1).getId();
            long student2Id = postStudent(student2).getId();
            long classId = postCourse(course).getId();
            long assignmentId = postAssignment(assignment).getId();

            classId = addStudentToClass(student1Id, classId).getId();
            classId = addStudentToClass(student2Id, classId).getId();

            GradebookInput gradebookInput1 = GradebookInput.builder()
                    .studentId(student1Id)
                    .courseId(classId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            GradebookInput gradebookInput2 = GradebookInput.builder()
                    .studentId(student2Id)
                    .courseId(classId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            GradebookOutput entry1Posted = postGradebookEntry(gradebookInput1);
            GradebookOutput entry2Posted = postGradebookEntry(gradebookInput2);

            String urlToEntriesOfStudent = String.format("http://localhost:%d/api/student_gradebook/%d", port, student1Id);
            Traverson traverson = new Traverson(URI.create(urlToEntriesOfStudent), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<GradebookOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<GradebookOutput> gradebookResource = traverson
                    .follow("$._links.student_gradebook.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(gradebookResource).isNotNull();
            assertThat(gradebookResource.getContent()).containsExactly(entry1Posted);
        }

        @Test
        @DisplayName("when Student does not exist with given ID, getGradesOfStudent should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_getGradesOfStudentShouldReturnResponseNotFound() {
            String requestUrl = baseUrl + "/student_gradebook/99";
            ResponseEntity<?> response = template.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Class exists with given ID, getGradesOfClass should return list of GradebookEntries")
        public void whenClassExistsWithGivenId_getGradesOfClassShouldReturnListOfEntries() {
            long student1Id = postStudent(student1).getId();
            long student2Id = postStudent(student2).getId();
            long classId = postCourse(course).getId();
            long assignmentId = postAssignment(assignment).getId();

            classId = addStudentToClass(student1Id, classId).getId();
            classId = addStudentToClass(student2Id, classId).getId();

            GradebookInput gradebookInput1 = GradebookInput.builder()
                    .studentId(student1Id)
                    .courseId(classId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            GradebookInput gradebookInput2 = GradebookInput.builder()
                    .studentId(student2Id)
                    .courseId(classId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            GradebookOutput entry1 = postGradebookEntry(gradebookInput1);
            GradebookOutput entry2 = postGradebookEntry(gradebookInput2);

            String urlToEntriesOfStudent = String.format("http://localhost:%d/api/class_gradebook/%d", port, classId);
            Traverson traverson = new Traverson(URI.create(urlToEntriesOfStudent), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<GradebookOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<GradebookOutput> gradebookResource = traverson
                    .follow("$._links.class_gradebook.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(gradebookResource).isNotNull();
            assertThat(gradebookResource.getContent()).containsExactly(entry1, entry2);
        }

        @Test
        @DisplayName("when class does not exist with given ID, getGradesOfStudent should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_getGradesOfStudentShouldReturnResponseNotFound() {
            Link linkToEntriesOfClass = linkTo(methodOn(GradebookController.class).getGradesOfClass(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToEntriesOfClass.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST methods")
    class PostMethodTests {
        @Test
        @DisplayName("when entities found with given IDs, gradeAssignment should return created GradebookEntry")
        public void whenEntitiesFoundWithGivenIds_gradeAssignmentShouldReturnCreatedGradebookEntry() {
            long studentId = postStudent(student1).getId();
            long courseId = postCourse(course).getId();
            long assignmentId = postAssignment(assignment).getId();

            addStudentToClass(studentId, courseId);

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            ResponseEntity<GradebookOutput> response = template.exchange(
                    baseUrl + "/gradebook",
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(gradebookInput),
                    GradebookOutput.class
            );


            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            GradebookOutput expected = GradebookOutput.builder()
                    .id(response.getBody().getId())
                    .student(new SimpleData(studentId, student1.getName()))
                    .course(new SimpleData(courseId, course.getName()))
                    .assignment(new SimpleData(assignmentId, assignment.getName()))
                    .grade(4)
                    .build();

            assertThat(response.getBody()).isEqualTo(expected);
        }

        @Test
        @DisplayName("when an entry exists with the given IDs, gradeAssignment should return response 'Conflict'")
        public void whenAnEntryExistsWithTheGivenIds_gradeAssignmentShouldReturnResponseConflict() {
            long studentId = postStudent(student2).getId();
            long classId = postCourse(course).getId();
            long assignmentId = postAssignment(assignment).getId();
            addStudentToClass(studentId, classId);
            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .courseId(classId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            postGradebookEntry(gradebookInput);

            ResponseEntity<?> response = template.exchange(
                    baseUrl + "/gradebook",
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(gradebookInput),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/invalid_entry_data.csv", numLinesToSkip = 1, delimiter = ';')
        @DisplayName("when GradebookInput has invalid parameters, gradeAssignment should return response 'Bad Request'")
        public void whenGradebookInputHasInvalidParameters_gradeAssignment_shouldReturnResponseBadRequest(
                @AggregateWith(GradebookIntegrationTests.GradebookInputAggregator.class) GradebookInput input) {

            ResponseEntity<?> response = template.exchange(
                    baseUrl + "/gradebook",
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(input),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("when Student does not exist with given ID, gradeAssignment should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_gradeAssignmentShouldReturnResponseNotFound() {
            long classId = postCourse(course).getId();
            long assignmentId = postAssignment(assignment).getId();

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(99L)
                    .courseId(classId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            ResponseEntity<?> response = template.exchange(
                    baseUrl + "/gradebook",
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(gradebookInput),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Class does not exist with given ID, gradeAssignment should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_gradeAssignmentShouldReturnResponseNotFound() {
            student1 = postStudent(student1);
            AssignmentOutput assignmentPosted = postAssignment(assignment);

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(student1.getId())
                    .courseId(99L)
                    .assignmentId(assignmentPosted.getId())
                    .grade(2)
                    .build();
            ResponseEntity<?> response = template.exchange(
                    baseUrl + "/gradebook",
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(gradebookInput),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Assignment does not exist with given ID, gradeAssignment should return response 'Not Found'")
        public void whenAssignmentDoesNotExistWithGivenId_gradeAssignmentShouldReturnResponseNotFound() {
            long studentId = postStudent(student1).getId();
            long courseId = postCourse(course).getId();

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .assignmentId(99L)
                    .grade(1)
                    .build();
            ResponseEntity<?> response = template.exchange(
                    baseUrl + "/gradebook",
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(gradebookInput),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    private StudentDto postStudent(StudentDto student) {
        Link linkToStudents = linkTo(StudentController.class).withSelfRel();
        ResponseEntity<StudentDto> student1PostResponse = template.exchange(
                linkToStudents.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(student),
                StudentDto.class
        );

        assertThat(student1PostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(student1PostResponse.getBody()).isNotNull();

        return student1PostResponse.getBody();
    }

    private CourseOutput postCourse(CourseInput course) {
        Link linkToClasses = linkTo(CourseController.class).withSelfRel();
        ResponseEntity<CourseOutput> coursePostResponse = template.exchange(
                linkToClasses.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(course),
                CourseOutput.class
        );

        assertThat(coursePostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(coursePostResponse.getBody()).isNotNull();

        return coursePostResponse.getBody();
    }

    private AssignmentOutput postAssignment(AssignmentInput assignment) {
        Link linkToAssignments = linkTo(AssignmentController.class).withSelfRel();
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

    private GradebookOutput postGradebookEntry(GradebookInput entry) {
        Link linkToGradeAssignment =
                linkTo(methodOn(GradebookController.class).gradeAssignment(entry)).withSelfRel();
        ResponseEntity<GradebookOutput> entry1PostedResponse = template.exchange(
                linkToGradeAssignment.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(entry),
                GradebookOutput.class
        );

        assertThat(entry1PostedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(entry1PostedResponse.getBody()).isNotNull();

        return entry1PostedResponse.getBody();
    }

    private CourseOutput addStudentToClass(Long studentId, Long classId) {
        Link linkToClassEnrollment =
                linkTo(methodOn(CourseController.class).addStudentToClass(classId, studentId)).withSelfRel();
        ResponseEntity<CourseOutput> response = template.exchange(
                linkToClassEnrollment.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(null),
                CourseOutput.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    private static class GradebookInputAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
                throws ArgumentsAggregationException {
            return GradebookInput.builder()
                    .studentId(accessor.getLong(0))
                    .courseId(accessor.getLong(1))
                    .assignmentId(accessor.getLong(2))
                    .grade(accessor.getInteger(3))
                    .build();
        }
    }
}
