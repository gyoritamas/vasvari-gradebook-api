package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.UserDto;
import com.codecool.gradebookapi.dto.assembler.InitialCredentialsModelAssembler;
import com.codecool.gradebookapi.dto.assembler.UserModelAssembler;
import com.codecool.gradebookapi.dto.dataTypes.InitialCredentials;
import com.codecool.gradebookapi.dto.dataTypes.UsernameInput;
import com.codecool.gradebookapi.exception.*;
import com.codecool.gradebookapi.model.request.PasswordChangeRequest;
import com.codecool.gradebookapi.service.StudentService;
import com.codecool.gradebookapi.service.TeacherService;
import com.codecool.gradebookapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.codecool.gradebookapi.security.ApplicationUserRole.STUDENT;
import static com.codecool.gradebookapi.security.ApplicationUserRole.TEACHER;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Tag(name = "user-controller", description = "Operations on users")
@SecurityRequirement(name = "gradebookapi")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final UserModelAssembler userModelAssembler;
    private final InitialCredentialsModelAssembler credentialsModelAssembler;

    @GetMapping
    @Operation(summary = "Lists all users")
    @ApiResponse(responseCode = "200", description = "Returned list of all users")
    public ResponseEntity<CollectionModel<EntityModel<UserDto>>> getAll() {
        log.info("Returned list of all users");

        return ResponseEntity
                .ok(userModelAssembler.toCollectionModel(userService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Finds a user by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned user with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find user with given ID")
    })
    public ResponseEntity<EntityModel<UserDto>> getById(@PathVariable("id") Long id) {
        UserDto userFound = userService.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        log.info("Returned user {}", id);

        return ResponseEntity
                .ok(userModelAssembler.toModel(userFound));
    }

    @PostMapping
    @Operation(summary = "Creates a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new user"),
            @ApiResponse(responseCode = "400", description = "Could not create user due to invalid parameters")
    })
    @Deprecated
    public ResponseEntity<EntityModel<UserDto>> add(@RequestBody @Valid UserDto user) {
        UserDto userCreated = userService.save(user);
        EntityModel<UserDto> entityModel = userModelAssembler.toModel(userCreated);
        log.info("Created user with ID {}", userCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PostMapping("/create-student-user")
    @Operation(summary = "Creates user account for a student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new user"),
            @ApiResponse(responseCode = "400", description = "Could not create user due to invalid parameters")
    })
    public ResponseEntity<EntityModel<InitialCredentials>> createAccountForStudent(@RequestParam("studentId") Long studentId) {
        StudentDto student = studentService.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        InitialCredentials credentials = userService.createStudentUser(student);
        log.info("Created student user account | username: {}, password: {}", credentials.getUsername(), credentials.getPassword());

        EntityModel<InitialCredentials> entityModel = credentialsModelAssembler.toModel(credentials);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PostMapping("/create-teacher-user")
    @Operation(summary = "Creates user account for a teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new user"),
            @ApiResponse(responseCode = "400", description = "Could not create user due to invalid parameters")
    })
    public ResponseEntity<EntityModel<InitialCredentials>> createAccountForTeacher(@RequestParam("teacherId") Long teacherId) {
        TeacherDto teacher = teacherService.findById(teacherId)
                .orElseThrow(() -> new TeacherNotFoundException(teacherId));
        InitialCredentials credentials = userService.createTeacherUser(teacher);
        log.info("Created teacher user account | username: {}, password: {}", credentials.getUsername(), credentials.getPassword());

        EntityModel<InitialCredentials> entityModel = credentialsModelAssembler.toModel(credentials);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PostMapping("/create-admin-user")
    @Operation(summary = "Creates user account for an admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new user"),
            @ApiResponse(responseCode = "400", description = "Could not create user due to invalid parameters")
    })
    public ResponseEntity<EntityModel<InitialCredentials>> createAccountForAdmin(@RequestBody @Valid UsernameInput usernameInput) {
        String username = usernameInput.getUsername();
        InitialCredentials credentials = userService.createAdminUser(username);
        log.info("Created admin user account | username: {}, password: {}", credentials.getUsername(), credentials.getPassword());
        EntityModel<InitialCredentials> entityModel = credentialsModelAssembler.toModel(credentials);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "Finds a student-user related to a student entity with the given ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned user related to student"),
            @ApiResponse(responseCode = "404", description = "Could not find user with the given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find user related to student")
    })
    public ResponseEntity<EntityModel<UserDto>> findUserRelatedToStudent(@PathVariable("id") Long studentId) {
        if (studentService.findById(studentId).isEmpty()) throw new StudentNotFoundException(studentId);
        UserDto user = userService.getUserRelatedToSchoolActor(STUDENT, studentId)
                .orElseThrow(() -> new StudentUserNotFoundException(studentId));

        EntityModel<UserDto> entityModel = userModelAssembler.toModel(user);
        log.info("Returned user related to student {}", studentId);

        return ResponseEntity.ok(entityModel);
    }

    @GetMapping("/teachers/{id}")
    @Operation(summary = "Finds a teacher-user related to a teacher entity with the given ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned user related to teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find user related to teacher")
    })
    public ResponseEntity<EntityModel<UserDto>> findUserRelatedToTeacher(@PathVariable("id") Long teacherId) {
        if (teacherService.findById(teacherId).isEmpty()) throw new TeacherNotFoundException(teacherId);
        UserDto user = userService.getUserRelatedToSchoolActor(TEACHER, teacherId)
                .orElseThrow(() -> new TeacherUserNotFoundException(teacherId));

        EntityModel<UserDto> entityModel = userModelAssembler.toModel(user);
        log.info("Returned user related to teacher {}", teacherId);

        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates the user given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated user with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not update user due to invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Could not find user with given ID")
    })
    @Deprecated
    public ResponseEntity<EntityModel<UserDto>> update(@RequestBody @Valid UserDto user,
                                                       @PathVariable("id") Long id) {
        userService.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setId(id);
        log.info("Updated user {}", id);

        return ResponseEntity
                .ok(userModelAssembler.toModel(userService.save(user)));
    }

    @PostMapping("/{id}")
    @Operation(summary = "Changes the user's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated user with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not change password due to incorrect old password"),
            @ApiResponse(responseCode = "404", description = "Could not find user with given ID")
    })
    public ResponseEntity<?> changePassword(@PathVariable("id") Long userId, @RequestBody @Valid PasswordChangeRequest request) {
        UserDto user = userService.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        userService.changePassword(userId, request);

        log.info("Password of user {} has changed", user.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes the user given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted user with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find user with given ID"),
            @ApiResponse(responseCode = "405", description = "Could not delete user with given ID")
    })
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        userService.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        userService.deleteById(id);
        log.info("Deleted user {}", id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "Enables the user given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enabled user with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find user with given ID")
    })
    public ResponseEntity<?> enable(@PathVariable("id") Long id) {
        userService.setUserEnabled(id);
        log.info("User {} is enabled", id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "Disables the user given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disabled user with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find user with given ID")
    })
    public ResponseEntity<?> disable(@PathVariable("id") Long id) {
        userService.setUserDisabled(id);
        log.info("User {} is disabled", id);

        return ResponseEntity.ok().build();
    }
}
