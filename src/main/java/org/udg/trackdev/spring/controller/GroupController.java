package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.service.GroupService;

import javax.validation.constraints.NotBlank;
import java.util.Collection;

@RestController
@RequestMapping("/groups")
public class GroupController extends BaseController {

    @Autowired
    GroupService service;

    @GetMapping(path = "/{groupId}")
    public Group getGroup(@PathVariable(name = "groupId") Long groupId) {
        Group group = service.get(groupId);
        return group;
    }
}