package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.User;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface UserRepository extends JpaRepositoryImplementation<User, Long> {

}
