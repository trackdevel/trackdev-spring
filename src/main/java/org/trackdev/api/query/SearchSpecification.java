package org.trackdev.api.query;


import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


public class SearchSpecification<T> implements Specification<T> {

    private SpecSearchCriteria criteria;

    public SearchSpecification(final SpecSearchCriteria criteria) {
        super();
        this.criteria = criteria;
    }

    public SpecSearchCriteria getCriteria() {
        return criteria;
    }

    /**
     * Navigate through nested properties (e.g., "task.id" -> root.get("task").get("id"))
     */
    private Path<Object> getPath(Root<T> root, String key) {
        if (!key.contains(".")) {
            return root.get(key);
        }
        
        String[] parts = key.split("\\.");
        Path<Object> path = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return path;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        String key = criteria.getKey();
        Object value = criteria.getValue();
        Path<Object> path = getPath(root, key);
        
        switch (criteria.getOperation()) {
            case EQUALITY:
                if(value == null) {
                    return builder.isNull(path);
                }
                return builder.equal(path, value);
            case NEGATION:
                if(value == null) {
                    return builder.isNotNull(path);
                }
                return builder.notEqual(path, value);
            case GREATER_THAN:
                return builder.greaterThan(path.as(String.class), value.toString());
            case LESS_THAN:
                return builder.lessThan(path.as(String.class), value.toString());
            case LIKE:
                return builder.like(path.as(String.class), value.toString());
            case STARTS_WITH:
                return builder.like(path.as(String.class), value + "%");
            case ENDS_WITH:
                return builder.like(path.as(String.class), "%" + value);
            case CONTAINS:
                return builder.like(path.as(String.class), "%" + value + "%");
            default:
                return null;
        }
    }
}

