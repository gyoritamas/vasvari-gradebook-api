package org.vasvari.gradebookapi.jwt;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse implements Serializable {
    private String jwtToken;
}
