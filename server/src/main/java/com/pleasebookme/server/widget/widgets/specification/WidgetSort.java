package com.pleasebookme.server.widget.widgets.specification;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

/** Sort sanitisation and page-size limits for the widget dashboard list. */
public final class WidgetSort {
    private static final Set<String> SORTABLE_PROPERTIES = Set.of(
        "name",
        "status",
        "type",
        "createdAt",
        "updatedAt"
    );
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");
    private static final int MAX_PAGE_SIZE = 100;

    private WidgetSort() {}

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
