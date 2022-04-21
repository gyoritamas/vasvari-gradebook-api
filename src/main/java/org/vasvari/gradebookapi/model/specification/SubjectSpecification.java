package org.vasvari.gradebookapi.model.specification;

import org.vasvari.gradebookapi.model.Subject;
import org.vasvari.gradebookapi.model.request.SubjectRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SubjectSpecification {

    public Specification<Subject> getSubjects(SubjectRequest request) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getName() != null && !request.getName().isEmpty()) {
                Expression<String> name = root.get("name");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(name), "%" + request.getName().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
