package com.codecool.gradebookapi.dto.assembler;

import com.codecool.gradebookapi.controller.TeacherController;
import com.codecool.gradebookapi.dto.TeacherDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TeacherModelAssembler implements RepresentationModelAssembler<TeacherDto, EntityModel<TeacherDto>> {

    @Override
    public EntityModel<TeacherDto> toModel(TeacherDto teacher) {
        return EntityModel.of(teacher,
                linkTo(methodOn(TeacherController.class).getById(teacher.getId())).withSelfRel(),
                linkTo(methodOn(TeacherController.class).getAll()).withRel("teachers"));
    }

    @Override
    public CollectionModel<EntityModel<TeacherDto>> toCollectionModel(Iterable<? extends TeacherDto> entities) {
        CollectionModel<EntityModel<TeacherDto>> teachers = RepresentationModelAssembler.super.toCollectionModel(entities);
        teachers.add(linkTo(methodOn(TeacherController.class).getAll()).withSelfRel());

        return teachers;
    }
}
