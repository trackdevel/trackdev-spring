package org.udg.trackdev.spring.query;


import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


public class SearchSpecification<T> implements Specification<T> {

    private SpecSearchCriteria criteria;

    public SearchSpecification(final SpecSearchCriteria criteria) {
        super();
        this.criteria = criteria;
    }

    public SpecSearchCriteria getCriteria() {
        return criteria;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        String key = criteria.getKey();
        Object value = criteria.getValue();
        switch (criteria.getOperation()) {
            case EQUALITY:
                if(value == null) {
                    return builder.isNull(root.get(key));
                }
                return builder.equal(root.get(key), value);
            case NEGATION:
                if(value == null) {
                    return builder.isNotNull(root.get(key));
                }
                return builder.notEqual(root.get(key), value);
            case GREATER_THAN:
                return builder.greaterThan(root.get(key), value.toString());
            case LESS_THAN:
                return builder.lessThan(root.get(key), value.toString());
            case LIKE:
                return builder.like(root.get(key), value.toString());
            case STARTS_WITH:
                return builder.like(root.get(key), value + "%");
            case ENDS_WITH:
                return builder.like(root.get(key), "%" + value);
            case CONTAINS:
                return builder.like(root.get(key), "%" + value + "%");
            default:
                return null;
        }
    }
}

