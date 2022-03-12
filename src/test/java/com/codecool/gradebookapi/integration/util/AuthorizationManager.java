package com.codecool.gradebookapi.integration.util;

import com.codecool.gradebookapi.jwt.JwtTokenUtil;
import com.codecool.gradebookapi.security.ApplicationUserRole;
import com.codecool.gradebookapi.testmodel.TeacherDto;
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

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private HttpHeaders headersWithAuthorization;

    public void setRole(ApplicationUserRole role) {
        final UserDetails adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .authorities(ADMIN.getGrantedAuthorities())
                .build();

        final UserDetails teacherUser = User.builder()
                .username("teacher")
                .password(passwordEncoder.encode("teacher"))
                .authorities(TEACHER.getGrantedAuthorities())
                .build();

        final UserDetails studentUser = User.builder()
                .username("student")
                .password(passwordEncoder.encode("student"))
                .authorities(STUDENT.getGrantedAuthorities())
                .build();

        switch (role) {
            case ADMIN:
                headersWithAuthorization = createAuthorizationHeader(adminUser);
                break;
            case TEACHER:
                headersWithAuthorization = createAuthorizationHeader(teacherUser);
                break;
            case STUDENT:
                headersWithAuthorization = createAuthorizationHeader(studentUser);
                break;
            default:
                throw new RuntimeException("Unexpected ApplicationUserRole type");
        }
    }

    public HttpEntity<TeacherDto> createHttpEntityWithAuthorization(TeacherDto teacher) {
        HttpHeaders headers = headersWithAuthorization;
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(teacher, headers);
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
