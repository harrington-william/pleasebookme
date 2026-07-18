package com.pleasebookme.server.notification.template.controller;

import com.pleasebookme.server.notification.template.dto.NotificationTemplateRequest;
import com.pleasebookme.server.notification.template.dto.NotificationTemplateResponse;
import com.pleasebookme.server.notification.template.service.NotificationTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-templates")
@RequiredArgsConstructor
public class NotificationTemplateController {
    private final NotificationTemplateService notificationTemplateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationTemplateResponse createNotificationTemplate(@Valid @RequestBody NotificationTemplateRequest request) {
        return NotificationTemplateResponse.from(notificationTemplateService.createNotificationTemplate(request));
    }

    @GetMapping("/{notificationTemplateId}")
    public NotificationTemplateResponse getNotificationTemplate(@PathVariable BigInteger notificationTemplateId) {
        return NotificationTemplateResponse.from(notificationTemplateService.getNotificationTemplateById(notificationTemplateId));
    }

    @GetMapping
    public List<NotificationTemplateResponse> getNotificationTemplates() {
        return notificationTemplateService.getAllNotificationTemplates().stream()
            .map(NotificationTemplateResponse::from)
            .toList();
    }

    @PutMapping("/{notificationTemplateId}")
    public NotificationTemplateResponse updateNotificationTemplate(
        @PathVariable BigInteger notificationTemplateId,
        @Valid @RequestBody NotificationTemplateRequest request
    ) {
        return NotificationTemplateResponse.from(notificationTemplateService.updateNotificationTemplate(notificationTemplateId, request));
    }

    @DeleteMapping("/{notificationTemplateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotificationTemplate(@PathVariable BigInteger notificationTemplateId) {
        notificationTemplateService.deleteNotificationTemplate(notificationTemplateId);
    }
}
