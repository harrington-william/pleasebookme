package com.pleasebookme.server.notification.delivery.controller;

import com.pleasebookme.server.notification.delivery.dto.NotificationDeliveryRequest;
import com.pleasebookme.server.notification.delivery.dto.NotificationDeliveryResponse;
import com.pleasebookme.server.notification.delivery.service.NotificationDeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-deliveries")
@RequiredArgsConstructor
public class NotificationDeliveryController {
    private final NotificationDeliveryService notificationDeliveryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationDeliveryResponse createNotificationDelivery(@Valid @RequestBody NotificationDeliveryRequest request) {
        return NotificationDeliveryResponse.from(notificationDeliveryService.createNotificationDelivery(request));
    }

    @GetMapping("/{notificationDeliveryId}")
    public NotificationDeliveryResponse getNotificationDelivery(@PathVariable BigInteger notificationDeliveryId) {
        return NotificationDeliveryResponse.from(notificationDeliveryService.getNotificationDeliveryById(notificationDeliveryId));
    }

    @GetMapping
    public List<NotificationDeliveryResponse> getNotificationDeliveries() {
        return notificationDeliveryService.getAllNotificationDeliveries().stream()
            .map(NotificationDeliveryResponse::from)
            .toList();
    }

    @PutMapping("/{notificationDeliveryId}")
    public NotificationDeliveryResponse updateNotificationDelivery(
        @PathVariable BigInteger notificationDeliveryId,
        @Valid @RequestBody NotificationDeliveryRequest request
    ) {
        return NotificationDeliveryResponse.from(notificationDeliveryService.updateNotificationDelivery(notificationDeliveryId, request));
    }

    @DeleteMapping("/{notificationDeliveryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotificationDelivery(@PathVariable BigInteger notificationDeliveryId) {
        notificationDeliveryService.deleteNotificationDelivery(notificationDeliveryId);
    }
}
