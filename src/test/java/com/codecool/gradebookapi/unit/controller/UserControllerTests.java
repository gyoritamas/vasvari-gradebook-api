package com.codecool.gradebookapi.unit.controller;

import com.codecool.gradebookapi.controller.UserController;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.UserDto;
import com.codecool.gradebookapi.dto.assembler.InitialCredentialsModelAssembler;
import com.codecool.gradebookapi.dto.assembler.UserModelAssembler;
import com.codecool.gradebookapi.dto.dataTypes.InitialCredentials;
import com.codecool.gradebookapi.dto.dataTypes.UsernameInput;
import com.codecool.gradebookapi.exception.*;
import com.codecool.gradebookapi.jwt.JwtAuthenticationEntryPoint;
import com.codecool.gradebookapi.jwt.JwtTokenUtil;
import com.codecool.gradebookapi.model.request.PasswordChangeRequest;
import com.codecool.gradebookapi.security.ApplicationUserRole;
import com.codecool.gradebookapi.security.PasswordConfig;
import com.codecool.gradebookapi.service.StudentService;
import com.codecool.gradebookapi.service.TeacherService;
import com.codecool.gradebookapi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static com.codecool.gradebookapi.security.ApplicationUserRole.STUDENT;
import static com.codecool.gradebookapi.security.ApplicationUserRole.TEACHER;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
@Import({UserModelAssembler.class, InitialCredentialsModelAssembler.class, PasswordConfig.class, JwtAuthenticationEntryPoint.class})
public class UserControllerTests {

    private static ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private StudentService studentService;
    @MockBean
    private TeacherService teacherService;
    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    private UserDto adminUser;
    private UserDto teacherUser;
    private UserDto studentUser;

    public UserControllerTests() {
        mapper = new ObjectMapper();
    }

    @BeforeEach
    public void setup() {
        adminUser = UserDto.builder()
                .id(1L)
                .username("admin")
                .password("$2a$10$yVEw7X064WXb9T/bJ/LD2eGbG7Ue8cRQgUXxFL5ajh.xh2zM6QAda")
                .role(ApplicationUserRole.ADMIN)
                .enabled(true)
                .build();
        teacherUser = UserDto.builder()
                .id(2L)
                .username("darrellbowen81")
                .password("$2a$10$UjB1LNZg1z/aV2m.aMD5/OMo1PNiQbaTIoW/f3vhqnyRnh2N5tucW")
                .role(ApplicationUserRole.TEACHER)
                .enabled(true)
                .build();
        studentUser = UserDto.builder()
                .id(4L)
                .username("johndoe91")
                .password("$2a$10$0Uq8bcLPXpPE76quhjJq0Oz.iluSIBRKtZHU0pt0EwCLNNOyC5nYC")
                .role(STUDENT)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("given empty database, getAll should return empty list")
    public void givenEmptyDatabase_getAllShouldReturnEmptyList() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        this.mockMvc
                .perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users").doesNotExist());
    }

    @Test
    @DisplayName("given Users exist in database, getAll should return list of Users")
    public void givenUsersExistInDatabase_getAllShouldReturnListOfUsers() throws Exception {
        when(userService.findAll()).thenReturn(List.of(adminUser, teacherUser, studentUser));

        this.mockMvc
                .perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users", hasSize(3)))
                .andExpect(jsonPath("$._embedded.users[0].username", is(adminUser.getUsername())))
                .andExpect(jsonPath("$._embedded.users[1].username", is(teacherUser.getUsername())))
                .andExpect(jsonPath("$._embedded.users[2].username", is(studentUser.getUsername())));
    }

    @Test
    @DisplayName("given User exists with given ID, getById should return User")
    public void givenUserExistsWithGivenId_getById_shouldReturnUser() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));

        this.mockMvc
                .perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(jsonPath("$.username", is(adminUser.getUsername())))
                .andExpect(jsonPath("$.password", is(adminUser.getPassword())))
                .andExpect(jsonPath("$.role", is(adminUser.getRole().name())))
                .andExpect(jsonPath("$.enabled", is(true)));
    }

    @Test
    @DisplayName("given User does not exist with given ID, getById should return response 'Not Found'")
    public void givenUserDoesNotExistWithGivenId_getById_shouldReturnResponseNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/users/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("given valid UserDto values, add should return created User")
    public void givenValidUserDtoValues_add_shouldReturnCreatedUser() throws Exception {
        when(userService.save(adminUser)).thenReturn(adminUser);

        String adminUserAsString = mapper.writeValueAsString(adminUser);

        this.mockMvc
                .perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(adminUserAsString)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is(adminUser.getUsername())))
                .andExpect(jsonPath("$.password", is(adminUser.getPassword())))
                .andExpect(jsonPath("$.role", is(adminUser.getRole().name())))
                .andExpect(jsonPath("$.enabled", is(true)));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/invalid_user_data.csv", numLinesToSkip = 1, delimiter = ';')
    @DisplayName("given invalid UserDto parameters, add should return response 'Bad Request'")
    public void givenInvalidUserDtoParameters_add_shouldReturnResponseBadRequest(
            @AggregateWith(UserControllerTests.UserDtoAggregator.class) UserDto user) throws Exception {

        String userAsString = mapper.writeValueAsString(user);

        this.mockMvc
                .perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("when Student exists with given ID, createAccountForStudent should return initial credentials")
    public void whenStudentExistsWithGivenId_createAccountForStudent_shouldReturnInitialCredentials() throws Exception {
        StudentDto student = StudentDto.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .build();

        when(studentService.findById(1L)).thenReturn(Optional.of(student));
        when(userService.createStudentUser(student))
                .thenReturn(new InitialCredentials(1L, "johndoe91", "password1234"));

        this.mockMvc
                .perform(
                        post("/api/users/create-student-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("studentId", "1")
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("johndoe91")))
                .andExpect(jsonPath("$.password", is("password1234")));
    }

    @Test
    @DisplayName("when Student does not exist with given ID, createAccountForStudent should return response 'Not Found'")
    public void whenStudentDoesNotExistWithGivenId_createAccountForStudent_shouldReturnResponseNotFound() throws Exception {
        this.mockMvc
                .perform(
                        post("/api/users/create-student-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("studentId", "99")
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when Teacher exists with given ID, createAccountForTeacher should return initial credentials")
    public void whenTeacherExistsWithGivenId_createAccountForTeacher_shouldReturnInitialCredentials() throws Exception {
        TeacherDto teacher = TeacherDto.builder()
                .id(1L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();

        when(teacherService.findById(1L)).thenReturn(Optional.of(teacher));
        when(userService.createTeacherUser(teacher))
                .thenReturn(new InitialCredentials(1L, "darrellbowen81", "password1234"));

        this.mockMvc
                .perform(
                        post("/api/users/create-teacher-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("teacherId", "1")
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("darrellbowen81")))
                .andExpect(jsonPath("$.password", is("password1234")));
    }

    @Test
    @DisplayName("when Teacher does not exist with given ID, createAccountForTeacher should return response 'Not Found'")
    public void whenTeacherDoesNotExistWithGivenId_createAccountForTeacher_shouldReturnResponseNotFound() throws Exception {
        this.mockMvc
                .perform(
                        post("/api/users/create-teacher-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("teacherId", "99")
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when UsernameInput is valid, createAccountForAdmin should return initial credentials")
    public void whenUsernameInputIsValid_createAccountForTeacher_shouldReturnInitialCredentials() throws Exception {
        UsernameInput usernameInput = new UsernameInput("administrator");

        String usernameInputAsString = mapper.writeValueAsString(usernameInput);

        when(userService.createAdminUser("administrator"))
                .thenReturn(new InitialCredentials(1L, "administrator", "password1234"));

        this.mockMvc
                .perform(
                        post("/api/users/create-admin-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(usernameInputAsString)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("administrator")))
                .andExpect(jsonPath("$.password", is("password1234")));
    }

    @Test
    @DisplayName("when Admin exists with given name, createAccountForAdmin should return response 'Bad Request'")
    public void whenAdminExistsWithGivenName_createAccountForAdmin_shouldReturnResponseBadRequest() throws Exception {
        when(userService.createAdminUser("admin")).thenThrow(new UsernameTakenException("admin"));

        this.mockMvc
                .perform(
                        post("/api/users/create-admin-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("username", "admin")
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("when student-user exists, findUserRelatedToStudent should return UserDto")
    public void whenStudentUserExists_findUserRelatedToStudent_shouldReturnUserDto() throws Exception {
        StudentDto student = StudentDto.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .build();

        when(studentService.findById(1L)).thenReturn(Optional.of(student));
        when(userService.getUserRelatedToSchoolActor(STUDENT, 1L))
                .thenReturn(Optional.of(studentUser));

        this.mockMvc
                .perform(get("/api/users/students/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(studentUser.getUsername())))
                .andExpect(jsonPath("$.password", is(studentUser.getPassword())))
                .andExpect(jsonPath("$.enabled", is(studentUser.isEnabled())))
                .andExpect(jsonPath("$.role", is(studentUser.getRole().name())));
    }

    @Test
    @DisplayName("when student-user does not exist, findUserRelatedToStudent should return response 'Not Found'")
    public void whenStudentUserDoesNotExist_findUserRelatedToStudent_shouldReturnResponseNotFound() throws Exception {
        StudentDto student = StudentDto.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .build();

        when(studentService.findById(1L)).thenReturn(Optional.of(student));
        when(userService.getUserRelatedToSchoolActor(STUDENT, 1L))
                .thenThrow(StudentUserNotFoundException.class);

        this.mockMvc
                .perform(get("/api/users/students/1"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when Student does not exist with given ID, findUserRelatedToStudent should return response 'Not Found'")
    public void whenStudentDoesNotExistWithGivenId_findUserRelatedToStudent_shouldReturnResponseNotFound() throws Exception {
        when(studentService.findById(99L)).thenThrow(StudentNotFoundException.class);

        this.mockMvc
                .perform(get("/api/users/students/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("when teacher-user exists, findUserRelatedToTeacher should return UserDto")
    public void whenStudentUserExists_findUserRelatedToTeacher_shouldReturnUserDto() throws Exception {
        TeacherDto teacher = TeacherDto.builder()
                .id(1L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();

        when(teacherService.findById(1L)).thenReturn(Optional.of(teacher));
        when(userService.getUserRelatedToSchoolActor(TEACHER, 1L))
                .thenReturn(Optional.of(teacherUser));

        this.mockMvc
                .perform(get("/api/users/teachers/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(teacherUser.getUsername())))
                .andExpect(jsonPath("$.password", is(teacherUser.getPassword())))
                .andExpect(jsonPath("$.enabled", is(teacherUser.isEnabled())))
                .andExpect(jsonPath("$.role", is(teacherUser.getRole().name())));
    }

    @Test
    @DisplayName("when teacher-user does not exist, findUserRelatedToTeacher should return response 'Not Found'")
    public void whenTeacherUserDoesNotExist_findUserRelatedToTeacher_shouldReturnResponseNotFound() throws Exception {
        TeacherDto teacher = TeacherDto.builder()
                .id(1L)
                .firstname("Darrell")
                .lastname("Bowen")
                .build();

        when(teacherService.findById(1L)).thenReturn(Optional.of(teacher));
        when(userService.getUserRelatedToSchoolActor(STUDENT, 1L))
                .thenThrow(TeacherUserNotFoundException.class);

        this.mockMvc
                .perform(get("/api/users/teachers/1"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when Teacher does not exist with given ID, findUserRelatedToTeacher should return response 'Not Found'")
    public void whenTeacherDoesNotExistWithGivenId_findUserRelatedToTeacher_shouldReturnResponseNotFound() throws Exception {
        when(teacherService.findById(99L)).thenThrow(TeacherNotFoundException.class);

        this.mockMvc
                .perform(get("/api/users/teachers/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when User exists with given ID and PasswordChangeRequest is valid, changePassword should return response 'OK'")
    public void whenUserExistsWithGivenIdAndPasswordChangeRequestIsValid_changePassword_shouldReturnResponseOk() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));
        PasswordChangeRequest request = new PasswordChangeRequest(adminUser.getPassword(), "NeWPaSSWoRD1234");
        String requestAsString = mapper.writeValueAsString(request);

        this.mockMvc
                .perform(
                        post("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestAsString)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("when User exists with given ID and PasswordChangeRequest is invalid, changePassword should return response 'Bad Request'")
    public void whenUserExistsWithGivenIdAndPasswordChangeRequestIsInvalid_changePassword_shouldReturnResponseBadRequest() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));
        PasswordChangeRequest request = new PasswordChangeRequest("wrongpassword", "NeWPaSSWoRD1234");
        doThrow(IncorrectPasswordException.class).when(userService).changePassword(1L, request);
        String requestAsString = mapper.writeValueAsString(request);

        this.mockMvc
                .perform(
                        post("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestAsString)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("when User does not exist with given ID, changePassword should return response 'Not Found'")
    public void whenUserDoesNotExistWithGivenId_changePassword_shouldReturnResponseNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());
        PasswordChangeRequest request = new PasswordChangeRequest(adminUser.getPassword(), "NeWPaSSWoRD1234");
        String requestAsString = mapper.writeValueAsString(request);

        this.mockMvc
                .perform(
                        post("/api/users/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestAsString)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("when User exists with given ID, delete should return response 'No Content'")
    public void whenUserExistsWithGivenId_delete_shouldReturnResponseNoContent() throws Exception {
        when(userService.findById(4L)).thenReturn(Optional.of(studentUser));

        this.mockMvc
                .perform(
                        delete("/api/users/4")
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userService).deleteById(4L);
    }

    @Test
    @DisplayName("when User does not exist with given ID, delete should return response 'Not Found'")
    public void whenUserDoesNotExistWithGivenId_delete_shouldReturnResponseNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        this.mockMvc
                .perform(
                        delete("/api/users/99")
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(0)).deleteById(99L);
    }

    @Test
    @DisplayName("when User exists with given ID, enable should return Response 'OK'")
    public void whenUserExistsWithGivenId_enable_shouldReturnResponseOk() throws Exception {
        when(userService.findById(2L)).thenReturn(Optional.of(teacherUser));
        doNothing().when(userService).setUserEnabled(2L);

        this.mockMvc
                .perform(
                        post("/api/users/2/enable")
                )
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).setUserEnabled(2L);
    }

    @Test
    @DisplayName("when User does not exist with given ID, enable should return Response 'Not Found'")
    public void whenUserDoesNotExistWithGivenId_enable_shouldReturnResponseNotFound() throws Exception {
        doThrow(UserNotFoundException.class).when(userService).setUserEnabled(99L);

        this.mockMvc
                .perform(
                        post("/api/users/99/enable")
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService).setUserEnabled(99L);
    }

    @Test
    @DisplayName("when User exists with given ID, disable should return Response 'OK'")
    public void whenUserExistsWithGivenId_disable_shouldReturnResponseOk() throws Exception {
        doNothing().when(userService).setUserDisabled(4L);

        this.mockMvc
                .perform(
                        post("/api/users/4/disable")
                )
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).setUserDisabled(4L);
    }

    @Test
    @DisplayName("when User does not exist with given ID, disable should return Response 'Not Found'")
    public void whenUserDoesNotExistWithGivenId_disable_shouldReturnResponseNotFound() throws Exception {
        doThrow(UserNotFoundException.class).when(userService).setUserDisabled(99L);

        this.mockMvc
                .perform(
                        post("/api/users/99/disable")
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService).setUserDisabled(99L);
    }


    private static class UserDtoAggregator implements ArgumentsAggregator {

        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
                throws ArgumentsAggregationException {
            return UserDto.builder()
                    .username(accessor.getString(0))
                    .password(accessor.getString(1))
                    .role(convertStringToRole(accessor.getString(2)))
                    .enabled(accessor.getBoolean(3))
                    .build();
        }

        private ApplicationUserRole convertStringToRole(String string) {
            return string == null ? null : ApplicationUserRole.valueOf(string);
        }
    }

}
