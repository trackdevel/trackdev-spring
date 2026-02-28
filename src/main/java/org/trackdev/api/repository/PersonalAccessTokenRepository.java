package org.trackdev.api.repository;

import org.trackdev.api.entity.PersonalAccessToken;
import org.trackdev.api.entity.User;

import java.util.List;
import java.util.Optional;

public interface PersonalAccessTokenRepository extends BaseRepositoryUUID<PersonalAccessToken> {

    Optional<PersonalAccessToken> findByTokenHashAndRevokedFalse(String tokenHash);

    List<PersonalAccessToken> findByUserAndRevokedFalseOrderByCreatedAtDesc(User user);

    List<PersonalAccessToken> findByUserOrderByCreatedAtDesc(User user);

    void deleteByUser(User user);
}
