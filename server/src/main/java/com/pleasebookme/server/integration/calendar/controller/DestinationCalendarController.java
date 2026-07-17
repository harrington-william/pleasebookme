package com.pleasebookme.server.integration.calendar.controller;

import com.pleasebookme.server.integration.calendar.dto.DestinationCalendarRequest;
import com.pleasebookme.server.integration.calendar.dto.DestinationCalendarResponse;
import com.pleasebookme.server.integration.calendar.service.DestinationCalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/destination-calendars")
@RequiredArgsConstructor
public class DestinationCalendarController {
    private final DestinationCalendarService destinationCalendarService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DestinationCalendarResponse createDestinationCalendar(@Valid @RequestBody DestinationCalendarRequest request) {
        return DestinationCalendarResponse.from(destinationCalendarService.createDestinationCalendar(request));
    }

    @GetMapping("/{destinationCalendarId}")
    public DestinationCalendarResponse getDestinationCalendar(@PathVariable BigInteger destinationCalendarId) {
        return DestinationCalendarResponse.from(destinationCalendarService.getDestinationCalendarById(destinationCalendarId));
    }

    @GetMapping
    public List<DestinationCalendarResponse> getDestinationCalendars() {
        return destinationCalendarService.getAllDestinationCalendars().stream()
            .map(DestinationCalendarResponse::from)
            .toList();
    }

    @PutMapping("/{destinationCalendarId}")
    public DestinationCalendarResponse updateDestinationCalendar(
        @PathVariable BigInteger destinationCalendarId,
        @Valid @RequestBody DestinationCalendarRequest request
    ) {
        return DestinationCalendarResponse.from(destinationCalendarService.updateDestinationCalendar(destinationCalendarId, request));
    }

    @DeleteMapping("/{destinationCalendarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDestinationCalendar(@PathVariable BigInteger destinationCalendarId) {
        destinationCalendarService.deleteDestinationCalendar(destinationCalendarId);
    }
}
