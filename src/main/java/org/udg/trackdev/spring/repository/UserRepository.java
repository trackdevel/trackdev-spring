package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepositoryUUID<User> {

    Optional<User> findByUsername(@Param("username") String username);

    User findByEmail(@Param("email") String email);

    boolean existsByEmail(@Param("email") String email);

    boolean existsByUsername(@Param("username") String username);

    Optional<User> findByGithubName(String githubName);

//    @Query("UPDATE User u SET u.lastLogin=:lastLogin WHERE u.username = ?#{ principal?.username }")
//    @Modifying
//    @Transactional
//    public void updateLastLogin(@Param("lastLogin") Date lastLogin);

}
