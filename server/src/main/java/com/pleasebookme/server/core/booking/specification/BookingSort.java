package com.pleasebookme.server.core.booking.specification;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public final class BookingSort {
    private static final Set<String> SORTABLE_PROPERTIES = Set.of(
        "startTime",
        "endTime",
        "title",
        "status",
        "createdAt",
        "updatedAt"
    );

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "startTime");

    private static final int MAX_PAGE_SIZE = 100;

    private BookingSort() {}

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
