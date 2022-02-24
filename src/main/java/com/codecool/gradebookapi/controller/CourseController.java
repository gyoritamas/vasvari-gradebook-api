package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.assembler.CourseModelAssembler;
import com.codecool.gradebookapi.exception.CourseInUseException;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
import com.codecool.gradebookapi.exception.StudentNotFoundException;
import com.codecool.gradebookapi.exception.TeacherNotFoundException;
import com.codecool.gradebookapi.service.CourseService;
import com.codecool.gradebookapi.service.GradebookService;
import com.codecool.gradebookapi.service.StudentService;
import com.codecool.gradebookapi.service.TeacherService;
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
@Tag(name = "class-controller", description = "Operations on courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private GradebookService gradebookService;

    @Autowired
    private CourseModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Lists all courses")
    @ApiResponse(responseCode = "200", description = "Returned list of all courses")
    public ResponseEntity<CollectionModel<EntityModel<CourseOutput>>> getAll() {
        log.info("Returned list of all courses");

        return ResponseEntity
                .ok(assembler.toCollectionModel(courseService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Finds a course by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned course with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find course with given ID")
    })
    public ResponseEntity<EntityModel<CourseOutput>> getById(@PathVariable("id") Long id) {
        CourseOutput courseFound = courseService.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        log.info("Returned course {}", id);

        return ResponseEntity
                .ok(assembler.toModel(courseFound));
    }

    @PostMapping
    @Operation(summary = "Creates a new course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new course"),
            @ApiResponse(responseCode = "400", description = "Could not create course due to invalid parameters")
    })
    public ResponseEntity<EntityModel<CourseOutput>> add(@RequestBody @Valid CourseInput course) {
        CourseOutput courseCreated = courseService.save(course);
        EntityModel<CourseOutput> entityModel = assembler.toModel(courseCreated);
        log.info("Created course with ID {}", courseCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates the course given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated course with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not update course due to invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Could not find course with given ID")
    })
    public ResponseEntity<EntityModel<CourseOutput>> update(@RequestBody @Valid CourseInput course,
                                                            @PathVariable("id") Long id) {
        courseService.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        log.info("Updated course {}", id);

        return ResponseEntity
                .ok(assembler.toModel(courseService.update(id, course)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes the course given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted course with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find course with given ID"),
            @ApiResponse(responseCode = "405", description = "Could not delete course with given ID")
    })
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        courseService.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        if (!gradebookService.findByClassId(id).isEmpty()) throw new CourseInUseException(id);
        courseService.deleteById(id);
        log.info("Deleted course {}", id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/set_teacher/{teacherId}")
    @Operation(summary = "Set the teacher of the course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Set the teacher of the course"),
            @ApiResponse(responseCode = "404", description = "Could not find course/teacher with given ID")
    })
    public ResponseEntity<EntityModel<CourseOutput>> setTeacherOfCourse(@PathVariable("courseId") Long courseId,
                                                                        @PathVariable("teacherId") Long teacherId) {
        courseService.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));
        log.info("Set teacher {} to class {}", teacherId, courseId);

        return ResponseEntity
                .ok(assembler.toModel(courseService.setTeacherOfCourse(teacherId, courseId)));
    }

    @PostMapping("/{courseId}/class_enrollment/{studentId}")
    @Operation(summary = "Adds a student to a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added student to course"),
            @ApiResponse(responseCode = "404", description = "Could not find course/student with given ID")
    })
    public ResponseEntity<EntityModel<CourseOutput>> addStudentToClass(@PathVariable("courseId") Long courseId,
                                                                       @PathVariable("studentId") Long studentId) {
        courseService.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        log.info("Added student {} to course {}", studentId, courseId);

        return ResponseEntity
                .ok(assembler.toModel(courseService.addStudentToCourse(studentId, courseId)));
    }
}
