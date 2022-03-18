package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.assembler.CourseModelAssembler;
import com.codecool.gradebookapi.dto.assembler.StudentModelAssembler;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
import com.codecool.gradebookapi.exception.StudentInUseException;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "student-controller", description = "Operations on students")
@SecurityRequirement(name = "gradebookapi")
@RequiredArgsConstructor
public class StudentController {

    private final UserService userService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;
    private final GradebookService gradebookService;
    private final StudentModelAssembler studentModelAssembler;
    private final CourseModelAssembler courseModelAssembler;

    @GetMapping("/students")
    @Operation(summary = "Lists all students")
    @ApiResponse(responseCode = "200", description = "Returned list of all students")
    public ResponseEntity<CollectionModel<EntityModel<StudentDto>>> getAll() {
        log.info("Returned list of all students");

        return ResponseEntity
                .ok(studentModelAssembler.toCollectionModel(studentService.findAll()));
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "Finds a student by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned student with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID")
    })
    public ResponseEntity<EntityModel<StudentDto>> getById(@PathVariable("id") Long id) {
        StudentDto studentFound = studentService.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
        log.info("Returned student {}", id);

        return ResponseEntity
                .ok(studentModelAssembler.toModel(studentFound));
    }

    @PostMapping("/students")
    @Operation(summary = "Creates a new student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created new student"),
            @ApiResponse(responseCode = "400", description = "Could not create student due to invalid parameters")
    })
    public ResponseEntity<EntityModel<StudentDto>> add(@RequestBody @Valid StudentDto student) {
        StudentDto studentCreated = studentService.save(student);
        EntityModel<StudentDto> entityModel = studentModelAssembler.toModel(studentCreated);
        log.info("Created student with ID {}", studentCreated.getId());

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping("/students/{id}")
    @Operation(summary = "Updates the student given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated student with given ID"),
            @ApiResponse(responseCode = "400", description = "Could not update student due to invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID")
    })
    public ResponseEntity<EntityModel<StudentDto>> update(@RequestBody @Valid StudentDto student,
                                                          @PathVariable("id") Long id) {
        studentService.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
        student.setId(id);
        log.info("Updated student {}", id);

        return ResponseEntity
                .ok(studentModelAssembler.toModel(studentService.save(student)));
    }

    @DeleteMapping("/students/{id}")
    @Operation(summary = "Deletes the student given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted student with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID"),
            @ApiResponse(responseCode = "405", description = "Could not delete student with given ID")
    })
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        studentService.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
        if (!gradebookService.findByStudentId(id).isEmpty()) throw new StudentInUseException(id);
        studentService.deleteById(id);
        log.info("Deleted student {}", id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/students/{id}/classes")
    @Operation(summary = "Lists all classes of the student given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of all classes related to student given by ID"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<CourseOutput>>> getClassesOfStudent(@PathVariable("id") Long id) {
        StudentDto student = studentService.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
        log.info("Returned list of all classes related to student {}", id);

        return ResponseEntity.
                ok(courseModelAssembler.toCollectionModel(studentService.findCoursesOfStudent(student)));
    }

    @GetMapping("/teacher-user/students")
    @Operation(summary = "Find all students the current user as teacher is teacher of")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of students related to current user as teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<StudentDto>>> getStudentsOfCurrentUserAsTeacher(
            @RequestParam(name = "gradeLevel", required = false) Integer gradeLevel,
            @RequestParam(name = "courseId", required = false) Long courseId) {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));

        List<StudentDto> students;
        if (courseId == null) {
            students = courseService.findStudentsOfTeacher(teacher);
        } else {
            // check if this teacher is teaching the course
            CourseOutput course = courseService.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
            if (!course.getTeacher().getId().equals(teacherId))
                throw new RuntimeException(String.format("Teacher %d is not teaching course %d", teacherId, courseId));
            students = courseService.getStudentsOfCourse(courseId);
        }

        if (gradeLevel != null)
            students = students.stream()
                    .filter(student -> student.getGradeLevel().equals(gradeLevel))
                    .collect(Collectors.toList());

        log.info("Returned list of all students related to teacher {}", teacherId);

        return ResponseEntity
                .ok(CollectionModel.of(studentModelAssembler.toCollectionModel(students),
                        linkTo(methodOn(StudentController.class).getStudentsOfCurrentUserAsTeacher(gradeLevel, courseId))
                                .withRel("students-of-teacher")));
    }
}
