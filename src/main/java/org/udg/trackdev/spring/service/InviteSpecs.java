package org.udg.trackdev.spring.service;

import org.springframework.data.jpa.domain.Specification;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.entity.InviteState;

public class InviteSpecs {
    public static Specification<Invite> isOwnedBy(String userId) {
        return (root, query, builder) -> {
            return builder.equal(root.get("ownerId"), userId);
        };
    }

    public static Specification<Invite> isInvited(String email) {
        return (root, query, builder) -> {
            return builder.equal(root.get("email"), email);
        };
    }

    public static Specification<Invite> isPending() {
        return (root, query, builder) -> {
            return builder.equal(root.get("state"), InviteState.PENDING);
        };
    }

    public static Specification<Invite> notForCourseYear() {
        return (root, query, builder) -> {
            return builder.isNull(root.get("courseYearId"));
        };
    }

    public static Specification<Invite> forCourseYear() {
        return (root, query, builder) -> {
            return builder.isNotNull(root.get("courseYearId"));
        };
    }

    public static Specification<Invite> forCourseYear(Long yearId) {
        return (root, query, builder) -> {
            return builder.equal(root.get("courseYearId"), yearId);
        };
    }
}
