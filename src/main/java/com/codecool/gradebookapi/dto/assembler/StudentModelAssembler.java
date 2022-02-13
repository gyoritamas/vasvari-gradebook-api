package com.codecool.gradebookapi.dto.assembler;

import com.codecool.gradebookapi.controller.StudentController;
import com.codecool.gradebookapi.dto.StudentDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StudentModelAssembler implements RepresentationModelAssembler<StudentDto, EntityModel<StudentDto>> {

    @Override
    public EntityModel<StudentDto> toModel(StudentDto student) {
        return EntityModel.of(student,
                linkTo(methodOn(StudentController.class).getById(student.getId())).withSelfRel(),
                linkTo(methodOn(StudentController.class).getAll()).withRel("students"));
    }

    @Override
    public CollectionModel<EntityModel<StudentDto>> toCollectionModel(Iterable<? extends StudentDto> entities) {
        CollectionModel<EntityModel<StudentDto>> students = RepresentationModelAssembler.super.toCollectionModel(entities);

        students.add(linkTo(methodOn(StudentController.class).getAll()).withSelfRel());

        return students;
    }
}
