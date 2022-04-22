package org.vasvari.gradebookapi.repository;

import org.vasvari.gradebookapi.model.SchoolActorApplicationUserRelation;
import org.vasvari.gradebookapi.security.ApplicationUserRole;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.Optional;

public interface SchoolActorApplicationUserRelationRepository extends JpaRepositoryImplementation<SchoolActorApplicationUserRelation, Long> {
    boolean existsSchoolActorApplicationUserRelationByUserRoleAndSchoolActorId(ApplicationUserRole role, Long schoolActorId);

    Optional<SchoolActorApplicationUserRelation> getByUserRoleAndSchoolActorId(ApplicationUserRole role, Long schoolActorId);

    Optional<SchoolActorApplicationUserRelation> findFirstByAppUserId(Long userId);
}
