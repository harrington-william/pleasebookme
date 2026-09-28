package com.pleasebookme.server.service.widget.embedded.controller;

import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingRequest;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetOrganizationResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetServiceResponse;
import com.pleasebookme.server.service.widget.embedded.service.EmbeddedWidgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController("embeddedWidgetController")
@RequestMapping("/api/v1/widget")
@RequiredArgsConstructor
public class EmbeddedWidgetController {
    private final EmbeddedWidgetService embeddedWidgetService;

    @GetMapping("/organization")
    public ResponseEntity<WidgetOrganizationResponse> getOrganization(
    ) {
        return ResponseEntity.ok(embeddedWidgetService.getOrganization());
    }

    @GetMapping("/services/{serviceSlug}")
    public ResponseEntity<WidgetServiceResponse> getService(
        @PathVariable String serviceSlug
    ) {
        return ResponseEntity.ok(embeddedWidgetService.getService(serviceSlug));
    }

    @GetMapping("/services/{serviceSlug}/slots")
    public ResponseEntity<AvailableSlotsResponse> getSlots(
        @PathVariable String serviceSlug,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(
            embeddedWidgetService.getSlots(serviceSlug, date)
        );
    }

    @PostMapping("/services/{serviceSlug}/bookings")
    public ResponseEntity<WidgetBookingResponse> createBooking(
        @PathVariable String serviceSlug,
        @Valid @RequestBody WidgetBookingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(embeddedWidgetService.createBooking(serviceSlug, request));
    }
}
