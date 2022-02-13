package com.codecool.gradebookapi.dto.assembler;

import com.codecool.gradebookapi.controller.AssignmentController;
import com.codecool.gradebookapi.dto.AssignmentOutput;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AssignmentModelAssembler implements RepresentationModelAssembler<AssignmentOutput, EntityModel<AssignmentOutput>> {

    @Override
    public EntityModel<AssignmentOutput> toModel(AssignmentOutput assignment) {
        return EntityModel.of(assignment,
                linkTo(methodOn(AssignmentController.class).getById(assignment.getId())).withSelfRel(),
                linkTo(methodOn(AssignmentController.class).getAll()).withRel("assignments"));
    }

    @Override
    public CollectionModel<EntityModel<AssignmentOutput>> toCollectionModel(Iterable<? extends AssignmentOutput> entities) {
        CollectionModel<EntityModel<AssignmentOutput>> assignments = RepresentationModelAssembler.super.toCollectionModel(entities);

        assignments.add(linkTo(methodOn(AssignmentController.class).getAll()).withSelfRel());

        return assignments;
    }
}
