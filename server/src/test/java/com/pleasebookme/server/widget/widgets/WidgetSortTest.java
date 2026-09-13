package com.pleasebookme.server.widget.widgets;

import com.pleasebookme.server.widget.widgets.specification.WidgetSort;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetSortTest {

    @Test
    void sanitize_dropsUnknownSortAndFallsBackToNewestFirst() {
        Sort sanitized = WidgetSort.sanitize(PageRequest.of(0, 20, Sort.by("tenant"))).getSort();

        assertThat(sanitized.getOrderFor("tenant")).isNull();
        assertThat(sanitized.getOrderFor("createdAt")).isNotNull();
        assertThat(sanitized.getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void sanitize_keepsWhitelistedPropertyAndDirection() {
        Pageable requested = PageRequest.of(2, 20, Sort.by(Sort.Direction.ASC, "name"));

        Pageable sanitized = WidgetSort.sanitize(requested);

        assertThat(sanitized.getPageNumber()).isEqualTo(2);
        assertThat(sanitized.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void sanitize_capsPageSizeAtOneHundred() {
        assertThat(WidgetSort.sanitize(PageRequest.of(3, 500)).getPageSize()).isEqualTo(100);
    }
}
