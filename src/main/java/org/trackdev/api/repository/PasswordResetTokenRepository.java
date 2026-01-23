package org.trackdev.api.repository;

import org.trackdev.api.entity.PasswordResetToken;
import org.trackdev.api.entity.User;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends BaseRepositoryUUID<PasswordResetToken> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    
    List<PasswordResetToken> findByUserAndUsedFalse(User user);
    
    void deleteByExpiresAtBefore(ZonedDateTime dateTime);
    
    void deleteByUser(User user);
}
