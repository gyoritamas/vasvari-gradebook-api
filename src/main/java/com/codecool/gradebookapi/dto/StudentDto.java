package com.codecool.gradebookapi.dto;

import com.codecool.gradebookapi.validation.Birthdate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Relation(collectionRelation = "students", itemRelation = "student")
@EqualsAndHashCode
public class StudentDto {
    private Long id;

    @NotBlank(message = "Firstname field cannot be empty")
    @Size(min = 2, message = "Firstname must be at least 2 characters long")
    @Schema(example = "John")
    private String firstname;

    @NotBlank(message = "Lastname field cannot be empty")
    @Size(min = 2, message = "Lastname must be at least 2 characters long")
    @Schema(example = "Doe")
    private String lastname;

    @NotNull(message = "Grade level field cannot be empty")
    @Min(value = 1, message = "Grade level must be between 1-12")
    @Max(value = 12, message = "Grade level must be between 1-12")
    @Schema(example = "11")
    private Integer gradeLevel;

    @NotBlank(message = "Email field cannot be empty")
    @Email(message = "Email must be a valid email address")
    @Schema(example = "johndoe@gmail.com")
    private String email;

    @NotBlank(message = "Address field cannot be empty")
    @Schema(example = "666 Armstrong St., Mesa, AZ 85203")
    private String address;

    @NotBlank(message = "Phone field cannot be empty")
    @Pattern(regexp = "^\\+?[\\d \\-()]{7,}",
            message = "Phone must be a valid phone number")
    @Schema(example = "202-555-0198")
    private String phone;

    @Birthdate(message = "Birthdate cannot be empty and must be a valid date of birth")
    @Schema(example = "2005-12-01")
    private String birthdate;

    @JsonIgnore
    public String getName() {
        return firstname + " " + lastname;
    }

}
