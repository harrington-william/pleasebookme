package com.pleasebookme.server.widget.widgets.controller;

import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.enums.WidgetType;
import com.pleasebookme.server.widget.widgets.credential.WidgetCredentialPair;
import com.pleasebookme.server.widget.widgets.dto.WidgetCreateRequest;
import com.pleasebookme.server.widget.widgets.dto.WidgetCredentialResponse;
import com.pleasebookme.server.widget.widgets.dto.WidgetFilter;
import com.pleasebookme.server.widget.widgets.dto.WidgetPageResponse;
import com.pleasebookme.server.widget.widgets.dto.WidgetResponse;
import com.pleasebookme.server.widget.widgets.dto.WidgetStatsResponse;
import com.pleasebookme.server.widget.widgets.dto.WidgetUpdateRequest;
import com.pleasebookme.server.widget.widgets.service.WidgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/v1/widgets")
@RequiredArgsConstructor
public class WidgetController {
    private final WidgetService widgetService;

    @PostMapping("/credentials")
    public WidgetCredentialResponse generateCredentials() {
        WidgetCredentialPair credentials = widgetService.generateCredentials();
        return new WidgetCredentialResponse(credentials.publicKey(), credentials.secretKey());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WidgetResponse createWidget(@Valid @RequestBody WidgetCreateRequest request) {
        return WidgetResponse.from(widgetService.createWidget(request));
    }

    @GetMapping("/stats")
    public WidgetStatsResponse getWidgetStats(@RequestParam BigInteger organizationId) {
        return widgetService.getWidgetStatsByOrganizationId(organizationId);
    }

    @GetMapping
    public WidgetPageResponse getWidgets(
        @RequestParam BigInteger organizationId,
        @RequestParam(required = false) WidgetType type,
        @RequestParam(required = false) WidgetStatus status,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        WidgetFilter filter = new WidgetFilter(type, status);
        return WidgetPageResponse.from(
            widgetService.getWidgetsByOrganizationId(organizationId, filter, pageable)
        );
    }

    @GetMapping("/{widgetId}")
    public WidgetResponse getWidget(@PathVariable BigInteger widgetId) {
        return WidgetResponse.from(widgetService.getWidgetById(widgetId));
    }

    @PutMapping("/{widgetId}")
    public WidgetResponse updateWidget(
        @PathVariable BigInteger widgetId,
        @Valid @RequestBody WidgetUpdateRequest request
    ) {
        return WidgetResponse.from(widgetService.updateWidget(widgetId, request));
    }

    @DeleteMapping("/{widgetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWidget(@PathVariable BigInteger widgetId) {
        widgetService.deleteWidget(widgetId);
    }
}
