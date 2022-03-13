package com.codecool.gradebookapi.integration.util;

import com.codecool.gradebookapi.jwt.JwtTokenUtil;
import com.codecool.gradebookapi.model.ApplicationUser;
import com.codecool.gradebookapi.repository.UserRepository;
import com.codecool.gradebookapi.security.ApplicationUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.codecool.gradebookapi.security.ApplicationUserRole.*;

@Component
public class AuthorizationManager {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    private final ApplicationUser ADMIN_USER;
    private final ApplicationUser TEACHER_USER;
    private final ApplicationUser STUDENT_USER;

    private HttpHeaders headersWithAuthorization;

    @Autowired
    public AuthorizationManager(JwtTokenUtil jwtTokenUtil, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;

        ADMIN_USER = ApplicationUser.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .role(ADMIN)
                .build();
        TEACHER_USER = ApplicationUser.builder()
                .username("teacher")
                .password(passwordEncoder.encode("teacher"))
                .role(TEACHER)
                .build();
        STUDENT_USER = ApplicationUser.builder()
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
        userRepository.deleteAll();
        userRepository.save(ADMIN_USER);
        userRepository.save(TEACHER_USER);
        userRepository.save(STUDENT_USER);
    }
}
