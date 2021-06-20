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
}
