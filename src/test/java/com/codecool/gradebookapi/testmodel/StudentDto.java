package com.codecool.gradebookapi.testmodel;

import lombok.*;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class StudentDto {
    private Long id;
    private String firstname;
    private String lastname;
    private Integer gradeLevel;
    private String email;
    private String address;
    private String phone;
    private String birthdate;

    public String getName() {
        return firstname + " " + lastname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentDto that = (StudentDto) o;
        return Objects.equals(firstname, that.firstname)
                && Objects.equals(lastname, that.lastname)
                && Objects.equals(gradeLevel, that.gradeLevel)
                && Objects.equals(email, that.email)
                && Objects.equals(address, that.address)
                && Objects.equals(phone, that.phone)
                && Objects.equals(birthdate, that.birthdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstname, lastname, gradeLevel, email, address, phone, birthdate);
    }
}