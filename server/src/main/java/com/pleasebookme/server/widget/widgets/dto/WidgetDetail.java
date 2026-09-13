package com.pleasebookme.server.widget.widgets.dto;

import com.pleasebookme.server.widget.widgetorigin.entity.WidgetOriginEntity;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;

public record WidgetDetail(
    WidgetEntity widget,
    WidgetOriginEntity origin
) {
}
