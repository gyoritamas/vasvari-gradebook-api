package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.CourseInput;
import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.assembler.CourseModelAssembler;
import com.codecool.gradebookapi.dto.assembler.StudentModelAssembler;
import com.codecool.gradebookapi.exception.CourseInUseException;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
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
@Tag(name = "course-controller", description = "Operations on courses")
@SecurityRequirement(name = "gradebookapi")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final UserService userService;
    private final GradebookService gradebookService;
    private final CourseModelAssembler courseModelAssembler;
    private final StudentModelAssembler studentModelAssembler;

    @GetMapping("/courses")
    @Operation(summary = "Lists all courses")
    @ApiResponse(responseCode = "200", description = "Returned list of all courses")
    public ResponseEntity<CollectionModel<EntityModel<CourseOutput>>> getAll() {
        log.info("Returned list of all courses");

        return ResponseEntity
                .ok(courseModelAssembler.toCollectionModel(courseService.findAll()));
    }

    @GetMapping("/courses/{id}")
    @Operation(summary = "Finds a course by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned course with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find course with given ID")
    })
    public ResponseEntity<EntityModel<CourseOutput>> getById(@PathVariable("id") Long id) {
        CourseOutput courseFound = courseService.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        log.info("Returned course {}", id);

        return ResponseEntity
                .ok(courseModelAssembler.toModel(courseFound));
    }

    @PostMapping("/courses")
    @Operation(summary = "Creates a new course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new course"),
            @ApiResponse(responseCode = "400", description = "Could not create course due to invalid parameters")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<CourseOutput>> add(@RequestBody @Valid CourseInput course) {
        CourseOutput courseCreated = courseService.save(course);
        EntityModel<CourseOutput> entityModel = courseModelAssembler.toModel(courseCreated);
        log.info("Created course with ID {}", courseCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping("/courses/{id}")
    @Operation(summary = "Updates the course given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated course with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not update course due to invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Could not find course with given ID")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<CourseOutput>> update(@RequestBody @Valid CourseInput course,
                                                            @PathVariable("id") Long id) {
        courseService.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        log.info("Updated course {}", id);

        return ResponseEntity
                .ok(courseModelAssembler.toModel(courseService.update(id, course)));
    }

    @DeleteMapping("/courses/{id}")
    @Operation(summary = "Deletes the course given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted course with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find course with given ID"),
            @ApiResponse(responseCode = "405", description = "Could not delete course with given ID")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        courseService.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        if (!gradebookService.findByClassId(id).isEmpty()) throw new CourseInUseException(id);
        courseService.deleteById(id);
        log.info("Deleted course {}", id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/courses/{courseId}/class_enrollment/{studentId}")
    @Operation(summary = "Adds a student to a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added student to course"),
            @ApiResponse(responseCode = "404", description = "Could not find course/student with given ID")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<CourseOutput>> addStudentToClass(@PathVariable("courseId") Long courseId,
                                                                       @PathVariable("studentId") Long studentId) {
        courseService.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        log.info("Added student {} to course {}", studentId, courseId);

        return ResponseEntity
                .ok(courseModelAssembler.toModel(courseService.addStudentToCourse(studentId, courseId)));
    }

    @GetMapping("/courses/{courseId}/students")
    @Operation(summary = "Finds all students of the course specified by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of students learning the subject"),
            @ApiResponse(responseCode = "404", description = "Could not find course/student with given ID")
    })
//    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<CollectionModel<EntityModel<StudentDto>>> getStudentsOfCourse(@PathVariable("courseId") Long courseId) {
        List<StudentDto> studentsOfCourse = courseService.getStudentsOfCourse(courseId);
        log.info("Returned list of all students learning subject {}", courseId);

        return ResponseEntity
                .ok(CollectionModel.of(studentModelAssembler.toCollectionModel(studentsOfCourse),
                        linkTo(methodOn(CourseController.class).getStudentsOfCourse(courseId))
                                .withRel("students-of-course")));
    }


    @GetMapping("/teacher-user/courses")
    @Operation(summary = "Finds all courses the current user as teacher is teaching")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of courses related to current user as teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CollectionModel<EntityModel<CourseOutput>>> getCoursesOfCurrentUserAsTeacher() {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));
        List<CourseOutput> coursesOfTeacher = courseService.findCoursesOfTeacher(teacher);

        log.info("Returned list of all courses related to teacher {}", teacherId);

        return ResponseEntity
                .ok(CollectionModel.of(courseModelAssembler.toCollectionModel(coursesOfTeacher),
                        linkTo(methodOn(CourseController.class).getCoursesOfCurrentUserAsTeacher())
                                .withRel("courses-of-teacher")));
    }

    @GetMapping("/student-user/courses")
    @Operation(summary = "Find all courses the current user as student is enrolled in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of courses related to current user as student"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID")
    })
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CollectionModel<EntityModel<CourseOutput>>> getCoursesOfCurrentUserAsStudent() {
        Long studentId = userService.getStudentIdOfCurrentUser();
        StudentDto student = studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        List<CourseOutput> coursesOfStudent = studentService.findCoursesOfStudent(student);

        log.info("Returned list of all courses related to student {}", studentId);

        return ResponseEntity
                .ok(CollectionModel.of(courseModelAssembler.toCollectionModel(coursesOfStudent),
                        linkTo(methodOn(CourseController.class).getCoursesOfCurrentUserAsStudent())
                                .withRel("courses-of-student")));
    }


}
