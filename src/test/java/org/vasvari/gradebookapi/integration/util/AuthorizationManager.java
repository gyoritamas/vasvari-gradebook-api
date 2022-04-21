package org.vasvari.gradebookapi.integration.util;

import org.vasvari.gradebookapi.jwt.JwtTokenUtil;
import org.vasvari.gradebookapi.security.ApplicationUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.vasvari.gradebookapi.security.ApplicationUserRole.*;

@TestComponent
@Import(DefaultUsersManager.class)
public class AuthorizationManager {

    private final JwtTokenUtil jwtTokenUtil;

    private HttpHeaders headersWithAuthorization;

    @Autowired
    public AuthorizationManager(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public void setRole(ApplicationUserRole role) {
        UserDetails user;
        switch (role) {
            case ADMIN:
                user = User.builder()
                        .username("admin")
                        .password("admin")
                        .authorities(ADMIN.getGrantedAuthorities())
                        .build();
                break;
            case TEACHER:
                user = User.builder()
                        .username("teacher")
                        .password("teacher")
                        .authorities(TEACHER.getGrantedAuthorities())
                        .build();
                break;
            case STUDENT:
                user = User.builder()
                        .username("student")
                        .password("student")
                        .authorities(STUDENT.getGrantedAuthorities())
                        .build();
                break;
            default:
                throw new RuntimeException("Unexpected ApplicationUserRole type");
        }
        headersWithAuthorization = createAuthorizationHeader(user);
    }

    public <T> HttpEntity<T> createHttpEntityWithAuthorization(T object) {
        HttpHeaders headers = headersWithAuthorization;
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(object, headers);
    }

    private HttpHeaders createAuthorizationHeader(UserDetails userDetails) {
        String token = jwtTokenUtil.generateToken(userDetails);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return headers;
    }

    public HttpHeaders getHeadersWithAuthorization() {
        return headersWithAuthorization;
    }

}
