package org.trackdev.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.query.CriteriaParser;
import org.trackdev.api.query.GenericSpecificationsBuilder;
import org.trackdev.api.query.SearchSpecification;
import org.trackdev.api.service.IBaseService;

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
