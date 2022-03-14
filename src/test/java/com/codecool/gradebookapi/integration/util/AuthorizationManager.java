package com.codecool.gradebookapi.integration.util;

import com.codecool.gradebookapi.dto.UserDto;
import com.codecool.gradebookapi.jwt.JwtTokenUtil;
import com.codecool.gradebookapi.security.ApplicationUserRole;
import com.codecool.gradebookapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.codecool.gradebookapi.security.ApplicationUserRole.*;

@TestComponent
public class AuthorizationManager {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    private final UserDto ADMIN_USER;
    private final UserDto TEACHER_USER;
    private final UserDto STUDENT_USER;

    private HttpHeaders headersWithAuthorization;

    @Autowired
    public AuthorizationManager(JwtTokenUtil jwtTokenUtil, PasswordEncoder passwordEncoder, UserService userService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;

        ADMIN_USER = UserDto.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .role(ADMIN)
                .build();
        TEACHER_USER = UserDto.builder()
                .username("teacher")
                .password(passwordEncoder.encode("teacher"))
                .role(TEACHER)
                .build();
        STUDENT_USER = UserDto.builder()
                .username("student")
                .password(passwordEncoder.encode("student"))
                .role(STUDENT)
                .build();

        addDefaultUsers();
    }

    public void setRole(ApplicationUserRole role) {
        UserDetails user;
        switch (role) {
            case ADMIN:
                user = User.builder()
                        .username(ADMIN_USER.getUsername())
                        .password(ADMIN_USER.getPassword())
                        .authorities(ADMIN_USER.getRole().getGrantedAuthorities())
                        .build();
                break;
            case TEACHER:
                user = User.builder()
                        .username(TEACHER_USER.getUsername())
                        .password(TEACHER_USER.getPassword())
                        .authorities(TEACHER_USER.getRole().getGrantedAuthorities())
                        .build();
                break;
            case STUDENT:
                user = User.builder()
                        .username(STUDENT_USER.getUsername())
                        .password(STUDENT_USER.getPassword())
                        .authorities(STUDENT_USER.getRole().getGrantedAuthorities())
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

    private void addDefaultUsers() {
        userService.save(ADMIN_USER);
        userService.save(TEACHER_USER);
        userService.save(STUDENT_USER);
    }
}
