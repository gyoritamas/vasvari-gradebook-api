package com.codecool.gradebookapi.repository;

import com.codecool.gradebookapi.model.SchoolActorApplicationUserRelation;
import com.codecool.gradebookapi.security.ApplicationUserRole;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.Optional;

public interface SchoolActorApplicationUserRelationRepository extends JpaRepositoryImplementation<SchoolActorApplicationUserRelation, Long> {
    boolean existsSchoolActorApplicationUserRelationByUserRoleAndSchoolActorId(ApplicationUserRole role, Long schoolActorId);
    Optional<SchoolActorApplicationUserRelation> findFirstByAppUserId(Long userId);
}
