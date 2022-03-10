package com.codecool.gradebookapi.dto;

import com.codecool.gradebookapi.security.ApplicationUserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Relation(collectionRelation = "users", itemRelation = "user")
@EqualsAndHashCode
public class UserDto {
    private Long id;

    @NotBlank(message = "Username field cannot be empty")
    @Schema(example = "username")
    private String username;

    @NotBlank(message = "Password field cannot be empty")
    @Schema(example = "password")
    private String password;

    @NotNull(message = "Role field cannot be empty")
    @Enumerated(EnumType.STRING)
    private ApplicationUserRole role;

    public UserDto(String username, String password, ApplicationUserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
