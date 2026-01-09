package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.entity.Role;

import java.util.List;

@Component
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByUserType(@Param("userType") UserType userType);
}
