package com.codecool.gradebookapi.unit.service;

import com.codecool.gradebookapi.dto.*;
import com.codecool.gradebookapi.model.AssignmentType;
import com.codecool.gradebookapi.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDate;
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
    private SubjectService subjectService;
    @Autowired
    private TeacherService teacherService;
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
                .birthdate(LocalDate.of(1990, 12, 1))
                .build();
        long student1Id = studentService.save(student1).getId();

        StudentDto student2 = StudentDto.builder()
                .firstname("Jane")
                .lastname("Doe")
                .gradeLevel(2)
                .email("janedoe@email.com")
                .address("9351 Morris St., Reisterstown, MD 21136")
                .phone("202-555-0198")
                .birthdate(LocalDate.of(1990, 4, 13))
                .build();
        long student2Id = studentService.save(student2).getId();

        TeacherDto teacher = TeacherDto.builder()
                .firstname("Darrell")
                .lastname("Bowen")
                .email("darrellbowen@email.com")
                .address("3982 Turnpike Drive, Birmingham, AL 35203")
                .phone("619-446-8496")
                .birthdate(LocalDate.of(1984, 2, 1))
                .build();
        long teacherId = teacherService.save(teacher).getId();
        SubjectInput subject1 = SubjectInput.builder()
                .name("Algebra")
                .teacherId(teacherId)
                .build();
        long subject1Id = subjectService.save(subject1).getId();

        SubjectInput subject2 = SubjectInput.builder()
                .name("Biology")
                .teacherId(teacherId)
                .build();
        long subject2Id = subjectService.save(subject2).getId();

        AssignmentInput assignment = AssignmentInput.builder()
                .name("Homework 1")
                .type(AssignmentType.HOMEWORK)
                .deadline(LocalDate.of(2051, 1, 1))
                .subjectId(subject1Id)
                .build();
        long assignmentId = assignmentService.save(assignment).getId();

        entry1 = GradebookInput.builder()
                .studentId(student1Id)
                .subjectId(subject1Id)
                .assignmentId(assignmentId)
                .grade(4)
                .build();
        entry2 = GradebookInput.builder()
                .studentId(student2Id)
                .subjectId(subject2Id)
                .assignmentId(assignmentId)
                .grade(5)
                .build();
    }

    @Test
    @Transactional
    @DisplayName("save should return saved GradebookEntry")
    public void saveShouldReturnSavedGradebookEntry() {
        GradebookOutput entrySaved = gradebookService.save(entry1);

        assertThat(entrySaved.getStudent().getId()).isEqualTo(entry1.getStudentId());
        assertThat(entrySaved.getSubject().getId()).isEqualTo(entry1.getSubjectId());
        assertThat(entrySaved.getAssignment().getId()).isEqualTo(entry1.getAssignmentId());
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
    @DisplayName("when entries related to given Subject exist, findBySubject should return list of GradebookEntries")
    public void whenEntriesRelatedToGivenSubjectExist_findBySubjectShouldReturnListOfEntries() {
        GradebookOutput entrySaved = gradebookService.save(entry1);

        List<GradebookOutput> entriesOfSubject1 = gradebookService.findBySubjectId(entry1.getSubjectId());

        assertThat(entriesOfSubject1).containsExactly(entrySaved);
    }

    @Test
    @Transactional
    @DisplayName("when no entries related to given Subject exist, findBySubject should return empty list")
    public void whenNoEntriesRelatedToGivenSubjectExist_findBySubjectShouldReturnEmptyList() {
        gradebookService.save(entry1);
        gradebookService.save(entry2);

        List<GradebookOutput> entries = gradebookService.findBySubjectId(99L);

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

    @Test
    @Transactional
    @DisplayName("when entry exists with given Student ID and Subject ID, findByStudentIdAndSubjectId should return list of entries")
    public void whenEntryExistsWithGivenStudentIdAndSubjectId_findByStudentIdAndSubjectId_shouldReturnListOfEntries() {
        long student1Id = entry1.getStudentId();
        long student2Id = entry2.getStudentId();
        long subject1Id = entry1.getSubjectId();
        long subject2Id = entry2.getSubjectId();
        GradebookOutput entry1Saved = gradebookService.save(entry1);
        GradebookOutput entry2Saved = gradebookService.save(entry2);

        List<GradebookOutput> gradebookEntries = gradebookService.findByStudentIdAndSubjectId(student1Id, subject1Id);

        assertThat(gradebookEntries).containsExactly(entry1Saved);

        gradebookEntries = gradebookService.findByStudentIdAndSubjectId(student2Id, subject2Id);

        assertThat(gradebookEntries).containsExactly(entry2Saved);
    }

    @Test
    @Transactional
    @DisplayName("when entry does not exist with given Student ID and Subject ID, findByStudentIdAndSubjectId should return empty list")
    public void whenEntryDoesNotExistWithGivenStudentIdAndSubjectId_findByStudentIdAndSubjectId_shouldReturnEmptyList() {
        long student1Id = entry1.getStudentId();
        long student2Id = entry2.getStudentId();
        long subject1Id = entry1.getSubjectId();
        long subject2Id = entry2.getSubjectId();
        gradebookService.save(entry1);
        gradebookService.save(entry2);

        List<GradebookOutput> gradebookEntries =
                gradebookService.findByStudentIdAndSubjectId(student1Id + student2Id, subject1Id + subject2Id);

        assertThat(gradebookEntries).isEmpty();
    }

}
