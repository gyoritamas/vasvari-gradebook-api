package com.codecool.gradebookapi.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentRequest {
    private String name;
    private Integer gradeLevel;
}
