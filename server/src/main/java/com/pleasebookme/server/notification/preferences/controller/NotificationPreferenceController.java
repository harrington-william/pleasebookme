package com.pleasebookme.server.notification.preferences.controller;

import com.pleasebookme.server.notification.preferences.dto.NotificationPreferenceRequest;
import com.pleasebookme.server.notification.preferences.dto.NotificationPreferenceResponse;
import com.pleasebookme.server.notification.preferences.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {
    private final NotificationPreferenceService notificationPreferenceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationPreferenceResponse createNotificationPreference(@Valid @RequestBody NotificationPreferenceRequest request) {
        return NotificationPreferenceResponse.from(notificationPreferenceService.createNotificationPreference(request));
    }

    @GetMapping("/{notificationPreferenceId}")
    public NotificationPreferenceResponse getNotificationPreference(@PathVariable BigInteger notificationPreferenceId) {
        return NotificationPreferenceResponse.from(notificationPreferenceService.getNotificationPreferenceById(notificationPreferenceId));
    }

    @GetMapping
    public List<NotificationPreferenceResponse> getNotificationPreferences() {
        return notificationPreferenceService.getAllNotificationPreferences().stream()
            .map(NotificationPreferenceResponse::from)
            .toList();
    }

    @PutMapping("/{notificationPreferenceId}")
    public NotificationPreferenceResponse updateNotificationPreference(
        @PathVariable BigInteger notificationPreferenceId,
        @Valid @RequestBody NotificationPreferenceRequest request
    ) {
        return NotificationPreferenceResponse.from(notificationPreferenceService.updateNotificationPreference(notificationPreferenceId, request));
    }

    @DeleteMapping("/{notificationPreferenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotificationPreference(@PathVariable BigInteger notificationPreferenceId) {
        notificationPreferenceService.deleteNotificationPreference(notificationPreferenceId);
    }
}
