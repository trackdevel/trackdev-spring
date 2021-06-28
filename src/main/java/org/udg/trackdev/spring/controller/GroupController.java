package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.GroupService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.Collection;

@RestController
@RequestMapping("/groups")
public class GroupController extends BaseController {
    @Autowired
    GroupService service;

    @GetMapping(path = "/{groupId}")
    @JsonView(EntityLevelViews.Basic.class)
    public Group getGroup(@PathVariable(name = "groupId") Long groupId) {
        Group group = service.get(groupId);
        return group;
    }

    @PatchMapping(path = "/{groupId}")
    @JsonView(EntityLevelViews.Basic.class)
    public Group editGroup(Principal principal,
                           @PathVariable(name = "groupId") Long groupId,
                           @Valid @RequestBody EditGroup groupRequest) {
        String userId = super.getUserId(principal);
        Group modifiedGroup = service.editGroup(groupId, groupRequest.name, groupRequest.members, userId);
        return modifiedGroup;
    }

    static class EditGroup {
        @Size(min = 1)
        public String name;

        public Collection<String> members;
    }
}