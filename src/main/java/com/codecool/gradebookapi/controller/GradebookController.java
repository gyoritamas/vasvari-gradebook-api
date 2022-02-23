package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.GradebookInput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.assembler.GradebookModelAssembler;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
import com.codecool.gradebookapi.exception.*;
import com.codecool.gradebookapi.service.AssignmentService;
import com.codecool.gradebookapi.service.CourseService;
import com.codecool.gradebookapi.service.GradebookService;
import com.codecool.gradebookapi.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "gradebook-controller", description = "Operations on gradebook entries")
public class GradebookController {

    @Autowired
    private GradebookService gradebookService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private GradebookModelAssembler assembler;

    @GetMapping("/gradebook")
    @Operation(summary = "Finds all gradebook entries")
    @ApiResponse(responseCode = "200", description = "Returned list of all gradebook entries")
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getAll() {
        log.info("Returned list of all gradebook entries");

        return ResponseEntity
                .ok(assembler.toCollectionModel(gradebookService.findAll()));
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
                .ok(assembler.toModel(entryFound));
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
                .map(entry -> assembler.toModel(entry))
                .collect(Collectors.toList());

        log.info("Returned list of all gradebook entries related to student {}", studentId);

        return ResponseEntity
                .ok(CollectionModel.of(entityModels,
                        linkTo(methodOn(GradebookController.class).getGradesOfStudent(studentId))
                                .withRel("student_gradebook")));
    }

    @GetMapping("/class_gradebook/{classId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of all gradebook entries related to class"),
            @ApiResponse(responseCode = "404", description = "Could not find class with given ID")
    })
    @Operation(summary = "Finds all gradebook entries related to class given by ID")
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradesOfClass(
            @PathVariable("classId") Long classId) {
        courseService.findById(classId).orElseThrow(() -> new CourseNotFoundException(classId));

        List<EntityModel<GradebookOutput>> entityModels = gradebookService.findByClassId(classId).stream()
                .map(entry -> assembler.toModel(entry))
                .collect(Collectors.toList());

        log.info("Returned list of all gradebook entries related to class {}", classId);

        return ResponseEntity
                .ok(CollectionModel.of(entityModels,
                        linkTo(methodOn(GradebookController.class).getGradesOfClass(classId)).withRel("class_gradebook")));
    }

    @PostMapping("/gradebook")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creates a new gradebook entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created gradebook entry"),
            @ApiResponse(responseCode = "400", description =
                    "Could not create gradebook entry due to invalid/missing parameters " +
                            "or because student is not enrolled in given class"),
            @ApiResponse(responseCode = "404", description = "Could not find student/class/assignment with given ID"),
            @ApiResponse(responseCode = "409", description =
                    "Could not create gradebook entry because an entry already exists with the same IDs")
    })
    public ResponseEntity<EntityModel<GradebookOutput>> gradeAssignment(@RequestBody @Valid GradebookInput gradebookInput) {
        Long studentId = gradebookInput.getStudentId();
        Long classId = gradebookInput.getCourseId();
        Long assignmentId = gradebookInput.getAssignmentId();

        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        courseService.findById(classId).orElseThrow(() -> new CourseNotFoundException(classId));
        assignmentService.findById(assignmentId).orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        if (!courseService.isStudentInCourse(studentId, classId))
            throw new StudentNotInCourseException(studentId, classId);
        if (gradebookService.isDuplicateEntry(gradebookInput)) throw new DuplicateEntryException(gradebookInput);

        GradebookOutput entryCreated = gradebookService.save(gradebookInput);
        EntityModel<GradebookOutput> entityModel = assembler.toModel(entryCreated);
        log.info("Created gradebook entry with ID {}", entryCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }
}
