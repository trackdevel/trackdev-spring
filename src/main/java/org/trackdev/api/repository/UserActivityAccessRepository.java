package org.trackdev.api.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.User;
import org.trackdev.api.entity.UserActivityAccess;

import java.util.Optional;

@Repository
public interface UserActivityAccessRepository extends BaseRepositoryLong<UserActivityAccess> {

    /**
     * Find the activity access record for a user
     */
    Optional<UserActivityAccess> findByUser(User user);

    /**
     * Find the activity access record for a user with pessimistic write lock
     * to prevent race conditions during concurrent mark-as-read calls
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uaa FROM UserActivityAccess uaa WHERE uaa.user = :user")
    Optional<UserActivityAccess> findByUserWithLock(@Param("user") User user);

    /**
     * Check if a user has an activity access record
     */
    boolean existsByUser(User user);
}
