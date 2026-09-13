package com.pleasebookme.server.resource.resources.specification;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

/**
 * Sort sanitisation for the resource list endpoint.
 *
 * <p>{@code Pageable} binds {@code ?sort=} straight from the query string, so an
 * unfiltered property name reaches Spring Data and an unknown one fails at query
 * build time as a 500 rather than a 400. Only the columns the list actually
 * renders are sortable; anything else is dropped.
 */
public final class ResourceSort {
    private static final Set<String> SORTABLE_PROPERTIES = Set.of(
        "name",
        "slug",
        "capacity",
        "status",
        "createdAt",
        "updatedAt"
    );

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private static final int MAX_PAGE_SIZE = 100;

    private ResourceSort() {}

    public static Pageable sanitize(Pageable pageable) {
        List<Sort.Order> allowed = pageable.getSort().stream()
            .filter(order -> SORTABLE_PROPERTIES.contains(order.getProperty()))
            .toList();

        return PageRequest.of(
            pageable.getPageNumber(),
            Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
            allowed.isEmpty() ? DEFAULT_SORT : Sort.by(allowed)
        );
    }
}
