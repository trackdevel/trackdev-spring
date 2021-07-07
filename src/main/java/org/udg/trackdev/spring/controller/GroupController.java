package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.GroupService;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.Collection;

@RestController
@RequestMapping("/groups")
public class GroupController extends BaseController {
    @Autowired
    GroupService service;

    @Autowired
    AccessChecker accessChecker;

    @GetMapping(path = "/{groupId}")
    @JsonView(EntityLevelViews.Basic.class)
    public Group getGroup(Principal principal, @PathVariable(name = "groupId") Long groupId) {
        String userId = super.getUserId(principal);
        Group group = service.get(groupId);
        accessChecker.checkCanViewGroup(group, userId);
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

    @DeleteMapping(path = "/{groupId}")
    @JsonView(EntityLevelViews.Basic.class)
    public ResponseEntity deleteGroup(Principal principal,
                                      @PathVariable(name = "groupId") Long groupId) {
        String userId = super.getUserId(principal);
        service.deleteGroup(groupId, userId);
        return okNoContent();
    }

    static class EditGroup {
        @Size(min = 1, max = Group.NAME_LENGTH)
        public String name;

        public Collection<String> members;
    }
}