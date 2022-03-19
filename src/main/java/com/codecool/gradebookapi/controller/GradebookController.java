package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.SubjectOutput;
import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.assembler.GradebookModelAssembler;
import com.codecool.gradebookapi.exception.*;
import com.codecool.gradebookapi.service.*;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<EntityModel<GradebookOutput>> delete(@PathVariable("id") Long id) {
        gradebookService.findById(id).orElseThrow(() -> new GradebookEntryNotFoundException(id));
        gradebookService.deleteById(id);
        log.info("Deleted gradebook entry {}", id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student_gradebook/{studentId}")
    @Operation(summary = "Finds all gradebook entries related to student given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of all gradebook entries related to student"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradesOfStudent(
            @PathVariable("studentId") Long studentId) {
        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));

        List<EntityModel<GradebookOutput>> entityModels = gradebookService.findByStudentId(studentId).stream()
                .map(gradebookModelAssembler::toModel)
                .collect(Collectors.toList());

        log.info("Returned list of all gradebook entries related to student {}", studentId);

        return ResponseEntity
                .ok(CollectionModel.of(entityModels,
                        linkTo(methodOn(GradebookController.class).getGradesOfStudent(studentId))
                                .withRel("student_gradebook")));
    }

    @GetMapping("/subject_gradebook/{subjectId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of all gradebook entries related to subject"),
            @ApiResponse(responseCode = "404", description = "Could not find subject with given ID")
    })
    @Operation(summary = "Finds all gradebook entries related to subject given by ID")
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradesOfSubject(
            @PathVariable("subjectId") Long subjectId) {
        subjectService.findById(subjectId).orElseThrow(() -> new SubjectNotFoundException(subjectId));

        List<EntityModel<GradebookOutput>> entityModels = gradebookService.findBySubjectId(subjectId).stream()
                .map(gradebookModelAssembler::toModel)
                .collect(Collectors.toList());

        log.info("Returned list of all gradebook entries related to subject {}", subjectId);

        return ResponseEntity
                .ok(CollectionModel.of(entityModels,
                        linkTo(methodOn(GradebookController.class).getGradesOfSubject(subjectId)).withRel("subject_gradebook")));
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
    public ResponseEntity<EntityModel<GradebookOutput>> gradeAssignment(@RequestBody @Valid GradebookInput gradebookInput) {
        Long studentId = gradebookInput.getStudentId();
        Long subjectId = gradebookInput.getSubjectId();
        Long assignmentId = gradebookInput.getAssignmentId();

        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        subjectService.findById(subjectId).orElseThrow(() -> new SubjectNotFoundException(subjectId));
        assignmentService.findById(assignmentId).orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        if (!subjectService.isStudentAddedToSubject(studentId, subjectId))
            throw new RelationNotFoundException(studentId, subjectId);
        if (gradebookService.isDuplicateEntry(gradebookInput)) throw new DuplicateEntryException(gradebookInput);

        GradebookOutput entryCreated = gradebookService.save(gradebookInput);
        EntityModel<GradebookOutput> entityModel = gradebookModelAssembler.toModel(entryCreated);
        log.info("Created gradebook entry with ID {}", entryCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/student-user/gradebook-entries")
    @Operation(summary = "Finds all gradebook entries related to current user as student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of all gradebook entries related to current user as student"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find subject with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradesOfCurrentUserAsStudent(
            @RequestParam(name = "subjectId", required = false) Long subjectId) {
        Long studentId = userService.getStudentIdOfCurrentUser();
        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        if (subjectId == null)
            return getGradebookEntriesByStudentId(studentId);
        else
            return getGradebookEntriesByStudentIdAndSubjectId(subjectId, studentId);
    }

    private ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradebookEntriesByStudentId(Long studentId) {
        List<EntityModel<GradebookOutput>> entityModels = gradebookService.findByStudentId(studentId).stream()
                .map(gradebookModelAssembler::toModel)
                .collect(Collectors.toList());

        log.info("Returned list of all gradebook entries related to student {}", studentId);

        return ResponseEntity
                .ok(CollectionModel.of(entityModels,
                        linkTo(methodOn(GradebookController.class).getGradesOfCurrentUserAsStudent(null))
                                .withRel("gradebook-entries-of-student").expand()));
    }

    private ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradebookEntriesByStudentIdAndSubjectId(Long subjectId, Long studentId) {
        subjectService.findById(subjectId).orElseThrow(() -> new SubjectNotFoundException(subjectId));
        if (!subjectService.isStudentAddedToSubject(studentId, subjectId))
            throw new RelationNotFoundException(studentId, subjectId);

        List<EntityModel<GradebookOutput>> entityModels = gradebookService.findByStudentIdAndSubjectId(studentId, subjectId).stream()
                .map(gradebookModelAssembler::toModel)
                .collect(Collectors.toList());

        log.info("Returned list of all gradebook entries related to student {} and subject {}", studentId, subjectId);

        return ResponseEntity
                .ok(CollectionModel.of(entityModels,
                        linkTo(methodOn(GradebookController.class).getGradesOfCurrentUserAsStudent(subjectId))
                                .withRel("gradebook-entries-of-student")));
    }

    @GetMapping("/teacher-user/gradebook-entries")
    @Operation(summary = "Find all gradebook entries related to the current user as teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of gradebook entries related to current user as teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradebookEntriesOfCurrentUserAsTeacher(
            @RequestParam(name = "gradeLevel", required = false) Integer gradeLevel,
            @RequestParam(name = "subjectId", required = false) Long subjectId) {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));

        List<GradebookOutput> gradebookEntries = new ArrayList<>();
        if (subjectId == null) {
            // every gradebook entry of every subject taught by the teacher
            List<SubjectOutput> subjectsOfTeacher = subjectService.findSubjectsOfTeacher(teacher);
            for (SubjectOutput subject : subjectsOfTeacher) {
                gradebookEntries.addAll(gradebookService.findBySubjectId(subject.getId()));
            }
        } else {
            // every gradebook entry of the specific subject
            SubjectOutput subject = subjectService.findById(subjectId).orElseThrow(() -> new SubjectNotFoundException(subjectId));
            if (!subject.getTeacher().getId().equals(teacherId))
                throw new RuntimeException(String.format("Teacher %d is not teaching subject %d", teacherId, subjectId));
            gradebookEntries.addAll(gradebookService.findBySubjectId(subjectId));
        }

        if (gradeLevel != null)
            gradebookEntries = gradebookEntries.stream()
                    .filter(entry -> studentService.findById(entry.getStudent().getId()).get().getGradeLevel().equals(gradeLevel))
                    .collect(Collectors.toList());

        log.info("Returned list of all gradebook entries related to teacher {}", teacherId);

        return ResponseEntity
                .ok(CollectionModel.of(gradebookModelAssembler.toCollectionModel(gradebookEntries),
                        linkTo(methodOn(GradebookController.class).getGradebookEntriesOfCurrentUserAsTeacher(gradeLevel, subjectId))
                                .withRel("gradebook-entries-of-teacher")));

    }
}
