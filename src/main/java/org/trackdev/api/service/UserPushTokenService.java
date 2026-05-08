package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.entity.User;
import org.trackdev.api.entity.UserPushToken;
import org.trackdev.api.repository.UserPushTokenRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class UserPushTokenService
        extends BaseServiceUUID<UserPushToken, UserPushTokenRepository> {

    private static final Logger log = LoggerFactory.getLogger(UserPushTokenService.class);

    @Autowired
    private UserService userService;

    @Transactional
    public UserPushToken registerToken(String userId, String token,
                                       UserPushToken.Platform platform, String deviceId) {
        User user = userService.get(userId);

        Optional<UserPushToken> existing = repo().findByToken(token);
        if (existing.isPresent()) {
            UserPushToken pt = existing.get();
            pt.setUser(user);
            pt.setPlatform(platform);
            pt.setDeviceId(deviceId);
            pt.setLastSeenAt(ZonedDateTime.now(ZoneId.of("UTC")));
            return repo().save(pt);
        }

        UserPushToken pt = new UserPushToken(user, token, platform, deviceId);
        repo().save(pt);
        log.info("Push token registered for user: {}, platform: {}", userId, platform);
        return pt;
    }

    @Transactional(readOnly = true)
    public List<UserPushToken> listTokens(String userId) {
        User user = userService.get(userId);
        return repo().findByUserOrderByLastSeenAtDesc(user);
    }

    @Transactional
    public void unregisterToken(String userId, String token) {
        Optional<UserPushToken> existing = repo().findByToken(token);
        if (existing.isEmpty()) {
            return;
        }
        UserPushToken pt = existing.get();
        if (!pt.getUser().getId().equals(userId)) {
            return;
        }
        repo().deleteByToken(token);
        log.info("Push token unregistered for user: {}", userId);
    }

    @Transactional
    public void deleteStaleTokens(Collection<String> tokens) {
        for (String t : tokens) {
            repo().deleteByToken(t);
        }
        if (!tokens.isEmpty()) {
            log.info("Removed {} stale push token(s) reported by FCM", tokens.size());
        }
    }

    @Transactional(readOnly = true)
    public List<UserPushToken> findByUser(User user) {
        return repo().findByUserOrderByLastSeenAtDesc(user);
    }
}
