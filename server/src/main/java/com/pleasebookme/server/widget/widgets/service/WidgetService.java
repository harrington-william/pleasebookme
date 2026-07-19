package com.pleasebookme.server.widget.widgets.service;

import com.pleasebookme.server.widget.widgets.dto.WidgetRequest;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;

import java.math.BigInteger;
import java.util.List;

public interface WidgetService {
    WidgetEntity createWidget(WidgetRequest request);

    WidgetEntity getWidgetById(BigInteger widgetId);

    List<WidgetEntity> getAllWidgets();

    WidgetEntity updateWidget(
        BigInteger widgetId,
        WidgetRequest request
    );

    void deleteWidget(BigInteger widgetId);
}
