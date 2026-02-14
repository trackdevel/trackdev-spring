package org.trackdev.api.repository;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.trackdev.api.entity.DiscordInfo;

@Component
public interface DiscordInfoRepository extends BaseRepositoryUUID<DiscordInfo> {
    Optional<DiscordInfo> findByDiscordId(String discordId);
}
