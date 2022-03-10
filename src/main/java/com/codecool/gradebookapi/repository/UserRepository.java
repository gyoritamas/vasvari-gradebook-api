package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.User;
import com.codecool.gradebookapi.security.ApplicationUserRole;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepositoryImplementation<User, Long> {
    Optional<User> findByUsername(String username);
}
