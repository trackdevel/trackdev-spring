package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.Role;
import org.udg.trackdev.spring.entity.Task;

@Component
public interface RoleRepository extends JpaRepository<Role, Long> {
}
