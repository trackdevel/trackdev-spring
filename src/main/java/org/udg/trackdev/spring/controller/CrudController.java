package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.udg.trackdev.spring.controller.exceptions.ControllerException;
import org.udg.trackdev.spring.query.*;
import org.udg.trackdev.spring.service.IBaseService;

import java.util.List;

public class CrudController<T, Service extends IBaseService<T>> extends BaseController {

    @Autowired
    protected Service service;

    public List<T> search(String search) {
        if (search == null)
            return service.findAll();
        Specification<T> specification = this.buildSpecificationFromSearch(search);
        return service.search(specification);
    }

    protected <K> Specification<K> buildSpecificationFromSearch(String search) {
        Specification<K> specification;
        try {
            CriteriaParser parser = new CriteriaParser();
            GenericSpecificationsBuilder<K> specBuilder = new GenericSpecificationsBuilder<>();
            specification = specBuilder.build(parser.parse(search), SearchSpecification::new);
        } catch (Exception ex) {
            throw new ControllerException("Error parsing search parameter");
        }
        return specification;
    }

    protected String scopedSearch(String reducedScopeSearch, String requestSearch) {
        return reducedScopeSearch + (requestSearch != null ? " and ( " + requestSearch + " )" : "");
    }
}
