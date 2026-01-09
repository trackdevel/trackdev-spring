package org.trackdev.api.repository;

import org.trackdev.api.entity.CourseInvite;
import org.trackdev.api.entity.CourseInvite.InviteStatus;

import java.util.Collection;
import java.util.Optional;

public interface CourseInviteRepository extends BaseRepositoryLong<CourseInvite> {

    Optional<CourseInvite> findByToken(String token);

    Collection<CourseInvite> findByCourseId(Long courseId);

    Collection<CourseInvite> findByEmailAndStatus(String email, InviteStatus status);

    Optional<CourseInvite> findByCourseIdAndEmailAndStatus(Long courseId, String email, InviteStatus status);

    Collection<CourseInvite> findByCourseIdAndStatus(Long courseId, InviteStatus status);
}
