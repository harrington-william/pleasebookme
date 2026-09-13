package com.pleasebookme.server.widget.widgets.dto;

import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.enums.WidgetType;

/** Optional list filters. Every field is nullable and means "no constraint". */
public record WidgetFilter(
    WidgetType type,
    WidgetStatus status
) {
    public static WidgetFilter none() {
        return new WidgetFilter(null, null);
    }
}
