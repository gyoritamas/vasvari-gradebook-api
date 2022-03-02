package com.codecool.gradebookapi.dto;

import com.codecool.gradebookapi.security.ApplicationUserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Relation(collectionRelation = "users", itemRelation = "user")
@EqualsAndHashCode
public class UserDto {
    private Long id;

    @Schema(example = "username")
    private String username;

    @Schema(example = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    private ApplicationUserRole role;
}
