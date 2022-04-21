package org.vasvari.gradebookapi.dto.assembler;

import org.vasvari.gradebookapi.controller.SubjectController;
import org.vasvari.gradebookapi.dto.SubjectOutput;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SubjectModelAssembler implements RepresentationModelAssembler<SubjectOutput, EntityModel<SubjectOutput>> {

    @Override
    public EntityModel<SubjectOutput> toModel(SubjectOutput subject) {
        return EntityModel.of(subject,
                linkTo(methodOn(SubjectController.class).getById(subject.getId())).withSelfRel(),
                linkTo(methodOn(SubjectController.class).getAll()).withRel("subjects"));
    }

    @Override
    public CollectionModel<EntityModel<SubjectOutput>> toCollectionModel(Iterable<? extends SubjectOutput> entities) {
        CollectionModel<EntityModel<SubjectOutput>> classes = RepresentationModelAssembler.super.toCollectionModel(entities);

        classes.add(linkTo(methodOn(SubjectController.class).getAll()).withSelfRel());

        return classes;
    }
}

