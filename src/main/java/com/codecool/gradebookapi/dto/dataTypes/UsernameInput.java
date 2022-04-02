package com.codecool.gradebookapi.dto.dataTypes;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsernameInput {

    @Schema(example = "johndoe91")
    @Size(min = 4, max = 20)
    @Pattern(regexp = "^[a-zA-Z]([0-9a-zA-Z]){3,20}",
            message = "username can only contain letters and numbers and must start with a letter")
    private String username;
}
