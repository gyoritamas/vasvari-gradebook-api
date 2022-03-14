package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.TeacherController;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.integration.util.AuthorizationManager;
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

import static com.codecool.gradebookapi.security.ApplicationUserRole.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(AuthorizationManager.class)
public class TeacherIntegrationTests {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private AuthorizationManager auth;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private Link baseLink;

    private TeacherDto teacher1;
    private TeacherDto teacher2;

    @BeforeEach
    public void setUp() {
        this.baseUrl = "http://localhost:" + port + "/api/teachers";
        baseLink = linkTo(TeacherController.class).withRel("teachers");

        auth.setRole(ADMIN);

        teacher1 = TeacherDto.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate("1985-12-01")
                .build();
        teacher2 = TeacherDto.builder()
                .firstname("Jane")
                .lastname("Doe")
                .email("janedoe@email.com")
                .address("9351 Morris St., Reisterstown, MD 21136")
                .phone("202-555-9810")
                .birthdate("1987-04-13")
                .build();
    }

    @Nested
    @DisplayName("POST methods")
    class PostMethodTests {
        @Test
        @DisplayName("when Teacher posted with valid parameters, should return created Teacher")
        public void whenTeacherPostedWithValidParameters_shouldReturnCreatedTeacher() {
            ResponseEntity<TeacherDto> response = template.exchange(
                    baseLink.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(teacher1),
                    TeacherDto.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            TeacherDto expected = teacher1;
            expected.setId(response.getBody().getId());

            assertThat(response.getBody()).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/invalid_teacher_data.csv", numLinesToSkip = 1, delimiter = ';')
        @DisplayName("when Teacher posted with invalid parameters, should return response 'Bad Request'")
        public void whenTeacherPostedWithInvalidParameters_shouldReturnResponseBadRequest(
                @AggregateWith(TeacherAggregator.class) TeacherDto teacher) {
            ResponseEntity<?> response = template.exchange(
                    baseLink.getHref(),
                    HttpMethod.POST,
                    auth.createHttpEntityWithAuthorization(teacher),
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
            TypeReferences.CollectionModelType<TeacherDto> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<TeacherDto> teacherResource = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(teacherResource).isNotNull();
            assertThat(teacherResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when Teachers posted, getAll should return list of Teachers")
        public void whenTeachersPosted_getAllShouldReturnListOfTeachers() {
            teacher1 = postTeacher(teacher1);
            teacher2 = postTeacher(teacher2);

            Traverson traverson = new Traverson(URI.create(baseUrl), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<TeacherDto> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<TeacherDto> teacherResource = traverson
                    .follow("$._links.self.href")
                    .withHeaders(auth.getHeadersWithAuthorization())
                    .toObject(collectionModelType);

            assertThat(teacherResource).isNotNull();
            assertThat(teacherResource.getContent()).isNotNull();
            assertThat(teacherResource.getContent()).hasSize(2);
            assertThat(teacherResource.getContent()).containsExactly(teacher1, teacher2);
        }

        @Test
        @DisplayName("when Teacher exists with given ID, getById should return Teacher")
        public void whenTeacherExistsWithGivenId_getByIdShouldReturnTeacher() {
            long teacherId = postTeacher(teacher1).getId();

            Link link = linkTo(methodOn(TeacherController.class).getById(teacherId)).withSelfRel();
            ResponseEntity<TeacherDto> getResponse = template.exchange(
                    link.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    TeacherDto.class
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).isNotNull();

            TeacherDto expected = teacher1;
            expected.setId(getResponse.getBody().getId());

            assertThat(getResponse.getBody()).isEqualTo(expected);
        }

        @Test
        @DisplayName("when Teacher does not exist with given ID, getById should return response 'Not Found'")
        public void whenTeacherDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link link = linkTo(methodOn(TeacherController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    link.getHref(),
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
        @DisplayName("when Teacher exists with given ID, update should return updated Teacher")
        public void whenTeacherExistsWithGivenId_updateShouldReturnUpdatedTeacher() {
            teacher1 = postTeacher(teacher1);

            // update teacher
            teacher1.setFirstname("James");
            Link linkToUpdateTeacher = linkTo(methodOn(TeacherController.class)
                    .getById(teacher1.getId()))
                    .withSelfRel();
            ResponseEntity<TeacherDto> putResponse = template.exchange(
                    linkToUpdateTeacher.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(teacher1),
                    TeacherDto.class
            );

            assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(putResponse.getBody()).isNotNull();
            assertThat(putResponse.getBody().getId()).isEqualTo(teacher1.getId());
            assertThat(putResponse.getBody().getFirstname()).isEqualTo("James");
        }

        @Test
        @DisplayName("when Teacher does not exist with given ID, update should return response 'Not Found'")
        public void whenTeacherDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() {
            Link linkToTeacher = linkTo(methodOn(TeacherController.class)
                    .getById(99L))
                    .withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToTeacher.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(teacher1),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/invalid_teacher_data.csv", numLinesToSkip = 1, delimiter = ';')
        @DisplayName("when Teacher updated with invalid parameters, should return response 'Bad Request'")
        public void whenTeacherUpdatedWithInvalidParameters_shouldReturnResponseBadRequest(
                @AggregateWith(TeacherAggregator.class) TeacherDto teacher) {
            long teacherId = postTeacher(teacher2).getId();

            // update teacher
            Link linkToTeacher = linkTo(methodOn(TeacherController.class).getById(teacherId)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToTeacher.getHref(),
                    HttpMethod.PUT,
                    auth.createHttpEntityWithAuthorization(teacher),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DELETE methods")
    class DeleteMethodTests {
        @Test
        @DisplayName("when Teacher exists with given ID, delete should remove Teacher")
        public void whenTeacherExistsWithGivenId_deleteShouldRemoveTeacher() {
            long teacherId = postTeacher(teacher1).getId();

            // delete teacher
            Link linkToTeacher = linkTo(methodOn(TeacherController.class).getById(teacherId)).withSelfRel();
            template.exchange(
                    linkToTeacher.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            // get teacher
            ResponseEntity<?> response = template.exchange(
                    linkToTeacher.getHref(),
                    HttpMethod.GET,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Teacher does not exist with given ID, delete should return response 'Not Found'")
        public void whenTeacherDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() {
            Link linkToTeacher = linkTo(methodOn(TeacherController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.exchange(
                    linkToTeacher.getHref(),
                    HttpMethod.DELETE,
                    auth.createHttpEntityWithAuthorization(null),
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }

    private TeacherDto postTeacher(TeacherDto teacher) {
        ResponseEntity<TeacherDto> postResponse = template.exchange(
                baseLink.getHref(),
                HttpMethod.POST,
                auth.createHttpEntityWithAuthorization(teacher),
                TeacherDto.class
        );

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody()).isNotNull();

        return postResponse.getBody();
    }

    private static class TeacherAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
                throws ArgumentsAggregationException {
            return TeacherDto.builder()
                    .firstname(accessor.getString(0))
                    .lastname(accessor.getString(1))
                    .email(accessor.getString(2))
                    .address(accessor.getString(3))
                    .phone(accessor.getString(4))
                    .birthdate(accessor.getString(5))
                    .build();
        }
    }

}
