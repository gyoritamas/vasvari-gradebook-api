package com.codecool.gradebookapi.integration;

import com.codecool.gradebookapi.integration.util.DefaultUsersManager;
import com.codecool.gradebookapi.jwt.JwtAuthenticationController;
import com.codecool.gradebookapi.jwt.JwtRequest;
import com.codecool.gradebookapi.jwt.JwtResponse;
import com.codecool.gradebookapi.jwt.JwtTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.Link;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(DefaultUsersManager.class)
public class JwtAuthenticationIntegrationTests {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Test
    @DisplayName("given correct username and password, authentication token is created")
    public void givenCorrectUsernameAndPassword_authenticationTokenIsCreated() {
        JwtRequest request = new JwtRequest("admin", "admin");
        Link linkToAuthenticate =
                linkTo(methodOn(JwtAuthenticationController.class).createAuthenticationToken(request)).withSelfRel();
        ResponseEntity<JwtResponse> response = template.postForEntity(
                linkToAuthenticate.getHref(),
                createHttpEntity(request),
                JwtResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getJwtToken()).isNotNull();

        String token = response.getBody().getJwtToken();
        assertThat(jwtTokenUtil.getUsernameFromToken(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("given wrong password, createAuthenticationToken returns with HttpResponse 'Bad Request'")
    public void givenWrongPassword_createAuthenticationToken_returnsWithHttpResponseBadRequest() {
        JwtRequest request = new JwtRequest("admin", "wrong_password");
        Link linkToAuthenticate =
                linkTo(methodOn(JwtAuthenticationController.class).createAuthenticationToken(request)).withSelfRel();
        ResponseEntity<JwtResponse> response = template.postForEntity(
                linkToAuthenticate.getHref(),
                createHttpEntity(request),
                JwtResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("given wrong username, createAuthenticationToken returns with HttpResponse 'Bad Request'")
    public void givenWrongUsername_createAuthenticationToken_returnsWithHttpResponseBadRequest() {
        JwtRequest request = new JwtRequest("wrong_username", "admin");
        Link linkToAuthenticate =
                linkTo(methodOn(JwtAuthenticationController.class).createAuthenticationToken(request)).withSelfRel();
        ResponseEntity<JwtResponse> response = template.postForEntity(
                linkToAuthenticate.getHref(),
                createHttpEntity(request),
                JwtResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private <T> HttpEntity<T> createHttpEntity(T object) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(object, headers);
    }
}
