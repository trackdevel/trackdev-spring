package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.query.*;
import org.udg.trackdev.spring.service.IBaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrudController<T, Service extends IBaseService<T>> extends BaseController {

    @Autowired
    Service service;

    public List<T> search(String search) {
        if (search == null)
            return service.findAll();
        Specification<T> specification = this.resolveSpecificationFromInfixExpr(search);
        return service.search(specification);
    }

    protected Specification<T> resolveSpecificationFromInfixExpr(String searchParameters) {
        CriteriaParser parser = new CriteriaParser();
        GenericSpecificationsBuilder<T> specBuilder = new GenericSpecificationsBuilder<>();
        return specBuilder.build(parser.parse(searchParameters), SearchSpecification<T>::new);
    }

}
