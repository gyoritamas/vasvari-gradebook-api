package org.vasvari.gradebookapi.model.specification;

import org.vasvari.gradebookapi.model.GradebookEntry;
import org.vasvari.gradebookapi.model.request.GradebookRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class GradebookEntrySpecification {

    public Specification<GradebookEntry> getGradebookEntries(GradebookRequest request) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStudentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("student").get("id"), request.getStudentId()));
            }

            if (request.getSubjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subject").get("id"), request.getSubjectId()));
            }

            if (request.getAssignmentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignment").get("id"), request.getAssignmentId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
