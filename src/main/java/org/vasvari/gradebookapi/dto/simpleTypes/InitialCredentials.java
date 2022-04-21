package org.vasvari.gradebookapi.dto.simpleTypes;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InitialCredentials {
    private Long userId;
    private String username;
    private String password;
}
