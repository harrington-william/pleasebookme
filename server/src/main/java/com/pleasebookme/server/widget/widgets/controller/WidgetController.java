package com.pleasebookme.server.widget.widgets.controller;

import com.pleasebookme.server.widget.widgets.dto.WidgetRequest;
import com.pleasebookme.server.widget.widgets.dto.WidgetResponse;
import com.pleasebookme.server.widget.widgets.service.WidgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/widgets")
@RequiredArgsConstructor
public class WidgetController {
    private final WidgetService widgetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WidgetResponse createWidget(@Valid @RequestBody WidgetRequest request) {
        return WidgetResponse.from(widgetService.createWidget(request));
    }

    @GetMapping("/{widgetId}")
    public WidgetResponse getWidget(@PathVariable BigInteger widgetId) {
        return WidgetResponse.from(widgetService.getWidgetById(widgetId));
    }

    @GetMapping
    public List<WidgetResponse> getWidgets() {
        return widgetService.getAllWidgets().stream()
            .map(WidgetResponse::from)
            .toList();
    }

    @PutMapping("/{widgetId}")
    public WidgetResponse updateWidget(
        @PathVariable BigInteger widgetId,
        @Valid @RequestBody WidgetRequest request
    ) {
        return WidgetResponse.from(widgetService.updateWidget(widgetId, request));
    }

    @DeleteMapping("/{widgetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWidget(@PathVariable BigInteger widgetId) {
        widgetService.deleteWidget(widgetId);
    }
}
