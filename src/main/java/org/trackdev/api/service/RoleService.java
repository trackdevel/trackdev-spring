package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Role;
import org.trackdev.api.repository.RoleRepository;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;

    @Transactional
    public Role get(UserType type) {
        List<Role> roles = roleRepository.findByUserType(type);
        Role role = null;
        if(roles.size() == 1) {
            role = roles.get(0);
        } else if (roles.size() == 0) {
            role = new Role(type);
            roleRepository.save(role);
        } else {
            throw new ServiceException(ErrorConstants.UNKNOWN_ROLE);
        }
        return role;
    }
}
