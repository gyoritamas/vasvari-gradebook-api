package com.codecool.gradebookapi.model.request;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class PasswordChangeRequest {
    private String oldPassword;

    @Size(min = 8, max = 20)
    @Pattern(regexp = "^(?=.*[0-9])"
            + "(?=.*[a-z])(?=.*[A-Z])"
            + "(?=\\S+$).{8,20}$",
            message = "password must contain a number, a lower- and an uppercase letter")
    private String newPassword;
}
