package org.vasvari.gradebookapi.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.vasvari.gradebookapi.dto.GradebookInput;
import org.vasvari.gradebookapi.dto.GradebookOutput;
import org.vasvari.gradebookapi.dto.SubjectOutput;
import org.vasvari.gradebookapi.dto.TeacherDto;
import org.vasvari.gradebookapi.dto.assembler.GradebookModelAssembler;
import org.vasvari.gradebookapi.exception.*;
import org.vasvari.gradebookapi.model.request.GradebookRequest;
import org.vasvari.gradebookapi.service.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "gradebook-controller", description = "Operations on gradebook entries")
@SecurityRequirement(name = "gradebookapi")
@RequiredArgsConstructor
public class GradebookController {
    private final UserService userService;
    private final TeacherService teacherService;
    private final GradebookService gradebookService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final AssignmentService assignmentService;
    private final GradebookModelAssembler gradebookModelAssembler;

    @GetMapping("/gradebook")
    @Operation(summary = "Finds all gradebook entries")
    @ApiResponse(responseCode = "200", description = "Returned list of all gradebook entries")
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getAll() {
        log.info("Returned list of all gradebook entries");

        return ResponseEntity
                .ok(gradebookModelAssembler.toCollectionModel(gradebookService.findAll()));
    }

    @GetMapping("/gradebook/search")
    @Operation(summary = "Finds all gradebook entries, filtered by student, subject and assignment")
    @ApiResponse(responseCode = "200", description = "Returned list of all gradebook entries")
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> searchEntries(
            @RequestParam(value = "studentId", required = false) Long studentId,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "assignmentId", required = false) Long assignmentId) {

        GradebookRequest request = new GradebookRequest();
        request.setStudentId(studentId);
        request.setSubjectId(subjectId);
        request.setAssignmentId(assignmentId);

        List<GradebookOutput> entries = gradebookService.findGradebookEntries(request);

        log.info("Returned list of gradebook entries with the following filters: " +
                "studentId={}, subjectId={}, assignmentId={}", studentId, subjectId, assignmentId);

        return ResponseEntity
                .ok(CollectionModel.of(gradebookModelAssembler.toCollectionModel(entries),
                        linkTo(methodOn(GradebookController.class).searchEntries(studentId, subjectId, assignmentId))
                                .withRel("entries-filtered")));
    }

    @GetMapping("/gradebook/{id}")
    @Operation(summary = "Finds a gradebook entry by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned gradebook entry with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find gradebook entry with given ID")
    })
    public ResponseEntity<EntityModel<GradebookOutput>> getById(@PathVariable("id") Long id) {
        GradebookOutput entryFound = gradebookService.findById(id)
                .orElseThrow(() -> new GradebookEntryNotFoundException(id));
        log.info("Returned gradebook entry {}", id);

        return ResponseEntity
                .ok(gradebookModelAssembler.toModel(entryFound));
    }

    @DeleteMapping("/gradebook/{id}")
    @Operation(summary = "Deletes a gradebook entry specified by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted gradebook entry with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find gradebook entry with given ID")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<EntityModel<GradebookOutput>> delete(@PathVariable("id") Long id) {
        gradebookService.findById(id).orElseThrow(() -> new GradebookEntryNotFoundException(id));
        gradebookService.deleteById(id);
        log.info("Deleted gradebook entry {}", id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/gradebook")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creates a new gradebook entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created gradebook entry"),
            @ApiResponse(responseCode = "400", description =
                    "Could not create gradebook entry due to invalid/missing parameters " +
                            "or because student is not enrolled in given subject"),
            @ApiResponse(responseCode = "404", description = "Could not find student/subject/assignment with given ID"),
            @ApiResponse(responseCode = "409", description =
                    "Could not create gradebook entry because an entry already exists with the same IDs")
    })
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<EntityModel<GradebookOutput>> gradeAssignment(@RequestBody @Valid GradebookInput gradebookInput) {
        checkForErrors(gradebookInput);

        GradebookOutput entryCreated = gradebookService.save(gradebookInput);
        EntityModel<GradebookOutput> entityModel = gradebookModelAssembler.toModel(entryCreated);
        log.info("Created gradebook entry with ID {}", entryCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping("/gradebook/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Updates a gradebook entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated a gradebook entry"),
            @ApiResponse(responseCode = "400", description =
                    "Could not update gradebook entry due to invalid/missing parameters " +
                            "or because student is not enrolled in given subject"),
            @ApiResponse(responseCode = "404", description = "Could not find student/subject/assignment with given ID")
    })
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<EntityModel<GradebookOutput>> updateGradebookEntry(@RequestBody @Valid GradebookInput gradebookInput,
                                                                             @PathVariable("id") Long id) {
        checkForErrors(gradebookInput);

        GradebookOutput entryUpdated = gradebookService.update(id, gradebookInput);
        EntityModel<GradebookOutput> entityModel = gradebookModelAssembler.toModel(entryUpdated);
        log.info("Updated gradebook entry {}", id);

        return ResponseEntity.ok(entityModel);
    }

    private void checkForErrors(GradebookInput gradebookInput) {
        Long studentId = gradebookInput.getStudentId();
        Long subjectId = gradebookInput.getSubjectId();
        Long assignmentId = gradebookInput.getAssignmentId();

        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        subjectService.findById(subjectId).orElseThrow(() -> new SubjectNotFoundException(subjectId));
        assignmentService.findById(assignmentId).orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        if (!subjectService.isStudentAddedToSubject(studentId, subjectId))
            throw new SubjectRelationNotFoundException(studentId, subjectId);
    }

    @GetMapping("/student-user/gradebook-entries")
    @Operation(summary = "Finds all gradebook entries related to current user as student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of all gradebook entries related to current user as student"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find subject with given ID")
    })
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradesOfCurrentUserAsStudent(
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "assignmentId", required = false) Long assignmentId) {
        Long studentId = userService.getStudentIdOfCurrentUser();
        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));

        GradebookRequest request = new GradebookRequest();
        request.setStudentId(studentId);
        request.setSubjectId(subjectId);
        request.setAssignmentId(assignmentId);

        List<GradebookOutput> entries = gradebookService.findGradebookEntries(request);

        log.info("Returned gradebook entries related to student {} with the following filters: " +
                "subjectId={}, assignmentId={}", studentId, subjectId, assignmentId);

        return ResponseEntity
                .ok(CollectionModel.of(gradebookModelAssembler.toCollectionModel(entries),
                        linkTo(methodOn(GradebookController.class).getGradesOfCurrentUserAsStudent(subjectId, assignmentId))
                                .withRel("gradebook-entries-of-student")));
    }

    @GetMapping("/teacher-user/gradebook-entries")
    @Operation(summary = "Finds all gradebook entries related to the current user as teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of gradebook entries related to current user as teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradebookEntriesOfCurrentUserAsTeacher(
            @RequestParam(value = "studentId", required = false) Long studentId,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "assignmentId", required = false) Long assignmentId) {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));

        // filtered by teacher
        List<GradebookOutput> gradebookEntriesOfTeacher = findGradebookEntriesOfTeacher(teacher);

        // filtered by student, subject and assignment
        GradebookRequest request = new GradebookRequest();
        request.setStudentId(studentId);
        request.setSubjectId(subjectId);
        request.setAssignmentId(assignmentId);
        List<GradebookOutput> gradebookEntriesFiltered = gradebookService.findGradebookEntries(request);

        gradebookEntriesOfTeacher.retainAll(gradebookEntriesFiltered);

        log.info("Returned gradebook entries related to teacher {} with the following filters: " +
                "studentId={}, subjectId={}, assignmentId={}", teacherId, studentId, subjectId, assignmentId);

        return ResponseEntity
                .ok(CollectionModel.of(gradebookModelAssembler.toCollectionModel(gradebookEntriesOfTeacher),
                        linkTo(methodOn(GradebookController.class).getGradebookEntriesOfCurrentUserAsTeacher(studentId, subjectId, assignmentId))
                                .withRel("gradebook-entries-of-teacher")));
    }

    private List<GradebookOutput> findGradebookEntriesOfTeacher(TeacherDto teacher) {
        List<GradebookOutput> gradebookEntries = new ArrayList<>();
        List<SubjectOutput> subjectsOfTeacher = subjectService.findSubjectsOfTeacher(teacher);
        for (SubjectOutput subject : subjectsOfTeacher) {
            GradebookRequest request = GradebookRequest.builder().subjectId(subject.getId()).build();
            gradebookEntries.addAll(gradebookService.findGradebookEntries(request));
        }

        return gradebookEntries;
    }
}
