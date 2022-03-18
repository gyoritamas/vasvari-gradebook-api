package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.CourseOutput;
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
    private final CourseService courseService;
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
                .map(gradebookModelAssembler::toModel)
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
                        linkTo(methodOn(GradebookController.class).getGradesOfCurrentUserAsStudent(null))
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
                        linkTo(methodOn(GradebookController.class).getGradesOfCurrentUserAsStudent(courseId))
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
            @RequestParam(name = "courseId", required = false) Long courseId) {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));

        List<GradebookOutput> gradebookEntries = new ArrayList<>();
        if (courseId == null) {
            // every gradebook entry of every course taught by the teacher
            List<CourseOutput> coursesOfTeacher = courseService.findCoursesOfTeacher(teacher);
            for (CourseOutput course : coursesOfTeacher) {
                gradebookEntries.addAll(gradebookService.findByClassId(course.getId()));
            }
        } else {
            // every gradebook entry of the specific course
            CourseOutput course = courseService.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
            if (!course.getTeacher().getId().equals(teacherId))
                throw new RuntimeException(String.format("Teacher %d is not teaching course %d", teacherId, courseId));
            gradebookEntries.addAll(gradebookService.findByClassId(courseId));
        }

        if (gradeLevel != null)
            gradebookEntries = gradebookEntries.stream()
                    .filter(entry -> studentService.findById(entry.getStudent().getId()).get().getGradeLevel().equals(gradeLevel))
                    .collect(Collectors.toList());

        log.info("Returned list of all gradebook entries related to teacher {}", teacherId);

        return ResponseEntity
                .ok(CollectionModel.of(gradebookModelAssembler.toCollectionModel(gradebookEntries),
                        linkTo(methodOn(GradebookController.class).getGradebookEntriesOfCurrentUserAsTeacher(gradeLevel, courseId))
                                .withRel("gradebook-entries-of-teacher")));

    }
}
