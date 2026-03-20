package com.innowise.user_service.specification;

import com.innowise.user_service.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> byNameAndSurname(String name, String surname) {
        return (root, query, cb) -> cb.and(
                cb.like(root.get("name"), "%" + name + "%"),
                cb.like(root.get("surname"), "%" + surname + "%")
        );
    }
}
