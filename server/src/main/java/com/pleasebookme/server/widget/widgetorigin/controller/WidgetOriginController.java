package com.pleasebookme.server.widget.widgetorigin.controller;

import com.pleasebookme.server.widget.widgetorigin.dto.WidgetOriginRequest;
import com.pleasebookme.server.widget.widgetorigin.dto.WidgetOriginResponse;
import com.pleasebookme.server.widget.widgetorigin.service.WidgetOriginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/widget-origins")
@RequiredArgsConstructor
public class WidgetOriginController {
    private final WidgetOriginService widgetOriginService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WidgetOriginResponse createWidgetOrigin(@Valid @RequestBody WidgetOriginRequest request) {
        return WidgetOriginResponse.from(widgetOriginService.createWidgetOrigin(request));
    }

    @GetMapping("/{widgetOriginId}")
    public WidgetOriginResponse getWidgetOrigin(@PathVariable BigInteger widgetOriginId) {
        return WidgetOriginResponse.from(widgetOriginService.getWidgetOriginById(widgetOriginId));
    }

    @GetMapping
    public List<WidgetOriginResponse> getWidgetOrigins() {
        return widgetOriginService.getAllWidgetOrigins().stream()
            .map(WidgetOriginResponse::from)
            .toList();
    }

    @PutMapping("/{widgetOriginId}")
    public WidgetOriginResponse updateWidgetOrigin(
        @PathVariable BigInteger widgetOriginId,
        @Valid @RequestBody WidgetOriginRequest request
    ) {
        return WidgetOriginResponse.from(widgetOriginService.updateWidgetOrigin(widgetOriginId, request));
    }

    @DeleteMapping("/{widgetOriginId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWidgetOrigin(@PathVariable BigInteger widgetOriginId) {
        widgetOriginService.deleteWidgetOrigin(widgetOriginId);
    }
}
