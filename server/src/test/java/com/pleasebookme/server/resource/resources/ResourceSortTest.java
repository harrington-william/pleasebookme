package com.pleasebookme.server.resource.resources;

import com.pleasebookme.server.resource.resources.specification.ResourceSort;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceSortTest {

    @Test
    void sanitize_dropsUnknownSortPropertyInsteadOfPassingItToSpringData() {
        Pageable requested = PageRequest.of(0, 20, Sort.by("metadata"));

        Sort sanitized = ResourceSort.sanitize(requested).getSort();

        assertThat(sanitized.getOrderFor("metadata")).isNull();
        assertThat(sanitized.getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void sanitize_keepsWhitelistedPropertyAndDirection() {
        Pageable requested = PageRequest.of(2, 20, Sort.by(Sort.Direction.ASC, "name"));

        Sort sanitized = ResourceSort.sanitize(requested).getSort();

        assertThat(sanitized.getOrderFor("name")).isNotNull();
        assertThat(sanitized.getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void sanitize_keepsOnlyTheWhitelistedHalfOfAMixedSort() {
        Pageable requested = PageRequest.of(0, 20, Sort.by("name").and(Sort.by("organization")));

        Sort sanitized = ResourceSort.sanitize(requested).getSort();

        assertThat(sanitized.getOrderFor("name")).isNotNull();
        assertThat(sanitized.getOrderFor("organization")).isNull();
    }

    @Test
    void sanitize_capsPageSizeAndPreservesPageNumber() {
        Pageable requested = PageRequest.of(3, 5_000);

        Pageable sanitized = ResourceSort.sanitize(requested);

        assertThat(sanitized.getPageSize()).isEqualTo(100);
        assertThat(sanitized.getPageNumber()).isEqualTo(3);
    }

    @Test
    void sanitize_fallsBackToNewestFirstWhenNoSortRequested() {
        Sort sanitized = ResourceSort.sanitize(PageRequest.of(0, 20)).getSort();

        assertThat(sanitized.getOrderFor("createdAt")).isNotNull();
        assertThat(sanitized.getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
