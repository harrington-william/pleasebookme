package com.pleasebookme.server.service.widget.fullpage.controller;

import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingRequest;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetOrganizationResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetServiceResponse;
import com.pleasebookme.server.service.widget.fullpage.service.FullPageWidgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController("fullPageWidgetController")
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class FullPageWidgetController {
    private final FullPageWidgetService fullPageWidgetService;

    @GetMapping("/{organizationSlug}")
    public ResponseEntity<WidgetOrganizationResponse> getOrganization(
        @PathVariable String organizationSlug
    ) {
        return ResponseEntity.ok(fullPageWidgetService.getOrganization(organizationSlug));
    }

    @GetMapping("/{organizationSlug}/service/{serviceSlug}")
    public ResponseEntity<WidgetServiceResponse> getService(
        @PathVariable String organizationSlug,
        @PathVariable String serviceSlug
    ) {
        return ResponseEntity.ok(fullPageWidgetService.getService(organizationSlug, serviceSlug));
    }

    @GetMapping("/{organizationSlug}/service/{serviceSlug}/slots")
    public ResponseEntity<AvailableSlotsResponse> getSlots(
        @PathVariable String organizationSlug,
        @PathVariable String serviceSlug,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(
            fullPageWidgetService.getSlots(organizationSlug, serviceSlug, date)
        );
    }

    @PostMapping("/{organizationSlug}/service/{serviceSlug}/bookings")
    public ResponseEntity<WidgetBookingResponse> createBooking(
        @PathVariable String organizationSlug,
        @PathVariable String serviceSlug,
        @Valid @RequestBody WidgetBookingRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(fullPageWidgetService.createBooking(
                organizationSlug,
                serviceSlug,
                request
            ));
    }
}
