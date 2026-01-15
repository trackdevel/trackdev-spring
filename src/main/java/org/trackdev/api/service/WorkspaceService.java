package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.entity.Workspace;
import org.trackdev.api.repository.WorkspaceRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.List;
import java.util.Optional;

@Service
public class WorkspaceService extends BaseServiceLong<Workspace, WorkspaceRepository> {

    @Autowired
    AccessChecker accessChecker;

    public Workspace getWorkspace(Long id) {
        Optional<Workspace> ow = this.repo.findById(id);
        if (ow.isEmpty())
            throw new EntityNotFound(ErrorConstants.WORKSPACE_NOT_EXIST);
        return ow.get();
    }

    /**
     * Get a workspace with authorization check.
     */
    public Workspace getWorkspace(Long id, String userId) {
        Workspace workspace = getWorkspace(id);
        accessChecker.checkCanViewWorkspace(workspace, userId);
        return workspace;
    }

    public List<Workspace> getAllWorkspaces(String userId) {
        accessChecker.checkCanViewAllWorkspaces(userId);
        return this.repo.findAll();
    }

    @Transactional
    public Workspace createWorkspace(String name, String userId) {
        accessChecker.checkCanCreateWorkspace(userId);
        Workspace workspace = new Workspace(name);
        repo.save(workspace);
        return workspace;
    }

    @Transactional
    public Workspace editWorkspace(Long id, String name, String userId) {
        Workspace workspace = getWorkspace(id);
        accessChecker.checkCanManageWorkspace(workspace, userId);
        if (name != null) {
            workspace.setName(name);
        }
        repo.save(workspace);
        return workspace;
    }

    @Transactional
    public void deleteWorkspace(Long id, String userId) {
        Workspace workspace = getWorkspace(id);
        accessChecker.checkCanManageWorkspace(workspace, userId);
        repo.delete(workspace);
    }
}
