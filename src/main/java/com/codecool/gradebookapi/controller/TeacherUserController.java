package com.codecool.gradebookapi.controller;

import com.codecool.gradebookapi.dto.CourseOutput;
import com.codecool.gradebookapi.dto.GradebookOutput;
import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.assembler.CourseModelAssembler;
import com.codecool.gradebookapi.dto.assembler.GradebookModelAssembler;
import com.codecool.gradebookapi.dto.assembler.StudentModelAssembler;
import com.codecool.gradebookapi.exception.TeacherNotFoundException;
import com.codecool.gradebookapi.service.CourseService;
import com.codecool.gradebookapi.service.GradebookService;
import com.codecool.gradebookapi.service.TeacherService;
import com.codecool.gradebookapi.service.UserService;
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
    private final GradebookService gradebookService;
    private final CourseModelAssembler courseModelAssembler;
    private final StudentModelAssembler studentModelAssembler;
    private final GradebookModelAssembler gradebookModelAssembler;

    public TeacherUserController(UserService userService,
                                 CourseService courseService,
                                 TeacherService teacherService,
                                 GradebookService gradebookService,
                                 CourseModelAssembler courseModelAssembler,
                                 StudentModelAssembler studentModelAssembler,
                                 GradebookModelAssembler gradebookModelAssembler) {
        this.userService = userService;
        this.courseService = courseService;
        this.teacherService = teacherService;
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
        List<CourseOutput> coursesOfTeacher = courseService.findCoursesOfTeacher(teacher);

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
    public ResponseEntity<CollectionModel<EntityModel<StudentDto>>> getStudentsOfCurrentUserAsTeacher() {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));
        List<StudentDto> studentsOfTeacher = courseService.findStudentsOfTeacher(teacher);

        log.info("Returned list of all students related to teacher {}", teacherId);

        return ResponseEntity
                .ok(studentModelAssembler.toCollectionModel(studentsOfTeacher));
    }

    @GetMapping("/gradebook-entries")
    @Operation(summary = "Find all gradebook entries related to the current user as teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of gradebook entries related to current user as teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<GradebookOutput>>> getGradebookEntriesOfCurrentUserAsTeacher() {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));
        List<CourseOutput> coursesOfTeacher = courseService.findCoursesOfTeacher(teacher);

        List<GradebookOutput> gradebookEntries = new ArrayList<>();
        for (CourseOutput course : coursesOfTeacher) {
            gradebookEntries.addAll(gradebookService.findByClassId(course.getId()));
        }

        log.info("Returned list of all gradebook entries related to teacher {}", teacherId);

        return ResponseEntity
                .ok(gradebookModelAssembler.toCollectionModel(gradebookEntries));

    }
}
