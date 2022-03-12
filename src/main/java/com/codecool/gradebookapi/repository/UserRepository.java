package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.User;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.Optional;

public interface UserRepository extends JpaRepositoryImplementation<User, Long> {
    Optional<User> findByUsername(String username);
}
