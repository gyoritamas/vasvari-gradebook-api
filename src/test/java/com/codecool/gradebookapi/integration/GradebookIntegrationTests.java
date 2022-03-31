package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.*;
import com.codecool.gradebookapi.dto.*;
import com.codecool.gradebookapi.dto.dataTypes.SimpleData;
import com.codecool.gradebookapi.dto.dataTypes.SimpleStudent;
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

import static com.codecool.gradebookapi.security.ApplicationUserRole.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = BEFORE_CLASS)
@Import(AuthorizationManager.class)
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
    private TeacherDto teacher;
    private SubjectInput subject;
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
                .birthdate(LocalDate.of(2005, 12, 1))
                .build();
        student2 = StudentDto.builder()
                .firstname("Jane")
                .lastname("Doe")
                .gradeLevel(2)
                .email("janedoe@email.com")
                .address("9351 Morris St., Reisterstown, MD 21136")
                .phone("202-555-0198")
                .birthdate(LocalDate.of(1990, 4, 13))
                .build();

        teacher = TeacherDto.builder()
                .firstname("Darrell")
                .lastname("Bowen")
                .email("darrellbowen@email.com")
                .address("3982 Turnpike Drive, Birmingham, AL 35203")
                .phone("619-446-8496")
                .birthdate(LocalDate.of(1984, 2, 1))
                .build();

        subject = SubjectInput.builder()
                .name("Algebra")
                .build();
        assignment = AssignmentInput.builder()
                .name("Homework 1")
                .type(AssignmentType.HOMEWORK)
                .deadline(LocalDate.of(2051, 1, 1))
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
            CollectionModel<GradebookOutput> subjectResource = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(subjectResource).isNotNull();
            assertThat(subjectResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when entries posted, getAll should return list of entries")
        public void whenEntriesPosted_getAllShouldReturnListOfEntries() {
            long student1Id = postStudent(student1).getId();
            long student2Id = postStudent(student2).getId();
            long teacherId = postTeacher(teacher).getId();
            subject.setTeacherId(teacherId);
            long subjectId = postSubject(subject).getId();
            assignment.setSubjectId(subjectId);
            long assignmentId = postAssignment(assignment).getId();

            subjectId = addStudentToSubject(student1Id, subjectId).getId();
            subjectId = addStudentToSubject(student2Id, subjectId).getId();

            GradebookInput gradebookInput1 = GradebookInput.builder()
                    .studentId(student1Id)
                    .subjectId(subjectId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            GradebookInput gradebookInput2 = GradebookInput.builder()
                    .studentId(student2Id)
                    .subjectId(subjectId)
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
            long teacherId = postTeacher(teacher).getId();
            subject.setTeacherId(teacherId);
            long subjectId = postSubject(subject).getId();
            assignment.setSubjectId(subjectId);
            long assignmentId = postAssignment(assignment).getId();

            subjectId = addStudentToSubject(studentId, subjectId).getId();

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .subjectId(subjectId)
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
            long teacherId = postTeacher(teacher).getId();
            subject.setTeacherId(teacherId);
            long subjectId = postSubject(subject).getId();
            assignment.setSubjectId(subjectId);
            long assignmentId = postAssignment(assignment).getId();

            subjectId = addStudentToSubject(student1Id, subjectId).getId();
            subjectId = addStudentToSubject(student2Id, subjectId).getId();

            GradebookInput gradebookInput1 = GradebookInput.builder()
                    .studentId(student1Id)
                    .subjectId(subjectId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            GradebookInput gradebookInput2 = GradebookInput.builder()
                    .studentId(student2Id)
                    .subjectId(subjectId)
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
        @DisplayName("when Subject exists with given ID, getGradesOfSubject should return list of GradebookEntries")
        public void whenSubjectExistsWithGivenId_getGradesOfSubjectShouldReturnListOfEntries() {
            long student1Id = postStudent(student1).getId();
            long student2Id = postStudent(student2).getId();
            long teacherId = postTeacher(teacher).getId();
            subject.setTeacherId(teacherId);
            long subjectId = postSubject(subject).getId();
            assignment.setSubjectId(subjectId);
            long assignmentId = postAssignment(assignment).getId();

            subjectId = addStudentToSubject(student1Id, subjectId).getId();
            subjectId = addStudentToSubject(student2Id, subjectId).getId();

            GradebookInput gradebookInput1 = GradebookInput.builder()
                    .studentId(student1Id)
                    .subjectId(subjectId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            GradebookInput gradebookInput2 = GradebookInput.builder()
                    .studentId(student2Id)
                    .subjectId(subjectId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            GradebookOutput entry1 = postGradebookEntry(gradebookInput1);
            GradebookOutput entry2 = postGradebookEntry(gradebookInput2);

            String urlToEntriesOfStudent = String.format("http://localhost:%d/api/subject_gradebook/%d", port, subjectId);
            Traverson traverson = new Traverson(URI.create(urlToEntriesOfStudent), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<GradebookOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<GradebookOutput> gradebookResource = traverson
                    .follow("$._links.subject_gradebook.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(gradebookResource).isNotNull();
            assertThat(gradebookResource.getContent()).containsExactly(entry1, entry2);
        }

        @Test
        @DisplayName("when Subject does not exist with given ID, getGradesOfStudent should return response 'Not Found'")
        public void whenSubjectDoesNotExistWithGivenId_getGradesOfStudentShouldReturnResponseNotFound() {
            Link linkToEntriesOfSubject = linkTo(methodOn(GradebookController.class).getGradesOfSubject(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToEntriesOfSubject.getHref(),
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
            long teacherId = postTeacher(teacher).getId();
            subject.setTeacherId(teacherId);
            long subjectId = postSubject(subject).getId();
            assignment.setSubjectId(subjectId);
            long assignmentId = postAssignment(assignment).getId();

            addStudentToSubject(studentId, subjectId);

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .subjectId(subjectId)
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

            SimpleStudent simpleStudent = SimpleStudent.builder()
                    .id(studentId)
                    .firstname(student1.getFirstname())
                    .lastname(student1.getLastname())
                    .build();
            GradebookOutput expected = GradebookOutput.builder()
                    .id(response.getBody().getId())
                    .student(simpleStudent)
                    .subject(new SimpleData(subjectId, subject.getName()))
                    .assignment(new SimpleData(assignmentId, assignment.getName()))
                    .grade(4)
                    .build();

            assertThat(response.getBody()).isEqualTo(expected);
        }

        @Test
        @DisplayName("when an entry exists with the given IDs, gradeAssignment should return response 'Conflict'")
        public void whenAnEntryExistsWithTheGivenIds_gradeAssignmentShouldReturnResponseConflict() {
            long studentId = postStudent(student2).getId();
            long teacherId = postTeacher(teacher).getId();
            subject.setTeacherId(teacherId);
            long subjectId = postSubject(subject).getId();
            assignment.setSubjectId(subjectId);
            long assignmentId = postAssignment(assignment).getId();
            addStudentToSubject(studentId, subjectId);
            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .subjectId(subjectId)
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
            long teacherId = postTeacher(teacher).getId();
            subject.setTeacherId(teacherId);
            long subjectId = postSubject(subject).getId();
            assignment.setSubjectId(subjectId);
            long assignmentId = postAssignment(assignment).getId();

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(99L)
                    .subjectId(subjectId)
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
        @DisplayName("when Subject does not exist with given ID, gradeAssignment should return response 'Not Found'")
        public void whenSubjectDoesNotExistWithGivenId_gradeAssignmentShouldReturnResponseNotFound() {
            student1 = postStudent(student1);
            long teacherId = postTeacher(teacher).getId();
            subject.setTeacherId(teacherId);
            long subjectId = postSubject(subject).getId();
            assignment.setSubjectId(subjectId);
            AssignmentOutput assignmentPosted = postAssignment(assignment);

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(student1.getId())
                    .subjectId(99L)
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
            long teacherId = postTeacher(teacher).getId();
            subject.setTeacherId(teacherId);
            long subjectId = postSubject(subject).getId();

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .subjectId(subjectId)
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
        Link linkToStudents = linkTo(methodOn(StudentController.class).add(student)).withSelfRel();
        ResponseEntity<StudentDto> studentPostResponse = template.exchange(
                linkToStudents.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(student),
                StudentDto.class
        );

        assertThat(studentPostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(studentPostResponse.getBody()).isNotNull();

        return studentPostResponse.getBody();
    }

    private TeacherDto postTeacher(TeacherDto teacher) {
        Link linkToTeachers = linkTo(TeacherController.class).withSelfRel();
        ResponseEntity<TeacherDto> teacherPostResponse = template.exchange(
                linkToTeachers.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(teacher),
                TeacherDto.class
        );

        assertThat(teacherPostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(teacherPostResponse.getBody()).isNotNull();

        return teacherPostResponse.getBody();
    }

    private SubjectOutput postSubject(SubjectInput subject) {
        Link linkToSubjects = linkTo(methodOn(SubjectController.class).add(subject)).withSelfRel();
        ResponseEntity<SubjectOutput> subjectPostResponse = template.exchange(
                linkToSubjects.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(subject),
                SubjectOutput.class
        );

        assertThat(subjectPostResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(subjectPostResponse.getBody()).isNotNull();

        return subjectPostResponse.getBody();
    }

    private AssignmentOutput postAssignment(AssignmentInput assignment) {
        Link linkToAssignments = linkTo(methodOn(AssignmentController.class).add(assignment)).withSelfRel();
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

    private SubjectOutput addStudentToSubject(Long studentId, Long subjectId) {
        Link link = linkTo(methodOn(SubjectController.class).addStudentToSubject(subjectId, studentId)).withSelfRel();
        ResponseEntity<SubjectOutput> response = template.exchange(
                link.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(null),
                SubjectOutput.class
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
                    .subjectId(accessor.getLong(1))
                    .assignmentId(accessor.getLong(2))
                    .grade(accessor.getInteger(3))
                    .build();
        }
    }
}
