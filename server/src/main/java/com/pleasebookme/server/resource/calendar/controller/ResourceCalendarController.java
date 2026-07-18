package com.pleasebookme.server.resource.calendar.controller;

import com.pleasebookme.server.resource.calendar.dto.ResourceCalendarRequest;
import com.pleasebookme.server.resource.calendar.dto.ResourceCalendarResponse;
import com.pleasebookme.server.resource.calendar.service.ResourceCalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resource-calendars")
@RequiredArgsConstructor
public class ResourceCalendarController {
    private final ResourceCalendarService resourceCalendarService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceCalendarResponse createResourceCalendar(@Valid @RequestBody ResourceCalendarRequest request) {
        return ResourceCalendarResponse.from(resourceCalendarService.createResourceCalendar(request));
    }

    @GetMapping("/{resourceCalendarId}")
    public ResourceCalendarResponse getResourceCalendar(@PathVariable BigInteger resourceCalendarId) {
        return ResourceCalendarResponse.from(resourceCalendarService.getResourceCalendarById(resourceCalendarId));
    }

    @GetMapping
    public List<ResourceCalendarResponse> getResourceCalendars() {
        return resourceCalendarService.getAllResourceCalendars().stream()
            .map(ResourceCalendarResponse::from)
            .toList();
    }

    @PutMapping("/{resourceCalendarId}")
    public ResourceCalendarResponse updateResourceCalendar(
        @PathVariable BigInteger resourceCalendarId,
        @Valid @RequestBody ResourceCalendarRequest request
    ) {
        return ResourceCalendarResponse.from(resourceCalendarService.updateResourceCalendar(resourceCalendarId, request));
    }

    @DeleteMapping("/{resourceCalendarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResourceCalendar(@PathVariable BigInteger resourceCalendarId) {
        resourceCalendarService.deleteResourceCalendar(resourceCalendarId);
    }
}
