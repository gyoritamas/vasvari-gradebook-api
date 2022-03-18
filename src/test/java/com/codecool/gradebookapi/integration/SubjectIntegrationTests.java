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
import java.util.Collections;

import static com.codecool.gradebookapi.security.ApplicationUserRole.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(AuthorizationManager.class)
public class SubjectIntegrationTests {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private AuthorizationManager auth;

    @LocalServerPort
    private int port;

    private SubjectInput subjectInput1;
    private SubjectInput subjectInput2;
    private StudentDto student;
    private TeacherDto teacher;

    @BeforeEach
    public void setUp() {
        auth.setRole(ADMIN);

        subjectInput1 = SubjectInput.builder()
                .name("Algebra")
                .build();
        subjectInput2 = SubjectInput.builder()
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
            long teacherId = postTeacher(teacher).getId();
            subjectInput1.setTeacherId(teacherId);
            Link linkToSubjects = linkTo(methodOn(SubjectController.class).add(subjectInput1)).withSelfRel();
            ResponseEntity<SubjectOutput> response = template.exchange(
                    linkToSubjects.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(subjectInput1),
                    SubjectOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            SubjectOutput expected = SubjectOutput.builder()
                    .id(response.getBody().getId())
                    .name(subjectInput1.getName())
                    .teacher(
                            new SimpleData(teacherId, teacher.getName())
                    )
                    .students(Collections.emptyList())
                    .build();
            assertThat(response.getBody()).isEqualTo(expected);
        }

        @Test
        @DisplayName("when Class posted with invalid parameter, should return response 'Bad Request'")
        public void whenClassPostedWithInvalidParameter_shouldReturnResponseBadRequest() {
            SubjectInput inputWithBlankName = SubjectInput.builder().name("  ").build();
            Link linkToSubjects = linkTo(methodOn(SubjectController.class).add(inputWithBlankName)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToSubjects.getHref(),
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
            long teacherId = postTeacher(teacher).getId();
            subjectInput1.setTeacherId(teacherId);
            long subjectId = postSubject(subjectInput1).getId();

            // add student to subject
            Link linkToClassEnrollment =
                    linkTo(methodOn(SubjectController.class).addStudentToSubject(subjectId, studentId)).withSelfRel();
            ResponseEntity<SubjectOutput> response = template.exchange(
                    linkToClassEnrollment.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(null),
                    SubjectOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStudents()).containsExactly(new SimpleData(studentId, student.getName()));
        }

        @Test
        @DisplayName("when Student does not exist with given ID, should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_shouldReturnResponseNotFound() {
            long teacherId = postTeacher(teacher).getId();
            subjectInput1.setTeacherId(teacherId);
            long subjectId = postSubject(subjectInput1).getId();

            // add nonexistent student to subject
            Link linkToClassEnrollment =
                    linkTo(methodOn(SubjectController.class).addStudentToSubject(subjectId, 99L)).withSelfRel();
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

            // add student to nonexistent subject
            Link linkToClassEnrollment =
                    linkTo(methodOn(SubjectController.class).addStudentToSubject(99L, studentId)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClassEnrollment.getHref(),
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
            String urlToClasses = String.format("http://localhost:%d/api/subjects", port);
            Traverson traverson = new Traverson(URI.create(urlToClasses), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<SubjectOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<SubjectOutput> classResource = traverson
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
            long teacherId = postTeacher(teacher).getId();
            subjectInput1.setTeacherId(teacherId);
            subjectInput2.setTeacherId(teacherId);
            SubjectOutput subject1 = postSubject(subjectInput1);
            SubjectOutput subject2 = postSubject(subjectInput2);

            String urlToClasses = String.format("http://localhost:%d/api/subjects", port);
            Traverson traverson = new Traverson(URI.create(urlToClasses), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<SubjectOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<SubjectOutput> classResource = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(classResource).isNotNull();
            assertThat(classResource.getContent()).containsExactly(subject1, subject2);
        }

        @Test
        @DisplayName("when Class exists with given ID, getById should return Class")
        public void whenClassExistsWithGivenId_getByIdShouldReturnClass() {
            long teacherId = postTeacher(teacher).getId();
            subjectInput1.setTeacherId(teacherId);
            SubjectOutput subjectPosted = postSubject(subjectInput1);

            Link linkToClass = linkTo(methodOn(SubjectController.class).getById(subjectPosted.getId())).withSelfRel();
            ResponseEntity<SubjectOutput> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    SubjectOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(subjectPosted);
        }

        @Test
        @DisplayName("when Class does not exist with given ID, getById should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link linkToClass = linkTo(methodOn(SubjectController.class).getById(99L)).withSelfRel();
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
            long teacherId = postTeacher(teacher).getId();
            subjectInput1.setTeacherId(teacherId);
            long subjectId = postSubject(subjectInput1).getId();

            // update subject
            SubjectInput update = SubjectInput.builder().name("Algebra II").teacherId(teacherId).build();
            Link linkToClass = linkTo(methodOn(SubjectController.class).getById(subjectId)).withSelfRel();
            ResponseEntity<SubjectOutput> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(update),
                    SubjectOutput.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(subjectId);
            assertThat(response.getBody().getName()).isEqualTo("Algebra II");
        }

        @Test
        @DisplayName("when Class does not exist with given ID, update should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() {
            long teacherId = postTeacher(teacher).getId();
            subjectInput1.setTeacherId(teacherId);
            Link linkToClass = linkTo(methodOn(SubjectController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(subjectInput1),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when ClassInput has invalid parameters, update should return response 'Bad Request'")
        public void whenClassInputHasInvalidParameters_updateShouldReturnResponseBadRequest() {
            long teacherId = postTeacher(teacher).getId();
            subjectInput1.setTeacherId(teacherId);
            long subjectId = postSubject(subjectInput1).getId();

            // update subject
            SubjectInput updateWithBlankName = SubjectInput.builder().name(" ").build();
            Link linkToClass = linkTo(methodOn(SubjectController.class).getById(subjectId)).withSelfRel();
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
            long teacherId = postTeacher(teacher).getId();
            subjectInput1.setTeacherId(teacherId);
            long subjectId = postSubject(subjectInput1).getId();

            // delete subject
            Link linkToClass = linkTo(methodOn(SubjectController.class).getById(subjectId)).withSelfRel();
            template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    SubjectOutput.class
            );

            // get subject
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
            Link linkToClass = linkTo(methodOn(SubjectController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToClass.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Subject is used by a GradebookEntry, delete should return response 'Method Not Allowed'")
        public void whenSubjectIsUsedByAnEntry_deleteShouldReturnResponseMethodNotAllowed() {
            long teacherId = postTeacher(teacher).getId();
            subjectInput1.setTeacherId(teacherId);
            SubjectOutput subject = postSubject(subjectInput1);
            postEntryRelatedToSubject(subject);

            // delete subject
            Link linkToClass = linkTo(methodOn(SubjectController.class).getById(subject.getId())).withSelfRel();
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
        Link linkToStudents = linkTo(methodOn(StudentController.class).add(student)).withSelfRel();
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
        Link linkToTeachers = linkTo(methodOn(TeacherController.class).add(teacher)).withSelfRel();
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

    private SubjectOutput postSubject(SubjectInput subject) {
        Link linkToSubjects = linkTo(methodOn(SubjectController.class).add(subject)).withSelfRel();
        ResponseEntity<SubjectOutput> postSubjectResponse = template.exchange(
                linkToSubjects.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(subject),
                SubjectOutput.class
        );

        assertThat(postSubjectResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postSubjectResponse.getBody()).isNotNull();

        return postSubjectResponse.getBody();
    }

    private void postEntryRelatedToSubject(SubjectOutput subject) {
        // post assignment
        AssignmentInput assignmentInput = AssignmentInput.builder()
                .name("Homework 1")
                .type(AssignmentType.HOMEWORK)
                .deadline(LocalDate.of(2051, 1, 1))
                .subjectId(subject.getId())
                .build();
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

        // post student
        student = postStudent(student);

        // add student to subject
        Link linkToClassEnrollment =
                linkTo(methodOn(SubjectController.class).addStudentToSubject(subject.getId(), student.getId())).withSelfRel();
        template.exchange(
                linkToClassEnrollment.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(null),
                SubjectOutput.class
        );

        // post gradebook entry
        GradebookInput gradebookInput = GradebookInput.builder()
                .studentId(student.getId())
                .subjectId(subject.getId())
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
