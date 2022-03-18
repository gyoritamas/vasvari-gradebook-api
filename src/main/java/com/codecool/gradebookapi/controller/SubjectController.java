package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.SubjectInput;
import com.codecool.gradebookapi.dto.SubjectOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.assembler.SubjectModelAssembler;
import com.codecool.gradebookapi.dto.assembler.StudentModelAssembler;
import com.codecool.gradebookapi.exception.SubjectInUseException;
import com.codecool.gradebookapi.exception.SubjectNotFoundException;
import com.codecool.gradebookapi.exception.StudentNotFoundException;
import com.codecool.gradebookapi.exception.TeacherNotFoundException;
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
@Tag(name = "subject-controller", description = "Operations on subjects")
@SecurityRequirement(name = "gradebookapi")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final UserService userService;
    private final GradebookService gradebookService;
    private final SubjectModelAssembler subjectModelAssembler;
    private final StudentModelAssembler studentModelAssembler;

    @GetMapping("/subjects")
    @Operation(summary = "Lists all subjects")
    @ApiResponse(responseCode = "200", description = "Returned list of all subjects")
    public ResponseEntity<CollectionModel<EntityModel<SubjectOutput>>> getAll() {
        log.info("Returned list of all subjects");

        return ResponseEntity
                .ok(subjectModelAssembler.toCollectionModel(subjectService.findAll()));
    }

    @GetMapping("/subjects/{id}")
    @Operation(summary = "Finds a subject by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned subject with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find subject with given ID")
    })
    public ResponseEntity<EntityModel<SubjectOutput>> getById(@PathVariable("id") Long id) {
        SubjectOutput subjectFound = subjectService.findById(id).orElseThrow(() -> new SubjectNotFoundException(id));
        log.info("Returned subject {}", id);

        return ResponseEntity
                .ok(subjectModelAssembler.toModel(subjectFound));
    }

    @PostMapping("/subjects")
    @Operation(summary = "Creates a new subject")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new subject"),
            @ApiResponse(responseCode = "400", description = "Could not create subject due to invalid parameters")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<SubjectOutput>> add(@RequestBody @Valid SubjectInput subject) {
        SubjectOutput subjectCreated = subjectService.save(subject);
        EntityModel<SubjectOutput> entityModel = subjectModelAssembler.toModel(subjectCreated);
        log.info("Created subject with ID {}", subjectCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping("/subjects/{id}")
    @Operation(summary = "Updates the subject given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated subject with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not update subject due to invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Could not find subject with given ID")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<SubjectOutput>> update(@RequestBody @Valid SubjectInput subject,
                                                             @PathVariable("id") Long id) {
        subjectService.findById(id).orElseThrow(() -> new SubjectNotFoundException(id));
        log.info("Updated subject {}", id);

        return ResponseEntity
                .ok(subjectModelAssembler.toModel(subjectService.update(id, subject)));
    }

    @DeleteMapping("/subjects/{id}")
    @Operation(summary = "Deletes the subject given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted subject with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find subject with given ID"),
            @ApiResponse(responseCode = "405", description = "Could not delete subject with given ID")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        subjectService.findById(id).orElseThrow(() -> new SubjectNotFoundException(id));
        if (!gradebookService.findBySubjectId(id).isEmpty()) throw new SubjectInUseException(id);
        subjectService.deleteById(id);
        log.info("Deleted subject {}", id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subjects/{subjectId}/add_student/{studentId}")
    @Operation(summary = "Adds a student to a subject")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added student to subject"),
            @ApiResponse(responseCode = "404", description = "Could not find subject/student with given ID")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<SubjectOutput>> addStudentToSubject(@PathVariable("subjectId") Long subjectId,
                                                                          @PathVariable("studentId") Long studentId) {
        subjectService.findById(subjectId).orElseThrow(() -> new SubjectNotFoundException(subjectId));
        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        log.info("Added student {} to subject {}", studentId, subjectId);

        return ResponseEntity
                .ok(subjectModelAssembler.toModel(subjectService.addStudentToSubject(studentId, subjectId)));
    }

    @GetMapping("/subjects/{subjectId}/students")
    @Operation(summary = "Finds all students of the subject specified by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of students learning the subject"),
            @ApiResponse(responseCode = "404", description = "Could not find subject/student with given ID")
    })
//    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<CollectionModel<EntityModel<StudentDto>>> getStudentsOfSubject(@PathVariable("subjectId") Long subjectId) {
        List<StudentDto> studentsOfSubject = subjectService.getStudentsOfSubject(subjectId);
        log.info("Returned list of all students learning subject {}", subjectId);

        return ResponseEntity
                .ok(CollectionModel.of(studentModelAssembler.toCollectionModel(studentsOfSubject),
                        linkTo(methodOn(SubjectController.class).getStudentsOfSubject(subjectId))
                                .withRel("students-of-subject")));
    }


    @GetMapping("/teacher-user/subjects")
    @Operation(summary = "Finds all subjects the current user as teacher is teaching")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of subjects related to current user as teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CollectionModel<EntityModel<SubjectOutput>>> getSubjectsOfCurrentUserAsTeacher() {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));
        List<SubjectOutput> subjectsOfTeacher = subjectService.findSubjectsOfTeacher(teacher);

        log.info("Returned list of all subjects related to teacher {}", teacherId);

        return ResponseEntity
                .ok(CollectionModel.of(subjectModelAssembler.toCollectionModel(subjectsOfTeacher),
                        linkTo(methodOn(SubjectController.class).getSubjectsOfCurrentUserAsTeacher())
                                .withRel("subjects-of-teacher")));
    }

    @GetMapping("/student-user/subjects")
    @Operation(summary = "Find all subjects the current user as student is enrolled in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of subjects related to current user as student"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID")
    })
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CollectionModel<EntityModel<SubjectOutput>>> getSubjectsOfCurrentUserAsStudent() {
        Long studentId = userService.getStudentIdOfCurrentUser();
        StudentDto student = studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        List<SubjectOutput> subjectsOfStudent = studentService.findSubjectsOfStudent(student);

        log.info("Returned list of all subjects related to student {}", studentId);

        return ResponseEntity
                .ok(CollectionModel.of(subjectModelAssembler.toCollectionModel(subjectsOfStudent),
                        linkTo(methodOn(SubjectController.class).getSubjectsOfCurrentUserAsStudent())
                                .withRel("subjects-of-student")));
    }


}