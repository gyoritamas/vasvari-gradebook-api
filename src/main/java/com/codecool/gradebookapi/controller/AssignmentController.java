package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.AssignmentInput;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import com.codecool.gradebookapi.dto.assembler.AssignmentModelAssembler;
import com.codecool.gradebookapi.exception.AssignmentInUseException;
import com.codecool.gradebookapi.exception.AssignmentNotFoundException;
import com.codecool.gradebookapi.service.AssignmentService;
import com.codecool.gradebookapi.service.GradebookService;
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
@RequestMapping("/api/assignments")
@Slf4j
@Tag(name = "assignment-controller", description = "Operations on assignments")
@SecurityRequirement(name = "gradebookapi")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final GradebookService gradebookService;
    private final AssignmentModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Lists all assignments")
    @ApiResponse(responseCode = "200", description = "Returned list of all assignments")
    public ResponseEntity<CollectionModel<EntityModel<AssignmentOutput>>> getAll() {
        log.info("Returned list of all assignments");

        return ResponseEntity
                .ok(assembler.toCollectionModel(assignmentService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Finds an assignment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned assignment with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find assignment with given ID")
    })
    public ResponseEntity<EntityModel<AssignmentOutput>> getById(@PathVariable("id") Long id) {
        AssignmentOutput assignmentFound =
                assignmentService.findById(id).orElseThrow(() -> new AssignmentNotFoundException(id));
        log.info("Returned assignment {}", id);

        return ResponseEntity
                .ok(assembler.toModel(assignmentFound));
    }

    @PostMapping
    @Operation(summary = "Creates a new assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new assignment"),
            @ApiResponse(responseCode = "400", description = "Could not create assignment due to invalid parameters")
    })
    public ResponseEntity<EntityModel<AssignmentOutput>> add(@RequestBody @Valid AssignmentInput assignment) {
        AssignmentOutput assignmentCreated = assignmentService.save(assignment);
        EntityModel<AssignmentOutput> entityModel = assembler.toModel(assignmentCreated);
        log.info("Created assignment with ID {}", assignmentCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates the assignment given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated assignment with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not update assignment due to invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Could not find assignment with given ID")
    })
    public ResponseEntity<EntityModel<AssignmentOutput>> update(@RequestBody @Valid AssignmentInput assignment,
                                                                @PathVariable("id") Long id) {
        assignmentService.findById(id).orElseThrow(() -> new AssignmentNotFoundException(id));
        log.info("Updated assignment {}", id);

        return ResponseEntity
                .ok(assembler.toModel(assignmentService.update(id, assignment)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes the assignment given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted assignment with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find assignment with given ID"),
            @ApiResponse(responseCode = "405", description = "Could not delete assignment with given ID"),
    })
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        assignmentService.findById(id).orElseThrow(() -> new AssignmentNotFoundException(id));
        if (!gradebookService.findByAssignmentId(id).isEmpty()) throw new AssignmentInUseException(id);
        assignmentService.deleteById(id);
        log.info("Deleted assignment {}", id);

        return ResponseEntity.noContent().build();
    }
}
