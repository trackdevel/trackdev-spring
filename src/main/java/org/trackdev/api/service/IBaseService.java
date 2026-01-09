package org.trackdev.api.service;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface IBaseService<T> {
    List<T> search(Specification<T> specification);
    List<T> findAll();
}
