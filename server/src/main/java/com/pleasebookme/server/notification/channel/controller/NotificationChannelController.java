package com.pleasebookme.server.notification.channel.controller;

import com.pleasebookme.server.notification.channel.dto.NotificationChannelRequest;
import com.pleasebookme.server.notification.channel.dto.NotificationChannelResponse;
import com.pleasebookme.server.notification.channel.service.NotificationChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-channels")
@RequiredArgsConstructor
public class NotificationChannelController {
    private final NotificationChannelService notificationChannelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationChannelResponse createNotificationChannel(@Valid @RequestBody NotificationChannelRequest request) {
        return NotificationChannelResponse.from(notificationChannelService.createNotificationChannel(request));
    }

    @GetMapping("/{notificationChannelId}")
    public NotificationChannelResponse getNotificationChannel(@PathVariable BigInteger notificationChannelId) {
        return NotificationChannelResponse.from(notificationChannelService.getNotificationChannelById(notificationChannelId));
    }

    @GetMapping
    public List<NotificationChannelResponse> getNotificationChannels() {
        return notificationChannelService.getAllNotificationChannels().stream()
            .map(NotificationChannelResponse::from)
            .toList();
    }

    @PutMapping("/{notificationChannelId}")
    public NotificationChannelResponse updateNotificationChannel(
        @PathVariable BigInteger notificationChannelId,
        @Valid @RequestBody NotificationChannelRequest request
    ) {
        return NotificationChannelResponse.from(notificationChannelService.updateNotificationChannel(notificationChannelId, request));
    }

    @DeleteMapping("/{notificationChannelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotificationChannel(@PathVariable BigInteger notificationChannelId) {
        notificationChannelService.deleteNotificationChannel(notificationChannelId);
    }
}
