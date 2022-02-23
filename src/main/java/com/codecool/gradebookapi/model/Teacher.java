package com.codecool.gradebookapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String address;
    private String phone;
    private LocalDate birthdate;

    public String getName() {
        return firstname + " " + lastname;
    }
}
