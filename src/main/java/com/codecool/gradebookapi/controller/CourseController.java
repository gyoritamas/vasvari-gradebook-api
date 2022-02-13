package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.assembler.CourseModelAssembler;
import com.codecool.gradebookapi.exception.CourseInUseException;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
import com.codecool.gradebookapi.exception.StudentNotFoundException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/classes")
@Slf4j
@Tag(name = "class-controller", description = "Operations on classes")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private GradebookService gradebookService;

    @Autowired
    private CourseModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Lists all classes")
    @ApiResponse(responseCode = "200", description = "Returned list of all classes")
    public ResponseEntity<CollectionModel<EntityModel<CourseOutput>>> getAll() {
        log.info("Returned list of all classes");

        return ResponseEntity
                .ok(assembler.toCollectionModel(courseService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Finds a class by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned class with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find class with given ID")
    })
    public ResponseEntity<EntityModel<CourseOutput>> getById(@PathVariable("id") Long id) {
        CourseOutput classFound = courseService.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        log.info("Returned class {}", id);

        return ResponseEntity
                .ok(assembler.toModel(classFound));
    }

    @PostMapping
    @Operation(summary = "Creates a new class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new class"),
            @ApiResponse(responseCode = "400", description = "Could not create class due to invalid parameters")
    })
    public ResponseEntity<EntityModel<CourseOutput>> add(@RequestBody @Valid CourseInput clazz) {
        CourseOutput classCreated = courseService.save(clazz);
        EntityModel<CourseOutput> entityModel = assembler.toModel(classCreated);
        log.info("Created class with ID {}", classCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates the class given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated class with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not update class due to invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Could not find class with given ID")
    })
    public ResponseEntity<EntityModel<CourseOutput>> update(@RequestBody @Valid CourseInput clazz,
                                                            @PathVariable("id") Long id) {
        courseService.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        log.info("Updated class {}", id);

        return ResponseEntity
                .ok(assembler.toModel(courseService.update(id, clazz)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes the class given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted class with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find class with given ID"),
            @ApiResponse(responseCode = "405", description = "Could not delete class with given ID")
    })
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        courseService.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        if (!gradebookService.findByClassId(id).isEmpty()) throw new CourseInUseException(id);
        courseService.deleteById(id);
        log.info("Deleted class {}", id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{classId}/class_enrollment/{studentId}")
    @Operation(summary = "Adds a student to a class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added student to class"),
            @ApiResponse(responseCode = "404", description = "Could not find class/student with given ID")
    })
    public ResponseEntity<EntityModel<CourseOutput>> addStudentToClass(@PathVariable("classId") Long classId,
                                                                       @PathVariable("studentId") Long studentId) {
        courseService.findById(classId).orElseThrow(() -> new CourseNotFoundException(classId));
        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        log.info("Added student {} to class {}", studentId, classId);

        return ResponseEntity
                .ok(assembler.toModel(courseService.addStudentToClass(studentId, classId)));
    }
}
