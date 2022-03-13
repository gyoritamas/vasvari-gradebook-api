package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.ApplicationUser;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.Optional;

public interface UserRepository extends JpaRepositoryImplementation<ApplicationUser, Long> {
    Optional<ApplicationUser> findByUsername(String username);
}
