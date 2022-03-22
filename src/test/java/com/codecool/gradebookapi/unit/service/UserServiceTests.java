package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.UserDto;
import com.codecool.gradebookapi.dto.dataTypes.InitialCredentials;
import com.codecool.gradebookapi.security.ApplicationUserRole;
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

import javax.transaction.Transactional;
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
    @DisplayName("createStudentUser should return created Student user's initial credentials")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void createStudentUser_shouldReturnCreatedStudentUsersInitialCredentials() {
        StudentDto student = StudentDto.builder()
                .id(79L)
                .firstname("John")
                .lastname("Doe")
                .build();
        InitialCredentials credentials = userService.createStudentUser(student);

        Pattern usernamePattern = Pattern.compile("johndoe\\d{2}");
        assertThat(credentials.getUsername()).matches(usernamePattern);
        assertThat(credentials.getPassword()).matches("([a-zA-Z0-9]){" + UserService.PASSWORD_LENGTH + "}");
    }

    @Test
    @DisplayName("given Student already has user account, createStudentUser should throw exception")
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
    @DisplayName("createTeacherUser should return created teacher's initial credentials")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void createTeacherUser_shouldReturnCreatedTeacherUsersInitialCredentials() {
        TeacherDto teacher = TeacherDto.builder()
                .id(44L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();
        InitialCredentials credentials = userService.createTeacherUser(teacher);

        Pattern usernamePattern = Pattern.compile("darrellbowen\\d{2}");
        assertThat(credentials.getUsername()).matches(usernamePattern);
        assertThat(credentials.getPassword()).matches("([a-zA-Z0-9]){" + UserService.PASSWORD_LENGTH + "}");
    }

    @Test
    @DisplayName("given teacher already has user account, createTeacherUser should throw exception")
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
    @DisplayName("given username is not in use, createAdminUser should return admin user's initial credentials")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUsernameNotInUse_createAdminUserShouldReturnAdminUsersInitialCredentials() {
        InitialCredentials credentials = userService.createAdminUser("admin");

        assertThat(credentials.getUsername()).isEqualTo("admin");
        assertThat(credentials.getPassword()).matches("([a-zA-Z0-9]){" + UserService.PASSWORD_LENGTH + "}");
    }

    @Test
    @DisplayName("given username is in use, createAdminUser should throw exception")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUsernameIsInUse_createAdminUserShouldThrowException() {
        userService.createAdminUser("admin");

        assertThatThrownBy(() -> userService.createAdminUser("admin"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Username already taken");
    }

    @Test
    @DisplayName("given User exists with ID, findById should return user")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserExistsWithId_findByIdShouldReturnUser() {
        studentUser = userService.save(studentUser);
        long userId = studentUser.getId();

        Optional<UserDto> userFound = userService.findById(userId);

        assertThat(userFound).isPresent();
        assertThat(userFound.get()).isEqualTo(studentUser);
    }

    @Test
    @DisplayName("given User does not exists with ID, findById should return empty Optional")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserDoesNotExistWithId_findByIdShouldReturnEmptyOptional() {
        Optional<UserDto> userFound = userService.findById(99L);

        assertThat(userFound).isEmpty();
    }

    @Test
    @DisplayName("given User exists with username, findByUsername should return User")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserExistsWithUsername_findByUsernameShouldReturnUser() {
        teacherUser = userService.save(teacherUser);

        Optional<UserDto> userFound = userService.findByUsername(teacherUser.getUsername());

        assertThat(userFound).isPresent();
        assertThat(userFound.get()).isEqualTo(teacherUser);
    }

    @Test
    @DisplayName("given User does not exist with username, findByUsername should return empty Optional")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserDoesNotExistWithUsername_findByUsernameShouldReturnEmptyOptional() {
        Optional<UserDto> userFound = userService.findByUsername("anonymous");

        assertThat(userFound).isEmpty();
    }

    @Test
    @DisplayName("given User exists with ID, deleteById should delete User")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserExistsWithId_deleteByIdShouldDeleteUser() {
        adminUser = userService.save(adminUser);
        long userId = adminUser.getId();

        userService.deleteById(userId);

        assertThat(userService.findById(userId)).isEmpty();
    }

    @Test
    @DisplayName("given User exists with ID, findStudentIdByUserId should return Student ID")
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
    @DisplayName("given User does not exist with ID, findStudentIdByUserId should throw exception")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserDoesNotExistWithId_findStudentIdByUserId_shouldThrowException() {
        assertThatThrownBy(() -> userService.findStudentIdByUserId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No school actor related to user exists");
    }

    @Test
    @DisplayName("given User exists with ID, findTeacherIdByUserId should return Teacher ID")
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
    @DisplayName("given User does not exist with given ID, findTeacherIdByUserId should throw exception")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserDoesNotExistWithId_findTeacherIdByUserId_shouldThrowException() {
        assertThatThrownBy(() -> userService.findTeacherIdByUserId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No school actor related to user exists");
    }

    @Test
    @DisplayName("given User is STUDENT and has an account, getStudentIdOfCurrentUser should return Student ID")
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
    @DisplayName("given User is ADMIN, getStudentIdOfCurrentUser should throw exception")
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
    @DisplayName("given User is TEACHER, getStudentIdOfCurrentUser should throw exception")
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
    @DisplayName("given User is TEACHER and has an account, getTeacherIdOfCurrentUser should return Teacher ID")
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
    @DisplayName("given User is STUDENT, getTeacherIdOfCurrentUser should throw exception")
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
    @DisplayName("given User is ADMIN, getTeacherIdOfCurrentUser should throw exception")
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
    @DisplayName("given User with username exists, loadUserByUsername should return UserDetails")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserWithUsernameExists_loadUserByUsername_shouldReturnUserDetails() {
        userService.save(adminUser);
        UserDetails userDetails = userService.loadUserByUsername(adminUser.getUsername());

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(adminUser.getUsername());
        assertThat(userDetails.getPassword()).isEqualTo(adminUser.getPassword());
        assertThat(userDetails.getAuthorities()).isEqualTo(adminUser.getRole().getGrantedAuthorities());
    }

    @Test
    @DisplayName("given User with username does not exist, loadUserByUsername should throw exception")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserWithUsernameDoesNotExist_loadUserByUsername_shouldThrowException() {
        assertThatThrownBy(() -> userService.loadUserByUsername("anonymous"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(String.format("User not found with username \"%s\"", "anonymous"));
    }

    @Test
    @DisplayName("given current User is ADMIN, getRoleOfCurrentUser should return ADMIN")
    public void givenCurrentUserIsAdmin_getRoleOfCurrentUserShouldReturnAdmin() {
        InitialCredentials credentials = userService.createAdminUser("mrrobot");
        setCurrentUser(credentials.getUsername());

        ApplicationUserRole roleOfCurrentUser = userService.getRoleOfCurrentUser();

        assertThat(roleOfCurrentUser).isEqualTo(ADMIN);
    }

    @Test
    @DisplayName("given current User is TEACHER, getRoleOfCurrentUser should return TEACHER")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenCurrentUserIsTeacher_getRoleOfCurrentUserShouldReturnTeacher() {
        TeacherDto teacher = TeacherDto.builder()
                .id(44L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();
        InitialCredentials credentials = userService.createTeacherUser(teacher);
        setCurrentUser(credentials.getUsername());

        ApplicationUserRole roleOfCurrentUser = userService.getRoleOfCurrentUser();

        assertThat(roleOfCurrentUser).isEqualTo(TEACHER);
    }

    @Test
    @DisplayName("given current User is STUDENT, getRoleOfCurrentUser should return STUDENT")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenCurrentUserIsStudent_getRoleOfCurrentUserShouldReturnStudent() {
        StudentDto student = StudentDto.builder()
                .id(79L)
                .firstname("John")
                .lastname("Doe")
                .build();
        InitialCredentials credentials = userService.createStudentUser(student);
        setCurrentUser(credentials.getUsername());

        ApplicationUserRole roleOfCurrentUser = userService.getRoleOfCurrentUser();

        assertThat(roleOfCurrentUser).isEqualTo(STUDENT);
    }

    @Test
    @Transactional
    @DisplayName("given UserDto related to Student exists, getUserRelatedToSchoolActor should return UserDto")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserDtoRelatedToStudentExists_getUserRelatedToSchoolActor_shouldReturnUserDto() {
        // create student user
        StudentDto student = StudentDto.builder()
                .id(79L)
                .firstname("John")
                .lastname("Doe")
                .build();
        InitialCredentials credentials = userService.createStudentUser(student);

        // get user ID
        String username = credentials.getUsername();
        Optional<UserDto> userCreated = userService.findByUsername(username);
        assertThat(userCreated.isPresent()).isTrue();
        long userId = userCreated.get().getId();

        Optional<UserDto> userFound = userService.getUserRelatedToSchoolActor(STUDENT, 79L);
        assertThat(userFound.isPresent()).isTrue();
        assertThat(userFound.get().getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("given UserDto related to Student does not exist, getUserRelatedToSchoolActor should return empty Optional")
    public void givenUserDtoRelatedToStudentDoesNotExist_getUserRelatedToSchoolActor_shouldReturnEmptyOptional() {
        Optional<UserDto> userFound = userService.getUserRelatedToSchoolActor(STUDENT, 99L);

        assertThat(userFound).isEqualTo(Optional.empty());
    }

    @Test
    @Transactional
    @DisplayName("given UserDto related to Teacher exists, getUserRelatedToSchoolActor should return UserDto")
    @DirtiesContext(methodMode = BEFORE_METHOD)
    public void givenUserRelatedToTeacherExists_getUserRelatedToSchoolActor_shouldReturnUserDto() {
        // create teacher user
        TeacherDto teacher = TeacherDto.builder()
                .id(44L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();
        InitialCredentials credentials = userService.createTeacherUser(teacher);

        // get user ID
        String username = credentials.getUsername();
        Optional<UserDto> userCreated = userService.findByUsername(username);
        assertThat(userCreated.isPresent()).isTrue();
        long userId = userCreated.get().getId();

        Optional<UserDto> userFound = userService.getUserRelatedToSchoolActor(TEACHER, 44L);
        assertThat(userFound.isPresent()).isTrue();
        assertThat(userFound.get().getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("given UserDto related to Teacher does not exist, getUserRelatedToSchoolActor should return empty Optional")
    public void givenUserDtoRelatedToTeacherDoesNotExist_getUserRelatedToSchoolActor_shouldReturnEmptyOptional() {
        Optional<UserDto> userFound = userService.getUserRelatedToSchoolActor(TEACHER, 99L);

        assertThat(userFound).isEqualTo(Optional.empty());
    }

}
