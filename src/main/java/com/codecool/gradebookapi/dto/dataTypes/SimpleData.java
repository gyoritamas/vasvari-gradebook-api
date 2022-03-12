package com.codecool.gradebookapi.dto.dataTypes;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class SimpleData {
    private Long id;
    private String name;
}
