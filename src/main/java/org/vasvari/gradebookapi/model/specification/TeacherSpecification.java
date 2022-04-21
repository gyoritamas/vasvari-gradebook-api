package org.vasvari.gradebookapi.model.specification;

import org.vasvari.gradebookapi.model.Teacher;
import org.vasvari.gradebookapi.model.request.TeacherRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class TeacherSpecification {
    private static final String FULL_NAME_FIRST_PART = "lastname";
    private static final String FULL_NAME_SECOND_PART = "firstname";

    public Specification<Teacher> getTeachers(TeacherRequest request) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getName() != null && !request.getName().isEmpty()) {
                Expression<String> fullName = criteriaBuilder.concat(
                        criteriaBuilder.concat(root.get(FULL_NAME_FIRST_PART), " "), root.get(FULL_NAME_SECOND_PART)
                );
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fullName), "%" + request.getName().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
