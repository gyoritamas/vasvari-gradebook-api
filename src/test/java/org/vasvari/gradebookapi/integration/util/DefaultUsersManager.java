package org.vasvari.gradebookapi.integration.util;

import org.vasvari.gradebookapi.dto.UserDto;
import org.vasvari.gradebookapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.vasvari.gradebookapi.security.ApplicationUserRole.*;

@TestComponent
public class DefaultUsersManager {

    private final UserService userService;

    private final UserDto ADMIN_USER;
    private final UserDto TEACHER_USER;
    private final UserDto STUDENT_USER;

    @Autowired
    public DefaultUsersManager(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;

        ADMIN_USER = UserDto.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .role(ADMIN)
                .enabled(true)
                .build();
        TEACHER_USER = UserDto.builder()
                .username("teacher")
                .password(passwordEncoder.encode("teacher"))
                .role(TEACHER)
                .enabled(true)
                .build();
        STUDENT_USER = UserDto.builder()
                .username("student")
                .password(passwordEncoder.encode("student"))
                .role(STUDENT)
                .enabled(true)
                .build();

        addDefaultUsers();
    }

    private void addDefaultUsers() {
        userService.save(ADMIN_USER);
        userService.save(TEACHER_USER);
        userService.save(STUDENT_USER);
    }

}
