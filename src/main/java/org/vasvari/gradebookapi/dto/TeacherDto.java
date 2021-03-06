package org.vasvari.gradebookapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.*;
import java.time.LocalDate;

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

    @NotNull(message = "Birthdate field cannot be empty")
    @Past(message = "Birthdate must be a past date")
    @Schema(example = "1984-02-01")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate birthdate;

    @JsonIgnore
    public String getName() {
        return firstname + " " + lastname;
    }

}
