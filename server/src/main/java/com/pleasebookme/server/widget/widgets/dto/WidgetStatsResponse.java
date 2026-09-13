package com.pleasebookme.server.widget.widgets.dto;

public record WidgetStatsResponse(
    long total,
    long active,
    long disabled,
    long revoked
) {
}
