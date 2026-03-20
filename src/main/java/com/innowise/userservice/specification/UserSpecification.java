package com.innowise.userservice.specification;

import com.innowise.userservice.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {
    public static Specification<User> byNameAndSurname(String name, String surname) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (StringUtils.hasText(name)) {
                predicate = cb.and(predicate, cb.equal(root.get("name"), name));
            }

            if (StringUtils.hasText(surname)) {
                predicate = cb.and(predicate, cb.equal(root.get("surname"), surname));
            }

            return predicate;
        };
    }
}
