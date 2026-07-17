package com.pleasebookme.server.widget.widgetorigin.service;

import com.pleasebookme.server.widget.widgetorigin.dto.WidgetOriginRequest;
import com.pleasebookme.server.widget.widgetorigin.entity.WidgetOriginEntity;

import java.math.BigInteger;
import java.util.List;

public interface WidgetOriginService {
    WidgetOriginEntity createWidgetOrigin(WidgetOriginRequest request);

    WidgetOriginEntity getWidgetOriginById(BigInteger widgetOriginId);

    List<WidgetOriginEntity> getAllWidgetOrigins();

    WidgetOriginEntity updateWidgetOrigin(
        BigInteger widgetOriginId,
        WidgetOriginRequest request
    );

    void deleteWidgetOrigin(BigInteger widgetOriginId);
}
