package org.vasvari.gradebookapi.dto.assembler;

import org.vasvari.gradebookapi.controller.UserController;
import org.vasvari.gradebookapi.dto.simpleTypes.InitialCredentials;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InitialCredentialsModelAssembler implements RepresentationModelAssembler<InitialCredentials, EntityModel<InitialCredentials>> {
    @Override
    public EntityModel<InitialCredentials> toModel(InitialCredentials credentials) {
        return EntityModel.of(credentials,
                linkTo(methodOn(UserController.class).getById(credentials.getUserId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAll()).withRel("users"));
    }

    @Override
    public CollectionModel<EntityModel<InitialCredentials>> toCollectionModel(Iterable<? extends InitialCredentials> entities) {
        CollectionModel<EntityModel<InitialCredentials>> credentials = RepresentationModelAssembler.super.toCollectionModel(entities);

        credentials.add(linkTo(methodOn(UserController.class).getAll()).withSelfRel());

        return credentials;
    }
}
