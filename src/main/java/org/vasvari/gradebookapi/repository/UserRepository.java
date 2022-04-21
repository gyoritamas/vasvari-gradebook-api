package org.vasvari.gradebookapi.repository;

import org.vasvari.gradebookapi.model.ApplicationUser;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.Optional;

public interface UserRepository extends JpaRepositoryImplementation<ApplicationUser, Long> {
    Optional<ApplicationUser> findByUsername(String username);
}
