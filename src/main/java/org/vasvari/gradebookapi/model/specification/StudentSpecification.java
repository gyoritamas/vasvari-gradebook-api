package org.vasvari.gradebookapi.model.specification;

import org.vasvari.gradebookapi.model.Student;
import org.vasvari.gradebookapi.model.request.StudentRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class StudentSpecification {
    private static final String FULL_NAME_FIRST_PART = "lastname";
    private static final String FULL_NAME_SECOND_PART = "firstname";

    public Specification<Student> getStudents(StudentRequest request) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getName() != null && !request.getName().isEmpty()) {
                Expression<String> fullName = criteriaBuilder.concat(
                        criteriaBuilder.concat(root.get(FULL_NAME_FIRST_PART), " "), root.get(FULL_NAME_SECOND_PART)
                );
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fullName), "%" + request.getName().toLowerCase() + "%"));
            }

            if (request.getGradeLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("gradeLevel"), request.getGradeLevel()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
