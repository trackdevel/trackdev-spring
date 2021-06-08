package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.Role;

import java.util.List;

@Component
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByUserType(@Param("userType") UserType userType);
}
