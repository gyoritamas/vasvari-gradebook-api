package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.assembler.CourseModelAssembler;
import com.codecool.gradebookapi.dto.assembler.GradebookModelAssembler;
import com.codecool.gradebookapi.dto.assembler.StudentModelAssembler;
import com.codecool.gradebookapi.exception.CourseNotFoundException;
import com.codecool.gradebookapi.exception.TeacherNotFoundException;
import com.codecool.gradebookapi.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("teacher/api")
@Slf4j
@Tag(name = "teacher-user-controller", description = "Operations as teacher-user")
@SecurityRequirement(name = "gradebookapi")
public class TeacherUserController {
    private final UserService userService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final GradebookService gradebookService;
    private final CourseModelAssembler courseModelAssembler;
    private final StudentModelAssembler studentModelAssembler;
    private final GradebookModelAssembler gradebookModelAssembler;

    public TeacherUserController(UserService userService,
                                 CourseService courseService,
                                 TeacherService teacherService,
                                 StudentService studentService,
                                 GradebookService gradebookService,
                                 CourseModelAssembler courseModelAssembler,
                                 StudentModelAssembler studentModelAssembler,
                                 GradebookModelAssembler gradebookModelAssembler) {
        this.userService = userService;
        this.courseService = courseService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.gradebookService = gradebookService;
        this.courseModelAssembler = courseModelAssembler;
        this.studentModelAssembler = studentModelAssembler;
        this.gradebookModelAssembler = gradebookModelAssembler;
    }

    @GetMapping("/courses")
    @Operation(summary = "Find all courses the current user as teacher is teaching")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of courses related to current user as teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<CourseOutput>>> getCoursesOfCurrentUserAsTeacher() {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));
        List<CourseOutput> coursesOfTeacher = teacherService.findCoursesOfTeacher(teacher);

        log.info("Returned list of all courses related to teacher {}", teacherId);

        return ResponseEntity
                .ok(courseModelAssembler.toCollectionModel(coursesOfTeacher));
    }

    @GetMapping("/students")
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
            students = teacherService.findStudentsOfTeacher(teacher);
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
                .ok(studentModelAssembler.toCollectionModel(students));
    }

    @GetMapping("/gradebook-entries")
    @Operation(summary = "Find all gradebook entries related to the current user as teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of gradebook entries related to current user as teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradebookEntriesOfCurrentUserAsTeacher(
            @RequestParam(name = "gradeLevel", required = false) Integer gradeLevel,
            @RequestParam(name = "courseId", required = false) Long courseId
    ) {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));

        List<GradebookOutput> gradebookEntries = new ArrayList<>();
        if (courseId == null) {
            // every gradebook entry of every course taught by the teacher
            List<CourseOutput> coursesOfTeacher = teacherService.findCoursesOfTeacher(teacher);
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
                .ok(gradebookModelAssembler.toCollectionModel(gradebookEntries));

    }
}
