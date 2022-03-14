package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.assembler.TeacherModelAssembler;
import com.codecool.gradebookapi.exception.TeacherNotFoundException;
import com.codecool.gradebookapi.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/teachers")
@Slf4j
@Tag(name = "teacher-controller", description = "Operations on teachers")
@SecurityRequirement(name = "gradebookapi")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService service;
    private final TeacherModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Lists all teachers")
    @ApiResponse(responseCode = "200", description = "Returned list of all teachers")
    public ResponseEntity<CollectionModel<EntityModel<TeacherDto>>> getAll() {
        log.info("Returned list of all teachers");

        return ResponseEntity
                .ok(assembler.toCollectionModel(service.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Finds a teacher by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned teacher with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    public ResponseEntity<EntityModel<TeacherDto>> getById(@PathVariable("id") Long id) {
        TeacherDto teacherFound = service.findById(id).orElseThrow(() -> new TeacherNotFoundException(id));
        log.info("Returned teacher {}", id);

        return ResponseEntity
                .ok(assembler.toModel(teacherFound));
    }

    @PostMapping
    @Operation(summary = "Creates a new teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new teacher"),
            @ApiResponse(responseCode = "400", description = "Could not create teacher due to invalid parameters")
    })
    public ResponseEntity<EntityModel<TeacherDto>> add(@RequestBody @Valid TeacherDto teacher) {
        TeacherDto teacherCreated = service.save(teacher);
        EntityModel<TeacherDto> entityModel = assembler.toModel(teacherCreated);
        log.info("Created teacher with ID {}", teacherCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates the teacher given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated teacher with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not update teacher due to invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    public ResponseEntity<EntityModel<TeacherDto>> update(@RequestBody @Valid TeacherDto teacher,
                                                          @PathVariable("id") Long id) {
        service.findById(id).orElseThrow(() -> new TeacherNotFoundException(id));
        teacher.setId(id);
        log.info("Updated teacher {}", id);

        return ResponseEntity
                .ok(assembler.toModel(service.save(teacher)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes the teacher given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted teacher with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID"),
            @ApiResponse(responseCode = "405", description = "Could not delete teacher with given ID")
    })
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        service.findById(id).orElseThrow(() -> new TeacherNotFoundException(id));
        service.deleteById(id);
        log.info("Deleted teacher {}", id);

        return ResponseEntity.noContent().build();
    }
}
