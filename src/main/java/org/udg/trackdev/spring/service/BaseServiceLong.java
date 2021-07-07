package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.udg.trackdev.spring.controller.exceptions.EntityNotFound;
import org.udg.trackdev.spring.entity.BaseEntityLong;
import org.udg.trackdev.spring.repository.BaseRepositoryLong;

import java.util.List;
import java.util.Optional;

public class BaseServiceLong<T extends BaseEntityLong, Repo extends BaseRepositoryLong<T>> implements IBaseService<T> {

    @Autowired
    Repo repo;

    public T get(Long id) {
        Optional<T> oc = this.repo.findById(id);
        if (oc.isEmpty())
            throw new EntityNotFound("Entity does not exists");
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
