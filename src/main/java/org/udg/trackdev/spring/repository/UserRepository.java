package org.udg.trackdev.spring.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.udg.trackdev.spring.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepositoryUUID<User> {

    Optional<User> findByUsername(@Param("username") String username);

    User findByEmail(@Param("email") String email);

    boolean existsByEmail(@Param("email") String email);

    boolean existsByUsername(@Param("username") String username);

}
