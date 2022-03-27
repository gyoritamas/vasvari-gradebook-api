package com.codecool.gradebookapi.model.request;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
}
