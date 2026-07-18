package com.pleasebookme.server.notification.notifications.controller;

import com.pleasebookme.server.notification.notifications.dto.NotificationRequest;
import com.pleasebookme.server.notification.notifications.dto.NotificationResponse;
import com.pleasebookme.server.notification.notifications.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse createNotification(@Valid @RequestBody NotificationRequest request) {
        return NotificationResponse.from(notificationService.createNotification(request));
    }

    @GetMapping("/{notificationId}")
    public NotificationResponse getNotification(@PathVariable BigInteger notificationId) {
        return NotificationResponse.from(notificationService.getNotificationById(notificationId));
    }

    @GetMapping
    public List<NotificationResponse> getNotifications() {
        return notificationService.getAllNotifications().stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @PutMapping("/{notificationId}")
    public NotificationResponse updateNotification(
        @PathVariable BigInteger notificationId,
        @Valid @RequestBody NotificationRequest request
    ) {
        return NotificationResponse.from(notificationService.updateNotification(notificationId, request));
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable BigInteger notificationId) {
        notificationService.deleteNotification(notificationId);
    }
}
