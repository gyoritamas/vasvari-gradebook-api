package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.UserDto;
import com.codecool.gradebookapi.dto.assembler.UserModelAssembler;
import com.codecool.gradebookapi.exception.UserNotFoundException;
import com.codecool.gradebookapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Tag(name = "user-controller", description = "Operations on users")
@SecurityRequirement(name = "gradebookapi")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Lists all users")
    @ApiResponse(responseCode = "200", description = "Returned list of all users")
    public ResponseEntity<CollectionModel<EntityModel<UserDto>>> getAll() {
        log.info("Returned list of all users");



        return ResponseEntity
                .ok(assembler.toCollectionModel(userService.findAll()));
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
                .ok(assembler.toModel(userFound));
    }

    @PostMapping
    @Operation(summary = "Creates a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new user"),
            @ApiResponse(responseCode = "400", description = "Could not create user due to invalid parameters")
    })
    public ResponseEntity<EntityModel<UserDto>> add(@RequestBody @Valid UserDto user) {
        UserDto userCreated = userService.save(user);
        EntityModel<UserDto> entityModel = assembler.toModel(userCreated);
        log.info("Created user with ID {}", userCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates the user given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated user with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not update user due to invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Could not find user with given ID")
    })
    public ResponseEntity<EntityModel<UserDto>> update(@RequestBody @Valid UserDto user,
                                                          @PathVariable("id") Long id) {
        userService.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setId(id);
        log.info("Updated user {}", id);

        return ResponseEntity
                .ok(assembler.toModel(userService.save(user)));
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
}
