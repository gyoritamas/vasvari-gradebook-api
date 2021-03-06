package org.vasvari.gradebookapi.controller;

import org.vasvari.gradebookapi.dto.AssignmentInput;
import org.vasvari.gradebookapi.dto.AssignmentOutput;
import org.vasvari.gradebookapi.dto.assembler.AssignmentModelAssembler;
import org.vasvari.gradebookapi.exception.AssignmentInUseException;
import org.vasvari.gradebookapi.exception.AssignmentNotFoundException;
import org.vasvari.gradebookapi.model.AssignmentType;
import org.vasvari.gradebookapi.model.request.AssignmentRequest;
import org.vasvari.gradebookapi.model.request.GradebookRequest;
import org.vasvari.gradebookapi.service.AssignmentService;
import org.vasvari.gradebookapi.service.GradebookService;
import org.vasvari.gradebookapi.service.SubjectService;
import org.vasvari.gradebookapi.service.UserService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "assignment-controller", description = "Operations on assignments")
@SecurityRequirement(name = "gradebookapi")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final GradebookService gradebookService;
    private final SubjectService subjectService;
    private final UserService userService;
    private final AssignmentModelAssembler assembler;

    @GetMapping("/assignments")
    @Operation(summary = "Lists all assignments")
    @ApiResponse(responseCode = "200", description = "Returned list of all assignments")
    public ResponseEntity<CollectionModel<EntityModel<AssignmentOutput>>> getAll() {
        log.info("Returned list of all assignments");

        return ResponseEntity
                .ok(assembler.toCollectionModel(assignmentService.findAll()));
    }

    @GetMapping("/assignments/search")
    @Operation(summary = "Lists all assignments, filtered by name, type and subject")
    @ApiResponse(responseCode = "200", description = "Returned list of assignments")
    public ResponseEntity<CollectionModel<EntityModel<AssignmentOutput>>> searchAssignments(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "type", required = false) AssignmentType type,
            @RequestParam(value = "subjectId", required = false) Long subjectId) {
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle(title);
        request.setType(type);
        request.setSubjectId(subjectId);
        List<AssignmentOutput> assignmentList = assignmentService.findAssignments(request);

        log.info("Returned list of assignments with the following filters: " +
                "title={}, type={}, subjectId={}", title, type, subjectId);

        return ResponseEntity
                .ok(CollectionModel.of(assembler.toCollectionModel(assignmentList),
                        linkTo(methodOn(AssignmentController.class).searchAssignments(title, type, subjectId))
                                .withRel("assignments-filtered")));
    }

    @GetMapping("/assignments/{id}")
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

    @PostMapping("/assignments")
    @Operation(summary = "Creates a new assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new assignment"),
            @ApiResponse(responseCode = "400", description = "Could not create assignment due to invalid parameters")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<EntityModel<AssignmentOutput>> add(@RequestBody @Valid AssignmentInput assignment) {
        long subjectId = assignment.getSubjectId();
        // TODO: return proper response with Problem
        if (subjectService.findById(subjectId).isEmpty())
            return ResponseEntity.badRequest().build();

        AssignmentOutput assignmentCreated = assignmentService.save(assignment);
        EntityModel<AssignmentOutput> entityModel = assembler.toModel(assignmentCreated);
        log.info("Created assignment with ID {}", assignmentCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping("/assignments/{id}")
    @Operation(summary = "Updates the assignment given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated assignment with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not update assignment due to invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Could not find assignment with given ID")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<EntityModel<AssignmentOutput>> update(@RequestBody @Valid AssignmentInput assignment,
                                                                @PathVariable("id") Long id) {
        long subjectId = assignment.getSubjectId();
        if (subjectService.findById(subjectId).isEmpty())
            return ResponseEntity.badRequest().build();

        assignmentService.findById(id).orElseThrow(() -> new AssignmentNotFoundException(id));
        log.info("Updated assignment {}", id);

        return ResponseEntity
                .ok(assembler.toModel(assignmentService.update(id, assignment)));
    }

    @DeleteMapping("/assignments/{id}")
    @Operation(summary = "Deletes the assignment given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted assignment with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find assignment with given ID"),
            @ApiResponse(responseCode = "405", description = "Could not delete assignment with given ID"),
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        assignmentService.findById(id).orElseThrow(() -> new AssignmentNotFoundException(id));
        GradebookRequest request = GradebookRequest.builder().assignmentId(id).build();
        if (!gradebookService.findGradebookEntries(request).isEmpty()) throw new AssignmentInUseException(id);
        assignmentService.deleteById(id);
        log.info("Deleted assignment {}", id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teacher-user/assignments")
    @Operation(summary = "Finds all assignments the current user as teacher has created")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of assignments related to current user as teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CollectionModel<EntityModel<AssignmentOutput>>> getAssignmentsOfCurrentUserAsTeacher(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "type", required = false) AssignmentType type,
            @RequestParam(name = "subjectId", required = false) Long subjectId) {
        Long teacherId = userService.getTeacherIdOfCurrentUser();

        // filter by teacher
        List<AssignmentOutput> assignmentsOfTeacher = assignmentService.findAssignmentsOfTeacher(teacherId);

        // filter by request params
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle(title);
        request.setType(type);
        request.setSubjectId(subjectId);
        List<AssignmentOutput> assignmentsFiltered = assignmentService.findAssignments(request);

        assignmentsOfTeacher.retainAll(assignmentsFiltered);

        log.info("Returned assignments created by teacher {}, " +
                "with the following filters: title={}, type={}, subjectId={}", teacherId, title, type, subjectId);

        return ResponseEntity
                   .ok(CollectionModel.of(assembler.toCollectionModel(assignmentsOfTeacher),
                        linkTo(methodOn(AssignmentController.class).getAssignmentsOfCurrentUserAsTeacher(title, type, subjectId))
                                .withRel("assignments-of-teacher")));
    }

    @GetMapping("/student-user/assignments")
    @Operation(summary = "Find all assignments the current user as student has")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of assignments related to subjects of current user as student"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID")
    })
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CollectionModel<EntityModel<AssignmentOutput>>> getAssignmentsOfCurrentUserAsStudent(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "type", required = false) AssignmentType type,
            @RequestParam(name = "subjectId", required = false) Long subjectId) {
        Long studentId = userService.getStudentIdOfCurrentUser();

        // filter by student
        List<AssignmentOutput> assignmentsOfStudent = assignmentService.findAssignmentsOfStudent(studentId);

        // filter by request params
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle(title);
        request.setType(type);
        request.setSubjectId(subjectId);
        List<AssignmentOutput> assignmentsFiltered = assignmentService.findAssignments(request);

        assignmentsOfStudent.retainAll(assignmentsFiltered);

        log.info("Returned assignments of student {}, " +
                "with the following filters: title={}, type={}, subjectId={}", studentId, title, type, subjectId);

        return ResponseEntity
                .ok(CollectionModel.of(assembler.toCollectionModel(assignmentsOfStudent),
                        linkTo(methodOn(AssignmentController.class).getAssignmentsOfCurrentUserAsStudent(title, type, subjectId))
                                .withRel("assignments-of-student")));
    }
}
