package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.UserDto;
import com.codecool.gradebookapi.dto.dataTypes.InitialCredentials;
import com.codecool.gradebookapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.codecool.gradebookapi.security.ApplicationUserRole.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

@SpringBootTest
public class UserServiceTests {
    @Autowired
    private UserService userService;

    private UserDto adminUser;
    private UserDto teacherUser;
    private UserDto studentUser;

    @BeforeEach
    void setUp() {
        adminUser = UserDto.builder()
                .username("admin")
                .password("admin")
                .role(ADMIN)
                .build();
        teacherUser = UserDto.builder()
                .username("teacher")
                .password("teacher")
                .role(TEACHER)
                .build();
        studentUser = UserDto.builder()
                .username("student")
                .password("student")
                .role(STUDENT)
                .build();
    }

    @Test
    @DisplayName("save should return saved user")
    public void saveShouldReturnSavedUser() {
        UserDto userSaved = userService.save(adminUser);

        assertThat(userSaved.getId()).isNotNull();
        assertThat(userSaved.getUsername()).isEqualTo(adminUser.getUsername());
        assertThat(userSaved.getPassword()).isEqualTo(adminUser.getPassword());
        assertThat(userSaved.getRole()).isEqualTo(ADMIN);
    }

    @Test
    @DisplayName("findAll should return list of users")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void findAll_shouldReturnListOfUsers() {
        UserDto adminUser = userService.save(this.adminUser);
        UserDto teacherUser = userService.save(this.teacherUser);
        UserDto studentUser = userService.save(this.studentUser);

        List<UserDto> users = userService.findAll();

        assertThat(users).containsExactly(adminUser, teacherUser, studentUser);
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void createStudentUser_shouldReturnCreatedStudentUsersInitialCredentials() {
        StudentDto student = StudentDto.builder()
                .id(79L)
                .firstname("John")
                .lastname("Doe")
                .build();
        InitialCredentials credentials = userService.createStudentUser(student);

        Pattern usernamePattern = Pattern
                .compile(student.getName().replaceAll(" ", "").toLowerCase() + "\\d{2}");
        assertThat(credentials.getUsername()).matches(usernamePattern);
        assertThat(credentials.getPassword()).matches("([a-zA-Z0-9]){" + UserService.PASSWORD_LENGTH + "}");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenStudentAlreadyHasUserAccount_createStudentUserShouldThrowException() {
        StudentDto student = StudentDto.builder()
                .id(79L)
                .firstname("John")
                .lastname("Doe")
                .build();
        userService.createStudentUser(student);

        assertThatThrownBy(() -> userService.createStudentUser(student))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Already has an account");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void createTeacherUser_shouldReturnCreatedTeacherUsersInitialCredentials() {
        TeacherDto teacher = TeacherDto.builder()
                .id(44L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();
        InitialCredentials credentials = userService.createTeacherUser(teacher);

        Pattern usernamePattern = Pattern
                .compile(teacher.getName().replaceAll(" ", "").toLowerCase() + "\\d{2}");
        assertThat(credentials.getUsername()).matches(usernamePattern);
        assertThat(credentials.getPassword()).matches("([a-zA-Z0-9]){" + UserService.PASSWORD_LENGTH + "}");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenTeacherAlreadyHasUserAccount_createTeacherUserShouldThrowException() {
        TeacherDto teacher = TeacherDto.builder()
                .id(44L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();
        userService.createTeacherUser(teacher);

        assertThatThrownBy(() -> userService.createTeacherUser(teacher))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Already has an account");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUsernameNotInUse_createAdminUserShouldReturnAdminUsersInitialCredentials() {
        InitialCredentials credentials = userService.createAdminUser("admin");

        assertThat(credentials.getUsername()).isEqualTo("admin");
        assertThat(credentials.getPassword()).matches("([a-zA-Z0-9]){" + UserService.PASSWORD_LENGTH + "}");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUsernameIsInUse_createAdminUserShouldThrowException() {
        userService.createAdminUser("admin");

        assertThatThrownBy(() -> userService.createAdminUser("admin"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Username already taken");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserExistsWithId_findByIdShouldReturnUser() {
        studentUser = userService.save(studentUser);
        long userId = studentUser.getId();

        Optional<UserDto> userFound = userService.findById(userId);

        assertThat(userFound).isPresent();
        assertThat(userFound.get()).isEqualTo(studentUser);
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserDoesNotExistWithId_findByIdShouldReturnEmptyOptional() {
        Optional<UserDto> userFound = userService.findById(99L);

        assertThat(userFound).isEmpty();
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserExistsWithUsername_findByUsernameShouldReturnUser() {
        teacherUser = userService.save(teacherUser);

        Optional<UserDto> userFound = userService.findByUsername(teacherUser.getUsername());

        assertThat(userFound).isPresent();
        assertThat(userFound.get()).isEqualTo(teacherUser);
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserDoesNotExistWithUsername_findByUsernameShouldReturnEmptyOptional() {
        Optional<UserDto> userFound = userService.findByUsername("anonymous");

        assertThat(userFound).isEmpty();
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserExistsWithId_deleteByIdShouldDeleteUser() {
        adminUser = userService.save(adminUser);
        long userId = adminUser.getId();

        userService.deleteById(userId);

        assertThat(userService.findById(userId)).isEmpty();
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserExistsWithId_findStudentIdByUserId_shouldReturnStudentId() {
        StudentDto student = StudentDto.builder()
                .id(79L)
                .firstname("John")
                .lastname("Doe")
                .build();
        InitialCredentials credentials = userService.createStudentUser(student);
        Optional<UserDto> user = userService.findByUsername(credentials.getUsername());

        assertThat(user).isPresent();

        long userId = user.get().getId();
        long studentId = userService.findStudentIdByUserId(userId);

        assertThat(studentId).isEqualTo(79L);
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserDoesNotExistWithId_findStudentIdByUserId_shouldThrowException() {
        assertThatThrownBy(() -> userService.findStudentIdByUserId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No school actor related to user exists");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserExistsWithId_findTeacherIdByUserId_shouldReturnTeacherId() {
        TeacherDto teacher = TeacherDto.builder()
                .id(44L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();
        InitialCredentials credentials = userService.createTeacherUser(teacher);
        Optional<UserDto> user = userService.findByUsername(credentials.getUsername());

        assertThat(user).isPresent();

        long userId = user.get().getId();
        long teacherId = userService.findTeacherIdByUserId(userId);

        assertThat(teacherId).isEqualTo(44L);
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserDoesNotExistWithId_findTeacherIdByUserId_shouldThrowException() {
        assertThatThrownBy(() -> userService.findTeacherIdByUserId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No school actor related to user exists");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserIsStudentAndHasAnAccount_getStudentIdOfCurrentUser_shouldReturnStudentId() {
        StudentDto student = StudentDto.builder()
                .id(79L)
                .firstname("John")
                .lastname("Doe")
                .build();
        InitialCredentials credentials = userService.createStudentUser(student);
        setCurrentUser(credentials.getUsername());

        long studentId = userService.getStudentIdOfCurrentUser();

        assertThat(studentId).isEqualTo(79L);
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserIsAdmin_getStudentIdOfCurrentUser_shouldThrowException() {
        InitialCredentials credentials = userService.createAdminUser("admin");
        setCurrentUser(credentials.getUsername());

        assertThatThrownBy(() -> userService.getStudentIdOfCurrentUser())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No school actor related to user exists");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserIsTeacher_getStudentIdOfCurrentUser_shouldThrowException() {
        TeacherDto teacher = TeacherDto.builder()
                .id(44L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();
        InitialCredentials credentials = userService.createTeacherUser(teacher);
        setCurrentUser(credentials.getUsername());

        assertThatThrownBy(() -> userService.getStudentIdOfCurrentUser())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User role is incorrect");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserIsTeacherAndHasAnAccount_getTeacherIdOfCurrentUser_shouldReturnTeacherId() {
        TeacherDto teacher = TeacherDto.builder()
                .id(44L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();
        InitialCredentials credentials = userService.createTeacherUser(teacher);
        setCurrentUser(credentials.getUsername());

        long teacherId = userService.getTeacherIdOfCurrentUser();

        assertThat(teacherId).isEqualTo(44L);
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserIsStudent_getTeacherIdOfCurrentUser_shouldThrowException() {
        StudentDto student = StudentDto.builder()
                .id(79L)
                .firstname("John")
                .lastname("Doe")
                .build();
        InitialCredentials credentials = userService.createStudentUser(student);
        setCurrentUser(credentials.getUsername());

        assertThatThrownBy(() -> userService.getTeacherIdOfCurrentUser())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User role is incorrect");
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserIsAdmin_getTeacherIdOfCurrentUser_shouldThrowException() {
        InitialCredentials credentials = userService.createAdminUser("admin");
        setCurrentUser(credentials.getUsername());

        assertThatThrownBy(() -> userService.getTeacherIdOfCurrentUser())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No school actor related to user exists");
    }

    private void setCurrentUser(String username) {
        UserDetails userDetails = userService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserWithUsernameExists_loadUserByUsernameTest_shouldReturnUserDetails() {
        userService.save(adminUser);
        UserDetails userDetails = userService.loadUserByUsername(adminUser.getUsername());

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(adminUser.getUsername());
        assertThat(userDetails.getPassword()).isEqualTo(adminUser.getPassword());
        assertThat(userDetails.getAuthorities()).isEqualTo(adminUser.getRole().getGrantedAuthorities());
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserWithUsernameDoesNotExists_loadUserByUsernameTest_shouldThrowException() {
        assertThatThrownBy(() -> userService.loadUserByUsername("anonymous"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(String.format("User not found with username \"%s\"", "anonymous"));
    }
}
