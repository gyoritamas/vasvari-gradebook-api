package com.codecool.gradebookapi.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class JwtRequest implements Serializable {
    @Schema(example = "user")
    private String username;
    @Schema(example = "password")
    private String password;
}
