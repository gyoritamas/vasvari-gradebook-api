package org.vasvari.gradebookapi.dto.assembler;

import org.vasvari.gradebookapi.controller.AssignmentController;
import org.vasvari.gradebookapi.controller.SubjectController;
import org.vasvari.gradebookapi.controller.GradebookController;
import org.vasvari.gradebookapi.controller.StudentController;
import org.vasvari.gradebookapi.dto.GradebookOutput;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GradebookModelAssembler implements RepresentationModelAssembler<GradebookOutput, EntityModel<GradebookOutput>> {

    @Override
    public EntityModel<GradebookOutput> toModel(GradebookOutput entry) {
        return EntityModel.of(entry,
                linkTo(methodOn(GradebookController.class).getById(entry.getId())).withSelfRel(),
                linkTo(methodOn(StudentController.class).getById(entry.getStudent().getId())).withRel("student"),
                linkTo(methodOn(SubjectController.class).getById(entry.getSubject().getId())).withRel("subject"),
                linkTo(methodOn(AssignmentController.class).getById(entry.getAssignment().getId())).withRel("assignment"),
                linkTo(methodOn(GradebookController.class).getAll()).withRel("entries"));
    }

    @Override
    public CollectionModel<EntityModel<GradebookOutput>> toCollectionModel(Iterable<? extends GradebookOutput> entities) {
        CollectionModel<EntityModel<GradebookOutput>> entries = RepresentationModelAssembler.super.toCollectionModel(entities);

        entries.add(linkTo(methodOn(GradebookController.class).getAll()).withSelfRel());

        return entries;
    }
}
