package org.trackdev.api.repository;

import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.ProfileAttribute;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileAttributeRepository extends BaseRepositoryLong<ProfileAttribute> {
    
    List<ProfileAttribute> findByProfileId(Long profileId);
    
    Optional<ProfileAttribute> findByNameAndProfileId(String name, Long profileId);
    
    boolean existsByNameAndProfileId(String name, Long profileId);
}
