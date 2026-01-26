package org.trackdev.api.repository;

import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.Profile;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends BaseRepositoryLong<Profile> {
    
    List<Profile> findByOwnerId(String ownerId);
    
    Optional<Profile> findByNameAndOwnerId(String name, String ownerId);
    
    boolean existsByNameAndOwnerId(String name, String ownerId);
}
