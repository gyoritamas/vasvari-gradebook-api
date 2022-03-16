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
@EqualsAndHashCode
@Relation(collectionRelation = "teachers", itemRelation = "teacher")
public class TeacherDto {
    private Long id;

    @NotBlank(message = "Firstname field cannot be empty")
    @Size(min = 2, message = "Firstname must be at least 2 characters long")
    @Schema(example = "Darrell")
    private String firstname;

    @NotBlank(message = "Lastname field cannot be empty")
    @Size(min = 2, message = "Lastname must be at least 2 characters long")
    @Schema(example = "Bowen")
    private String lastname;

    @NotBlank(message = "Email field cannot be empty")
    @Email(message = "Email must be a valid email address")
    @Schema(example = "darrellbowen@email.com")
    private String email;

    @NotBlank(message = "Address field cannot be empty")
    @Schema(example = "3982 Turnpike Drive, Birmingham, AL 35203")
    private String address;

    @NotBlank(message = "Phone field cannot be empty")
    @Pattern(regexp = "^\\+?[\\d \\-()]{7,}",
            message = "Phone must be a valid phone number")
    @Schema(example = "619-446-8496")
    private String phone;

    @Birthdate(message = "Birthdate cannot be empty and must be a valid date of birth")
    @Schema(example = "1984-02-01")
    private String birthdate;

    @JsonIgnore
    public String getName() {
        return firstname + " " + lastname;
    }

}
