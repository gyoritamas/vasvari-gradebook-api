package org.vasvari.gradebookapi.controller;

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
import org.vasvari.gradebookapi.dto.StudentDto;
import org.vasvari.gradebookapi.dto.SubjectOutput;
import org.vasvari.gradebookapi.dto.TeacherDto;
import org.vasvari.gradebookapi.dto.assembler.StudentModelAssembler;
import org.vasvari.gradebookapi.dto.assembler.SubjectModelAssembler;
import org.vasvari.gradebookapi.exception.StudentInUseException;
import org.vasvari.gradebookapi.exception.StudentNotFoundException;
import org.vasvari.gradebookapi.exception.SubjectNotFoundException;
import org.vasvari.gradebookapi.exception.TeacherNotFoundException;
import org.vasvari.gradebookapi.model.request.GradebookRequest;
import org.vasvari.gradebookapi.model.request.StudentRequest;
import org.vasvari.gradebookapi.service.*;

import javax.validation.Valid;
import java.util.List;

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
    private final SubjectService subjectService;
    private final GradebookService gradebookService;
    private final StudentModelAssembler studentModelAssembler;
    private final SubjectModelAssembler subjectModelAssembler;

    @GetMapping("/students")
    @Operation(summary = "Lists all students")
    @ApiResponse(responseCode = "200", description = "Returned list of all students")
    public ResponseEntity<CollectionModel<EntityModel<StudentDto>>> getAll() {
        List<StudentDto> studentList = studentService.findAll();
        log.info("Returned list of all students");

        return ResponseEntity
                .ok(studentModelAssembler.toCollectionModel(studentList));
    }

    @GetMapping("/students/search")
    @Operation(summary = "Lists all students, filtered by name and grade level")
    @ApiResponse(responseCode = "200", description = "Returned list of students")
    public ResponseEntity<CollectionModel<EntityModel<StudentDto>>> searchStudents(@RequestParam(value = "studentName", required = false) String studentName,
                                                                                   @RequestParam(value = "gradeLevel", required = false) Integer gradeLevel,
                                                                                   @RequestParam(value = "subjectId", required = false) Long subjectId) {

        List<StudentDto> studentList = getStudentsFilteredByNameGradeLevelAndSubject(studentName, gradeLevel, subjectId);

        log.info("Returned list of students with the following filters: studentName={}, gradeLevel={}, subjectId={}", studentName, gradeLevel, subjectId);

        return ResponseEntity
                .ok(CollectionModel.of(studentModelAssembler.toCollectionModel(studentList),
                        linkTo(methodOn(StudentController.class).searchStudents(studentName, gradeLevel, subjectId))
                                .withRel("students-filtered")));
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        studentService.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
        GradebookRequest request = GradebookRequest.builder().studentId(id).build();
        if (!gradebookService.findGradebookEntries(request).isEmpty()) throw new StudentInUseException(id);
        studentService.deleteById(id);
        log.info("Deleted student {}", id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/students/{id}/subjects")
    @Operation(summary = "Lists all subjects of the student given by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of all subjects related to student given by ID"),
            @ApiResponse(responseCode = "404", description = "Could not find student with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<SubjectOutput>>> getSubjectsOfStudent(@PathVariable("id") Long id) {
        StudentDto student = studentService.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
        log.info("Returned list of all subjects related to student {}", id);

        return ResponseEntity.
                ok(subjectModelAssembler.toCollectionModel(studentService.findSubjectsOfStudent(student)));
    }

    @GetMapping("/teacher-user/students")
    @Operation(summary = "Finds all students the current user as teacher is teacher of")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of students related to current user as teacher"),
            @ApiResponse(responseCode = "404", description = "Could not find teacher with given ID")
    })
    public ResponseEntity<CollectionModel<EntityModel<StudentDto>>> getStudentsOfCurrentUserAsTeacher(
            @RequestParam(name = "studentName", required = false) String studentName,
            @RequestParam(name = "gradeLevel", required = false) Integer gradeLevel,
            @RequestParam(name = "subjectId", required = false) Long subjectId) {
        Long teacherId = userService.getTeacherIdOfCurrentUser();
        TeacherDto teacher = teacherService.findById(teacherId).orElseThrow(() -> new TeacherNotFoundException(teacherId));

        // filter by studentName and gradeLevel
        List<StudentDto> studentsFilteredByNameAndGradeLevel =
                getStudentsFilteredByNameGradeLevelAndSubject(studentName, gradeLevel, subjectId);

        // filter by teacher and/or subject
        List<StudentDto> students;
        if (subjectId == null) {
            students = subjectService.findStudentsOfTeacher(teacher);
        } else {
            // check if this teacher is teaching the subject
            SubjectOutput subject = subjectService.findById(subjectId).orElseThrow(() -> new SubjectNotFoundException(subjectId));
            if (!subject.getTeacher().getId().equals(teacherId))
                throw new RuntimeException(String.format("Teacher %d is not teaching subject %d", teacherId, subjectId));
            students = subjectService.getStudentsOfSubject(subjectId);
        }

        students.retainAll(studentsFilteredByNameAndGradeLevel);

        log.info("Returned list of all students related to teacher {}", teacherId);

        return ResponseEntity
                .ok(CollectionModel.of(studentModelAssembler.toCollectionModel(students),
                        linkTo(methodOn(StudentController.class).getStudentsOfCurrentUserAsTeacher(studentName, gradeLevel, subjectId))
                                .withRel("students-of-teacher")));
    }

    private List<StudentDto> getStudentsFilteredByNameGradeLevelAndSubject(String studentName, Integer gradeLevel, Long subjectId) {
        StudentRequest request = new StudentRequest();
        request.setName(studentName);
        request.setGradeLevel(gradeLevel);
        List<StudentDto> studentList = studentService.findStudents(request);
        if (subjectId != null) {
            List<StudentDto> studentsOfSubject = subjectService.getStudentsOfSubject(subjectId);
            studentList.retainAll(studentsOfSubject);
        }

        return studentList;
    }
}
