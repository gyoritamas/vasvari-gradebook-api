package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.AssignmentController;
import com.codecool.gradebookapi.controller.CourseController;
import com.codecool.gradebookapi.controller.GradebookController;
import com.codecool.gradebookapi.controller.StudentController;
import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.testmodel.AssignmentOutput;
import com.codecool.gradebookapi.testmodel.ClassOutput;
import com.codecool.gradebookapi.testmodel.GradebookOutput;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

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

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String studentBaseUrl;
    private String classBaseUrl;
    private String assignmentBaseUrl;

    private StudentDto student1;
    private StudentDto student2;
    private CourseInput clazz;
    private AssignmentInput assignment;
    private GradebookOutput gradebookOutput1;
    private GradebookOutput gradebookOutput2;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        studentBaseUrl = baseUrl + "/students";
        classBaseUrl = baseUrl + "/classes";
        assignmentBaseUrl = baseUrl + "/assignments";

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
        clazz = CourseInput.builder()
                .course("Algebra")
                .build();
        assignment = AssignmentInput.builder()
                .name("Homework 1")
                .type("HOMEWORK")
                .build();
        gradebookOutput1 = GradebookOutput.builder()
                .studentId(1L)
                .classId(1L)
                .assignmentId(1L)
                .grade(4)
                .build();
        gradebookOutput2 = GradebookOutput.builder()
                .studentId(2L)
                .classId(1L)
                .assignmentId(1L)
                .grade(5)
                .build();
    }

    @Nested
    @DirtiesContext(classMode = BEFORE_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("GET methods")
    class GetMethods {
        @Test
        @Order(1)
        @DisplayName("given empty database, getAll should return empty list")
        public void givenEmptyDatabase_getAllShouldReturnEmptyLIst() {
            String urlToEntries = String.format("http://localhost:%d/api/gradebook", port);
            Traverson traverson = new Traverson(URI.create(urlToEntries), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<GradebookOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<GradebookOutput> classResource = traverson
                    .follow("$._links.self.href")
                    .toObject(collectionModelType);

            assertThat(classResource).isNotNull();
            assertThat(classResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when entries posted, getAll should return list of entries")
        public void whenEntriesPosted_getAllShouldReturnListOfEntries() {
            Link linkToStudents = linkTo(StudentController.class).withSelfRel();
            long student1Id = template.postForObject(linkToStudents.getHref(), student1, StudentDto.class).getId();
            long student2Id = template.postForObject(linkToStudents.getHref(), student2, StudentDto.class).getId();
            Link linkToClasses = linkTo(CourseController.class).withSelfRel();
            long classId = template.postForObject(linkToClasses.getHref(), clazz, ClassOutput.class).getId();
            Link linkToAssignments = linkTo(AssignmentController.class).withSelfRel();
            long assignmentId =
                    template.postForObject(linkToAssignments.getHref(), assignment, AssignmentOutput.class).getId();
            classId = addStudentToClass(student1Id, classId).getId();
            classId = addStudentToClass(student2Id, classId).getId();

            GradebookInput gradebookInput1 = GradebookInput.builder()
                    .studentId(student1Id)
                    .classId(classId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            GradebookInput gradebookInput2 = GradebookInput.builder()
                    .studentId(student2Id)
                    .classId(classId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            Link linkToGradeAssignment =
                    linkTo(methodOn(GradebookController.class).gradeAssignment(gradebookInput1)).withSelfRel();
            GradebookOutput entry1Posted =
                    template.postForObject(linkToGradeAssignment.getHref(), gradebookInput1, GradebookOutput.class);
            GradebookOutput entry2Posted =
                    template.postForObject(linkToGradeAssignment.getHref(), gradebookInput2, GradebookOutput.class);

            String urlToGradebook = String.format("http://localhost:%d/api/gradebook", port);
            Traverson traverson = new Traverson(URI.create(urlToGradebook), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<GradebookOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<GradebookOutput> gradebookResource = traverson
                    .follow("$._links.self.href")
                    .toObject(collectionModelType);

            assertThat(gradebookResource).isNotNull();
            assertThat(gradebookResource.getContent()).isNotNull();
            assertThat(gradebookResource.getContent()).containsExactly(entry1Posted, entry2Posted);
        }

        @Test
        @DisplayName("when entry exists with given ID, getById should return entry")
        public void whenEntryExistsWithGivenId_getByIdShouldReturnEntry() {
            Link linkToStudents = linkTo(StudentController.class).withSelfRel();
            long studentId = template.postForObject(linkToStudents.getHref(), student1, StudentDto.class).getId();
            Link linkToClasses = linkTo(CourseController.class).withSelfRel();
            long classId = template.postForObject(linkToClasses.getHref(), clazz, ClassOutput.class).getId();
            Link linkToAssignments = linkTo(AssignmentController.class).withSelfRel();
            long assignmentId =
                    template.postForObject(linkToAssignments.getHref(), assignment, AssignmentOutput.class).getId();
            classId = addStudentToClass(studentId, classId).getId();

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .classId(classId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            Link linkToGradeAssignment =
                    linkTo(methodOn(GradebookController.class).gradeAssignment(gradebookInput)).withSelfRel();
            GradebookOutput entryPosted =
                    template.postForObject(linkToGradeAssignment.getHref(), gradebookInput, GradebookOutput.class);

            Link linkToGradebook = linkTo(methodOn(GradebookController.class).getById(entryPosted.getId())).withSelfRel();
            ResponseEntity<GradebookOutput> response =
                    template.getForEntity(linkToGradebook.getHref(), GradebookOutput.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(entryPosted);
        }

        @Test
        @DisplayName("when entry does not exist with given ID, getById should return response 'Not Found'")
        public void whenEntryDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link linkToGradebook = linkTo(methodOn(GradebookController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.getForEntity(linkToGradebook.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Student exists with given ID, getGradesOfStudent should return list of GradebookEntries")
        public void whenStudentExistsWithGivenId_getGradesOfStudentShouldReturnListOfEntries() {
            long student1Id = template.postForObject(studentBaseUrl, student1, StudentDto.class).getId();
            long student2Id = template.postForObject(studentBaseUrl, student2, StudentDto.class).getId();
            long classId = template.postForObject(classBaseUrl, clazz, ClassOutput.class).getId();
            long assignmentId =
                    template.postForObject(assignmentBaseUrl, assignment, AssignmentOutput.class).getId();
            classId = addStudentToClass(student1Id, classId).getId();
            classId = addStudentToClass(student2Id, classId).getId();
            GradebookInput gradebookInput1 = GradebookInput.builder()
                    .studentId(student1Id)
                    .classId(classId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            GradebookInput gradebookInput2 = GradebookInput.builder()
                    .studentId(student2Id)
                    .classId(classId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            GradebookOutput entry1Posted = template.postForObject(baseUrl + "/gradebook/", gradebookInput1, GradebookOutput.class);
            GradebookOutput entry2Posted = template.postForObject(baseUrl + "/gradebook/", gradebookInput2, GradebookOutput.class);

            String urlToEntriesOfStudent = String.format("http://localhost:%d/api/student_gradebook/%d", port, student1Id);
            Traverson traverson = new Traverson(URI.create(urlToEntriesOfStudent), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<GradebookOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<GradebookOutput> gradebookResource = traverson
                    .follow("$._links.student_gradebook.href")
                    .toObject(collectionModelType);

            assertThat(gradebookResource).isNotNull();
            assertThat(gradebookResource.getContent()).containsExactly(entry1Posted);
        }

        @Test
        @DisplayName("when Student does not exist with given ID, getGradesOfStudent should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_getGradesOfStudentShouldReturnResponseNotFound() {
            String requestUrl = baseUrl + "/student_gradebook/99";
            ResponseEntity<?> response = template.getForEntity(requestUrl, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Class exists with given ID, getGradesOfClass should return list of GradebookEntries")
        public void whenClassExistsWithGivenId_getGradesOfClassShouldReturnListOfEntries() {
            long student1Id = template.postForObject(studentBaseUrl, student1, StudentDto.class).getId();
            long student2Id = template.postForObject(studentBaseUrl, student2, StudentDto.class).getId();
            long classId = template.postForObject(classBaseUrl, clazz, ClassOutput.class).getId();
            long assignmentId =
                    template.postForObject(assignmentBaseUrl, assignment, AssignmentOutput.class).getId();
            classId = addStudentToClass(student1Id, classId).getId();
            classId = addStudentToClass(student2Id, classId).getId();

            GradebookInput gradebookInput1 = GradebookInput.builder()
                    .studentId(student1Id)
                    .classId(classId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            GradebookInput gradebookInput2 = GradebookInput.builder()
                    .studentId(student2Id)
                    .classId(classId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            Link linkToGradeAssignment =
                    linkTo(methodOn(GradebookController.class).gradeAssignment(gradebookInput1)).withSelfRel();
            GradebookOutput entry1 =
                    template.postForObject(linkToGradeAssignment.getHref(), gradebookInput1, GradebookOutput.class);
            GradebookOutput entry2 =
                    template.postForObject(linkToGradeAssignment.getHref(), gradebookInput2, GradebookOutput.class);

            String urlToEntriesOfStudent = String.format("http://localhost:%d/api/class_gradebook/%d", port, classId);
            Traverson traverson = new Traverson(URI.create(urlToEntriesOfStudent), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<GradebookOutput> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<GradebookOutput> gradebookResource = traverson
                    .follow("$._links.class_gradebook.href")
                    .toObject(collectionModelType);

            assertThat(gradebookResource).isNotNull();
            assertThat(gradebookResource.getContent()).containsExactly(entry1, entry2);
        }

        @Test
        @DisplayName("when class does not exist with given ID, getGradesOfStudent should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_getGradesOfStudentShouldReturnResponseNotFound() {
            Link linkToEntriesOfClass = linkTo(methodOn(GradebookController.class).getGradesOfClass(99L)).withSelfRel();
            ResponseEntity<?> response = template.getForEntity(linkToEntriesOfClass.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST methods")
    class PostMethods {
        @Test
        @DisplayName("when entities found with given IDs, gradeAssignment should return created GradebookEntry")
        public void whenEntitiesFoundWithGivenIds_gradeAssignmentShouldReturnCreatedGradebookEntry() {
            long studentId = template.postForObject(studentBaseUrl, student1, StudentDto.class).getId();
            long classId = template.postForObject(classBaseUrl, clazz, ClassOutput.class).getId();
            long assignmentId =
                    template.postForObject(assignmentBaseUrl, assignment, AssignmentOutput.class).getId();
            classId = addStudentToClass(studentId, classId).getId();

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .classId(classId)
                    .assignmentId(assignmentId)
                    .grade(4)
                    .build();
            ResponseEntity<GradebookOutput> response =
                    template.postForEntity(baseUrl + "/gradebook", gradebookInput, GradebookOutput.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(gradebookOutput1);
        }

        @Test
        @DisplayName("when an entry exists with the given IDs, gradeAssignment should return response 'Conflict'")
        public void whenAnEntryExistsWithTheGivenIds_gradeAssignmentShouldReturnResponseConflict() {
            long studentId = template.postForObject(studentBaseUrl, student2, StudentDto.class).getId();
            long classId = template.postForObject(classBaseUrl, clazz, ClassOutput.class).getId();
            long assignmentId = template.postForObject(assignmentBaseUrl, assignment, AssignmentOutput.class).getId();
            addStudentToClass(studentId, classId);
            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(studentId)
                    .classId(classId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            template.postForObject(baseUrl + "/gradebook", gradebookInput, GradebookOutput.class);

            ResponseEntity<?> response = template.postForEntity(baseUrl + "/gradebook", gradebookInput, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/invalid_entry_data.csv", numLinesToSkip = 1, delimiter = ';')
        @DisplayName("when GradebookInput has invalid parameters, gradeAssignment should return response 'Bad Request'")
        public void whenGradebookInputHasInvalidParameters_gradeAssignment_shouldReturnResponseBadRequest(
                @AggregateWith(GradebookIntegrationTests.GradebookInputAggregator.class) GradebookInput input) {

            ResponseEntity<?> response = template.postForEntity(baseUrl + "/gradebook", input, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("when Student does not exist with given ID, gradeAssignment should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_gradeAssignmentShouldReturnResponseNotFound() {
            long classId = template.postForObject(classBaseUrl, clazz, ClassOutput.class).getId();
            long assignmentId =
                    template.postForObject(assignmentBaseUrl, assignment, AssignmentOutput.class).getId();

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(99L)
                    .classId(classId)
                    .assignmentId(assignmentId)
                    .grade(5)
                    .build();
            ResponseEntity<?> response = template.postForEntity(baseUrl + "/gradebook", gradebookInput, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Class does not exist with given ID, gradeAssignment should return response 'Not Found'")
        public void whenClassDoesNotExistWithGivenId_gradeAssignmentShouldReturnResponseNotFound() {
            student1 = template.postForObject(studentBaseUrl, student1, StudentDto.class);
            AssignmentOutput assignmentPosted =
                    template.postForObject(assignmentBaseUrl, assignment, AssignmentOutput.class);

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(student1.getId())
                    .classId(99L)
                    .assignmentId(assignmentPosted.getId())
                    .grade(2)
                    .build();
            ResponseEntity<?> response = template.postForEntity(baseUrl + "/gradebook", gradebookInput, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Assignment does not exist with given ID, gradeAssignment should return response 'Not Found'")
        public void whenAssignmentDoesNotExistWithGivenId_gradeAssignmentShouldReturnResponseNotFound() {
            student1 = template.postForObject(studentBaseUrl, student1, StudentDto.class);
            ClassOutput classPosted = template.postForObject(classBaseUrl, clazz, ClassOutput.class);

            GradebookInput gradebookInput = GradebookInput.builder()
                    .studentId(student1.getId())
                    .classId(classPosted.getId())
                    .assignmentId(99L)
                    .grade(1)
                    .build();
            ResponseEntity<?> response = template.postForEntity(baseUrl + "/gradebook", gradebookInput, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    private ClassOutput addStudentToClass(Long studentId, Long classId) {
        Link linkToClassEnrollment =
                linkTo(methodOn(CourseController.class).addStudentToClass(classId, studentId)).withSelfRel();
        ResponseEntity<ClassOutput> response =
                template.postForEntity(linkToClassEnrollment.getHref(), null, ClassOutput.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        return response.getBody();
    }

    private static class GradebookInputAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
                throws ArgumentsAggregationException {
            return GradebookInput.builder()
                    .studentId(accessor.getLong(0))
                    .classId(accessor.getLong(1))
                    .assignmentId(accessor.getLong(2))
                    .grade(accessor.getInteger(3))
                    .build();
        }
    }
}
