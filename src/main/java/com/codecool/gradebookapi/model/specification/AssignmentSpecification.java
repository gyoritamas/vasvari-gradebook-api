package com.codecool.gradebookapi.model.specification;

import com.codecool.gradebookapi.model.Assignment;
import com.codecool.gradebookapi.model.AssignmentType;
import com.codecool.gradebookapi.model.request.AssignmentRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AssignmentSpecification {

    public Specification<Assignment> getAssignments(AssignmentRequest request) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getTitle() != null && !request.getTitle().isEmpty()) {
                Expression<String> title = root.get("name");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(title), "%" + request.getTitle().toLowerCase() + "%"));
            }

            if (request.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), request.getType()));
            }

            if (request.getSubjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subject").get("id"), request.getSubjectId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
