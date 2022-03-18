package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.assembler.CourseModelAssembler;
import com.codecool.gradebookapi.dto.assembler.GradebookModelAssembler;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
import com.codecool.gradebookapi.exception.StudentNotFoundException;
import com.codecool.gradebookapi.exception.StudentNotInCourseException;
import com.codecool.gradebookapi.service.CourseService;
import com.codecool.gradebookapi.service.GradebookService;
import com.codecool.gradebookapi.service.StudentService;
import com.codecool.gradebookapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("student/api")
@Slf4j
@Tag(name = "student-user-controller", description = "Operations as student-user")
@SecurityRequirement(name = "gradebookapi")
@RequiredArgsConstructor
public class StudentUserController {

    private final UserService userService;
    private final CourseService courseService;
    private final StudentService studentService;
    private final GradebookService gradebookService;
    private final CourseModelAssembler courseModelAssembler;
    private final GradebookModelAssembler gradebookModelAssembler;

    @GetMapping("/courses")
    @Operation(summary = "Find all courses the current user as student is enrolled in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of courses related to current user as student"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<CourseOutput>>> getCoursesOfCurrentUserAsStudent() {
        Long studentId = userService.getStudentIdOfCurrentUser();
        StudentDto student = studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        List<CourseOutput> coursesOfStudent = studentService.findCoursesOfStudent(student);

        log.info("Returned list of all courses related to student {}", studentId);

        return ResponseEntity
                .ok(CollectionModel.of(courseModelAssembler.toCollectionModel(coursesOfStudent),
                        linkTo(methodOn(StudentUserController.class).getCoursesOfCurrentUserAsStudent())
                                .withRel("courses-of-student")));
    }

    @GetMapping("/gradebook-entries")
    @Operation(summary = "Finds all gradebook entries related to current user as student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of all gradebook entries related to current user as student"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID"),
            @ApiResponse(responseCode = "404", description = "Could not find course with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradesOfCurrentUserAsStudent(
            @RequestParam(name = "courseId", required = false) Long courseId) {
        Long studentId = userService.getStudentIdOfCurrentUser();
        studentService.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        if (courseId == null)
            return getGradebookEntriesByStudentId(studentId);
        else
            return getGradebookEntriesByStudentIdAndCourseId(courseId, studentId);
    }

    private ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradebookEntriesByStudentId(Long studentId) {
        List<EntityModel<GradebookOutput>> entityModels = gradebookService.findByStudentId(studentId).stream()
                .map(gradebookModelAssembler::toModel)
                .collect(Collectors.toList());

        log.info("Returned list of all gradebook entries related to student {}", studentId);

        return ResponseEntity
                .ok(CollectionModel.of(entityModels,
                        linkTo(methodOn(StudentUserController.class).getGradesOfCurrentUserAsStudent(null))
                                .withRel("gradebook-entries-of-student").expand()));
    }

    private ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradebookEntriesByStudentIdAndCourseId(Long courseId, Long studentId) {
        courseService.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        if (!courseService.isStudentInCourse(studentId, courseId))
            throw new StudentNotInCourseException(studentId, courseId);

        List<EntityModel<GradebookOutput>> entityModels = gradebookService.findByStudentIdAndCourseId(studentId, courseId).stream()
                .map(gradebookModelAssembler::toModel)
                .collect(Collectors.toList());

        log.info("Returned list of all gradebook entries related to student {} and course {}", studentId, courseId);

        return ResponseEntity
                .ok(CollectionModel.of(entityModels,
                        linkTo(methodOn(StudentUserController.class).getGradesOfCurrentUserAsStudent(courseId))
                                .withRel("gradebook-entries-of-student")));
    }
}
