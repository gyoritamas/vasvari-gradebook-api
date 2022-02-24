package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.controller.*;
import com.codecool.gradebookapi.testmodel.TeacherDto;
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
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class TeacherIntegrationTests {
    @Autowired
    private TestRestTemplate template;

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
        public void whenStudentPostedWithValidParameters_shouldReturnCreatedStudent() {
            ResponseEntity<TeacherDto> response = template.postForEntity(baseLink.getHref(), teacher1, TeacherDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(teacher1);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/invalid_teacher_data.csv", numLinesToSkip = 1, delimiter = ';')
        @DisplayName("when Teacher posted with invalid parameters, should return response 'Bad Request'")
        public void whenTeacherPostedWithInvalidParameters_shouldReturnResponseBadRequest(
                @AggregateWith(TeacherAggregator.class) TeacherDto teacher) {
            ResponseEntity<?> response = template.postForEntity(baseLink.getHref(), teacher, String.class);

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
                    .toObject(collectionModelType);

            assertThat(teacherResource).isNotNull();
            assertThat(teacherResource.getContent()).isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("when Teachers posted, getAll should return list of Teachers")
        public void whenTeachersPosted_getAllShouldReturnListOfTeachers() {
            template.postForObject(baseLink.getHref(), teacher1, TeacherDto.class);
            template.postForObject(baseLink.getHref(), teacher2, TeacherDto.class);

            Traverson traverson = new Traverson(URI.create(baseUrl), MediaTypes.HAL_JSON);
            TypeReferences.CollectionModelType<TeacherDto> collectionModelType =
                    new TypeReferences.CollectionModelType<>() {
                    };
            CollectionModel<TeacherDto> teacherResource = traverson
                    .follow("$._links.self.href")
                    .toObject(collectionModelType);

            assertThat(teacherResource).isNotNull();
            assertThat(teacherResource.getContent()).isNotNull();
            assertThat(teacherResource.getContent()).hasSize(2);
            assertThat(teacherResource.getContent()).containsExactly(teacher1, teacher2);
        }

        @Test
        @DisplayName("when Teacher exists with given ID, getById should return Teacher")
        public void whenStudentExistsWithGivenId_getByIdShouldReturnStudent() {
            long teacherId = template.postForObject(baseLink.getHref(), teacher1, TeacherDto.class).getId();

            Link link = linkTo(methodOn(TeacherController.class).getById(teacherId)).withSelfRel();
            ResponseEntity<TeacherDto> response = template.getForEntity(link.getHref(), TeacherDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEqualTo(teacher1);
        }

        @Test
        @DisplayName("when Teacher does not exist with given ID, getById should return response 'Not Found'")
        public void whenTeacherDoesNotExistWithGivenId_getByIdShouldReturnResponseNotFound() {
            Link link = linkTo(methodOn(TeacherController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response = template.getForEntity(link.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }

    @Nested
    @DisplayName("PUT methods")
    class PutMethodTests {
        @Test
        @DisplayName("when Teacher exists with given ID, update should return updated Teacher")
        public void whenTeacherExistsWithGivenId_updateShouldReturnUpdatedTeacher() {
            TeacherDto teacherPosted = template.postForObject(baseUrl, teacher1, TeacherDto.class);
            teacherPosted.setFirstname("James");
            HttpEntity<TeacherDto> teacherHttpEntity = createHttpEntityWithMediaTypeJson(teacherPosted);

            Link linkToUpdateTeacher = linkTo(methodOn(TeacherController.class)
                    .getById(teacherPosted.getId()))
                    .withSelfRel();
            ResponseEntity<TeacherDto> response =
                    template.exchange(linkToUpdateTeacher.getHref(), HttpMethod.PUT, teacherHttpEntity, TeacherDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(teacherPosted.getId());
            assertThat(response.getBody().getFirstname()).isEqualTo("James");
        }

        @Test
        @DisplayName("when Teacher does not exist with given ID, update should return response 'Not Found'")
        public void whenStudentDoesNotExistWithGivenId_updateShouldReturnResponseNotFound() {
            Link linkToTeacher = linkTo(methodOn(TeacherController.class)
                    .getById(99L))
                    .withSelfRel();
            HttpEntity<TeacherDto> teacherHttpEntity = createHttpEntityWithMediaTypeJson(teacher1);
            ResponseEntity<?> response =
                    template.exchange(linkToTeacher.getHref(), HttpMethod.PUT, teacherHttpEntity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/invalid_teacher_data.csv", numLinesToSkip = 1, delimiter = ';')
        @DisplayName("when Teacher updated with invalid parameters, should return response 'Bad Request'")
        public void whenTeacherUpdatedWithInvalidParameters_shouldReturnResponseBadRequest(
                @AggregateWith(TeacherAggregator.class) TeacherDto teacher) {
            long teacherId = template.postForObject(baseUrl, teacher2, TeacherDto.class).getId();

            Link linkToTeacher = linkTo(methodOn(TeacherController.class).getById(teacherId)).withSelfRel();
            HttpEntity<TeacherDto> httpEntity = createHttpEntityWithMediaTypeJson(teacher);
            ResponseEntity<?> response =
                    template.exchange(linkToTeacher.getHref(), HttpMethod.PUT, httpEntity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DELETE methods")
    class DeleteMethodTests {
        @Test
        @DisplayName("when Teacher exists with given ID, delete should remove Teacher")
        public void whenTeacherExistsWithGivenId_deleteShouldRemoveTeacher() {
            long teacherId = template.postForObject(baseUrl, teacher1, TeacherDto.class).getId();

            Link linkToTeacher = linkTo(methodOn(TeacherController.class).getById(teacherId)).withSelfRel();
            template.exchange(linkToTeacher.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
            ResponseEntity<?> response = template.getForEntity(linkToTeacher.getHref(), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("when Teacher does not exist with given ID, delete should return response 'Not Found'")
        public void whenTeacherDoesNotExistWithGivenId_deleteShouldReturnResponseNotFound() {
            Link linkToTeacher = linkTo(methodOn(TeacherController.class).getById(99L)).withSelfRel();
            ResponseEntity<?> response =
                    template.exchange(linkToTeacher.getHref(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }

    private HttpEntity<TeacherDto> createHttpEntityWithMediaTypeJson(TeacherDto teacher) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(teacher, headers);
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
