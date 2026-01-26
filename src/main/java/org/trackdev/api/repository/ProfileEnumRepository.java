package org.trackdev.api.repository;

import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.ProfileEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileEnumRepository extends BaseRepositoryLong<ProfileEnum> {
    
    List<ProfileEnum> findByProfileId(Long profileId);
    
    Optional<ProfileEnum> findByNameAndProfileId(String name, Long profileId);
    
    boolean existsByNameAndProfileId(String name, Long profileId);
}
