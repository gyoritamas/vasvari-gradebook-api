package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.*;
import com.codecool.gradebookapi.service.AssignmentService;
import com.codecool.gradebookapi.service.CourseService;
import com.codecool.gradebookapi.service.GradebookService;
import com.codecool.gradebookapi.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class GradebookServiceTests {

    @Autowired
    private GradebookService gradebookService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private AssignmentService assignmentService;

    private GradebookInput entry1;
    private GradebookInput entry2;

    @BeforeEach
    public void setUp() {
        StudentDto student1 = StudentDto.builder()
                .firstname("John")
                .lastname("Doe")
                .gradeLevel(2)
                .email("johndoe@email.com")
                .address("666 Armstrong St., Mesa, AZ 85203")
                .phone("202-555-0198")
                .birthdate("1990-12-01")
                .build();
        StudentDto student2 = StudentDto.builder()
                .firstname("Jane")
                .lastname("Doe")
                .gradeLevel(2)
                .email("janedoe@email.com")
                .address("9351 Morris St., Reisterstown, MD 21136")
                .phone("202-555-0198")
                .birthdate("1990-04-13")
                .build();
        CourseInput class1 = CourseInput.builder()
                .name("Algebra")
                .build();
        CourseInput class2 = CourseInput.builder()
                .name("Biology")
                .build();
        AssignmentInput assignment = AssignmentInput.builder()
                .name("Homework 1")
                .type("HOMEWORK")
                .build();

        long student1Id = studentService.save(student1).getId();
        long student2Id = studentService.save(student2).getId();
        long class1Id = courseService.save(class1).getId();
        long class2Id = courseService.save(class2).getId();
        long assignmentId = assignmentService.save(assignment).getId();

        entry1 = GradebookInput.builder()
                .studentId(student1Id)
                .courseId(class1Id)
                .assignmentId(assignmentId)
                .grade(4)
                .build();
        entry2 = GradebookInput.builder()
                .studentId(student2Id)
                .courseId(class2Id)
                .assignmentId(assignmentId)
                .grade(5)
                .build();
    }

    @Test
    @Transactional
    @DisplayName("save should return saved GradebookEntry")
    public void saveShouldReturnSavedGradebookEntry() {
        GradebookOutput entrySaved = gradebookService.save(entry1);

        assertThat(entrySaved.getStudentId()).isEqualTo(entry1.getStudentId());
        assertThat(entrySaved.getCourseId()).isEqualTo(entry1.getCourseId());
        assertThat(entrySaved.getAssignmentId()).isEqualTo(entry1.getAssignmentId());
        assertThat(entrySaved.getGrade()).isEqualTo(entry1.getGrade());
    }

    @Test
    @Transactional
    @DisplayName("findAll should return list of GradebookEntries")
    public void findAll_shouldReturnListOfGradebookEntries() {
        GradebookOutput saved1 = gradebookService.save(entry1);
        GradebookOutput saved2 = gradebookService.save(entry2);

        List<GradebookOutput> actualListOfEntries = gradebookService.findAll();

        assertThat(actualListOfEntries).containsExactly(saved1, saved2);
    }

    @Test
    @Transactional
    @DisplayName("when GradebookEntry exists with given ID, findById should return GradebookEntry")
    public void whenEntryWithExistsWithGivenId_findByIdShouldReturnEntry() {
        GradebookOutput saved = gradebookService.save(entry1);

        Optional<GradebookOutput> entryFound = gradebookService.findById(saved.getId());

        assertThat(entryFound).isPresent();
        assertThat(entryFound.get()).isEqualTo(saved);
    }

    @Test
    @Transactional
    @DisplayName("when GradebookEntry with given ID does not exist, findById should return empty Optional")
    public void whenEntryWithGivenIdDoesNotExist_findByIdShouldReturnEmptyOptional() {
        Long id = gradebookService.save(entry2).getId();

        Optional<GradebookOutput> entryFound = gradebookService.findById(id + 1);

        assertThat(entryFound).isEqualTo(Optional.empty());
    }

    @Test
    @Transactional
    @DisplayName("deleteById should delete GradebookEntry with given ID")
    public void deleteById_shouldDeleteEntryWithId() {
        long id = gradebookService.save(entry1).getId();

        gradebookService.deleteById(id);
        Optional<GradebookOutput> entryFound = gradebookService.findById(id);

        assertThat(entryFound).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when entries related to given Student exist, findByStudent should return list of GradebookEntries")
    public void whenEntriesRelatedToGivenStudentExist_findByStudentShouldReturnListOfGradebookEntries() {
        GradebookOutput entrySaved = gradebookService.save(entry1);

        List<GradebookOutput> entriesOfStudent1 = gradebookService.findByStudentId(entry1.getStudentId());

        assertThat(entriesOfStudent1).containsExactly(entrySaved);
    }

    @Test
    @Transactional
    @DisplayName("when no entries related to given Student exist, findByStudent should return empty list")
    public void whenNoEntriesRelatedToGivenStudentExist_findByStudentShouldReturnEmptyList() {
        gradebookService.save(entry1);
        gradebookService.save(entry2);

        List<GradebookOutput> entries = gradebookService.findByStudentId(99L);

        assertThat(entries).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when entries related to given Class exist, findByClass should return list of GradebookEntries")
    public void whenEntriesRelatedToGivenClassExist_findByClassShouldReturnListOfEntries() {
        GradebookOutput entrySaved = gradebookService.save(entry1);

        List<GradebookOutput> entriesOfClass1 = gradebookService.findByClassId(entry1.getCourseId());

        assertThat(entriesOfClass1).containsExactly(entrySaved);
    }

    @Test
    @Transactional
    @DisplayName("when no entries related to given Class exist, findByClass should return empty list")
    public void whenNoEntriesRelatedToGivenClassExist_findByClassShouldReturnEmptyList() {
        gradebookService.save(entry1);
        gradebookService.save(entry2);

        List<GradebookOutput> entries = gradebookService.findByClassId(99L);

        assertThat(entries).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("when entry exists with same IDs as given entry, isDuplicate should return true")
    public void whenEntryExistsWithSameIdsAsGivenEntry_isDuplicateShouldReturnTrue() {
        gradebookService.save(entry1);

        boolean isDuplicate = gradebookService.isDuplicateEntry(entry1);

        assertTrue(isDuplicate);
    }

    @Test
    @Transactional
    @DisplayName("when no entry exists with same IDs as given entry, isDuplicate should return false")
    public void whenNoEntryExistsWithSameIdsAsGivenEntry_isDuplicateShouldReturnFalse() {
        gradebookService.save(entry1);

        boolean isDuplicate = gradebookService.isDuplicateEntry(entry2);

        assertFalse(isDuplicate);
    }

}
