package org.vasvari.gradebookapi.dto.simpleTypes;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class SimpleStudent {
    private Long id;
    private String firstname;
    private String lastname;
}

