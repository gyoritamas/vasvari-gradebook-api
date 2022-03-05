package com.codecool.gradebookapi.jwt;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class JwtRequest implements Serializable {

    private String username;

    private String password;
}
