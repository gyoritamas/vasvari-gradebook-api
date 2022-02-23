package com.codecool.gradebookapi.model;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    private Teacher teacher;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Student> students;

    public void addStudent(Student student) {
        students.add(student);
    }
}
