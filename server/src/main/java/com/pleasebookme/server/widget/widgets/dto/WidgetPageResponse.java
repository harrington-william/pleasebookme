package com.pleasebookme.server.widget.widgets.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record WidgetPageResponse(
    List<WidgetResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static WidgetPageResponse from(Page<WidgetDetail> widgets) {
        return new WidgetPageResponse(
            widgets.getContent().stream().map(WidgetResponse::from).toList(),
            widgets.getNumber(),
            widgets.getSize(),
            widgets.getTotalElements(),
            widgets.getTotalPages()
        );
    }
}
