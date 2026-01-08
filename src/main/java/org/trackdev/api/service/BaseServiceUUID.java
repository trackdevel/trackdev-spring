package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.entity.BaseEntityUUID;
import org.trackdev.api.repository.BaseRepositoryUUID;
import org.trackdev.api.utils.ErrorConstants;

import java.util.List;
import java.util.Optional;

public class BaseServiceUUID<T extends BaseEntityUUID, Repo extends BaseRepositoryUUID<T>> implements IBaseService<T> {

    @Autowired
    Repo repo;

    public T get(String id) {
        Optional<T> oc = this.repo.findById(id);
        if (oc.isEmpty())
            throw new EntityNotFound(ErrorConstants.ENTITY_NOT_EXIST);
        return oc.get();
    }


    public List<T> search(final Specification<T> specification) {
        return repo.findAll(specification);
    }

    public List<T> findAll() {
        return repo.findAll();
    }

    protected Repo repo() { return repo; }

}
