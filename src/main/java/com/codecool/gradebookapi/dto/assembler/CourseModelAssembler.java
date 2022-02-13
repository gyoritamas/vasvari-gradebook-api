package com.codecool.gradebookapi.dto.assembler;

import com.codecool.gradebookapi.controller.CourseController;
import com.codecool.gradebookapi.dto.CourseOutput;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CourseModelAssembler implements RepresentationModelAssembler<CourseOutput, EntityModel<CourseOutput>> {

    @Override
    public EntityModel<CourseOutput> toModel(CourseOutput clazz) {
        return EntityModel.of(clazz,
                linkTo(methodOn(CourseController.class).getById(clazz.getId())).withSelfRel(),
                linkTo(methodOn(CourseController.class).getAll()).withRel("classes"));
    }

    @Override
    public CollectionModel<EntityModel<CourseOutput>> toCollectionModel(Iterable<? extends CourseOutput> entities) {
        CollectionModel<EntityModel<CourseOutput>> classes = RepresentationModelAssembler.super.toCollectionModel(entities);

        classes.add(linkTo(methodOn(CourseController.class).getAll()).withSelfRel());

        return classes;
    }
}

