package com.codecool.gradebookapi.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequest implements Serializable {

    @NotBlank(message = "Username field cannot be empty")
    @Schema(example = "admin")
    private String username;

    @NotBlank(message = "Password field cannot be empty")
    @Schema(example = "admin")
    private String password;
}
