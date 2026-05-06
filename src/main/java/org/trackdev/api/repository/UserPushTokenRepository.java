package org.trackdev.api.repository;

import org.trackdev.api.entity.User;
import org.trackdev.api.entity.UserPushToken;

import java.util.List;
import java.util.Optional;

public interface UserPushTokenRepository extends BaseRepositoryUUID<UserPushToken> {

    Optional<UserPushToken> findByToken(String token);

    List<UserPushToken> findByUserOrderByLastSeenAtDesc(User user);

    void deleteByToken(String token);

    void deleteByUser(User user);
}
