package com.codecool.gradebookapi.dto.assembler;

import com.codecool.gradebookapi.controller.AssignmentController;
import com.codecool.gradebookapi.controller.CourseController;
import com.codecool.gradebookapi.controller.GradebookController;
import com.codecool.gradebookapi.controller.StudentController;
import com.codecool.gradebookapi.dto.GradebookOutput;
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
                linkTo(methodOn(StudentController.class).getById(entry.getStudentId())).withRel("student"),
                linkTo(methodOn(CourseController.class).getById(entry.getClassId())).withRel("class"),
                linkTo(methodOn(AssignmentController.class).getById(entry.getAssignmentId())).withRel("assignment"),
                linkTo(methodOn(GradebookController.class).getAll()).withRel("entries"));
    }

    @Override
    public CollectionModel<EntityModel<GradebookOutput>> toCollectionModel(Iterable<? extends GradebookOutput> entities) {
        CollectionModel<EntityModel<GradebookOutput>> entries = RepresentationModelAssembler.super.toCollectionModel(entities);

        entries.add(linkTo(methodOn(GradebookController.class).getAll()).withSelfRel());

        return entries;
    }
}
