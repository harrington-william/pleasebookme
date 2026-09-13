package com.pleasebookme.server.widget.widgets.service;

import com.pleasebookme.server.widget.widgets.credential.WidgetCredentialPair;
import com.pleasebookme.server.widget.widgets.dto.WidgetCreateRequest;
import com.pleasebookme.server.widget.widgets.dto.WidgetDetail;
import com.pleasebookme.server.widget.widgets.dto.WidgetFilter;
import com.pleasebookme.server.widget.widgets.dto.WidgetStatsResponse;
import com.pleasebookme.server.widget.widgets.dto.WidgetUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;

public interface WidgetService {
    WidgetCredentialPair generateCredentials();

    WidgetDetail createWidget(WidgetCreateRequest request);

    WidgetDetail getWidgetById(BigInteger widgetId);

    Page<WidgetDetail> getWidgetsByOrganizationId(
        BigInteger organizationId,
        WidgetFilter filter,
        Pageable pageable
    );

    WidgetStatsResponse getWidgetStatsByOrganizationId(BigInteger organizationId);

    WidgetDetail updateWidget(
        BigInteger widgetId,
        WidgetUpdateRequest request
    );

    void deleteWidget(BigInteger widgetId);
}
