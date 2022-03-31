package com.codecool.gradebookapi.model.request;

import com.codecool.gradebookapi.security.ApplicationUserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserRequest {
    private String username;
    private ApplicationUserRole role;
    private Boolean enabled;
}
