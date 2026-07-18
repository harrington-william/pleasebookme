package com.pleasebookme.server.notification.queue.controller;

import com.pleasebookme.server.notification.queue.dto.NotificationQueueRequest;
import com.pleasebookme.server.notification.queue.dto.NotificationQueueResponse;
import com.pleasebookme.server.notification.queue.service.NotificationQueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-queue")
@RequiredArgsConstructor
public class NotificationQueueController {
    private final NotificationQueueService notificationQueueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationQueueResponse createNotificationQueue(@Valid @RequestBody NotificationQueueRequest request) {
        return NotificationQueueResponse.from(notificationQueueService.createNotificationQueue(request));
    }

    @GetMapping("/{notificationQueueId}")
    public NotificationQueueResponse getNotificationQueue(@PathVariable BigInteger notificationQueueId) {
        return NotificationQueueResponse.from(notificationQueueService.getNotificationQueueById(notificationQueueId));
    }

    @GetMapping
    public List<NotificationQueueResponse> getNotificationQueues() {
        return notificationQueueService.getAllNotificationQueues().stream()
            .map(NotificationQueueResponse::from)
            .toList();
    }

    @PutMapping("/{notificationQueueId}")
    public NotificationQueueResponse updateNotificationQueue(
        @PathVariable BigInteger notificationQueueId,
        @Valid @RequestBody NotificationQueueRequest request
    ) {
        return NotificationQueueResponse.from(notificationQueueService.updateNotificationQueue(notificationQueueId, request));
    }

    @DeleteMapping("/{notificationQueueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotificationQueue(@PathVariable BigInteger notificationQueueId) {
        notificationQueueService.deleteNotificationQueue(notificationQueueId);
    }
}
