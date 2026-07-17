package com.pleasebookme.server.core.attendee.controller;

import com.pleasebookme.server.core.attendee.dto.AttendeeRequest;
import com.pleasebookme.server.core.attendee.dto.AttendeeResponse;
import com.pleasebookme.server.core.attendee.service.AttendeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendees")
@RequiredArgsConstructor
public class AttendeeController {
    private final AttendeeService attendeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendeeResponse createAttendee(@Valid @RequestBody AttendeeRequest request) {
        return AttendeeResponse.from(attendeeService.createAttendee(request));
    }

    @GetMapping("/{attendeeId}")
    public AttendeeResponse getAttendee(@PathVariable BigInteger attendeeId) {
        return AttendeeResponse.from(attendeeService.getAttendeeById(attendeeId));
    }

    @GetMapping
    public List<AttendeeResponse> getAttendees() {
        return attendeeService.getAllAttendees().stream()
            .map(AttendeeResponse::from)
            .toList();
    }

    @PutMapping("/{attendeeId}")
    public AttendeeResponse updateAttendee(
        @PathVariable BigInteger attendeeId,
        @Valid @RequestBody AttendeeRequest request
    ) {
        return AttendeeResponse.from(attendeeService.updateAttendee(attendeeId, request));
    }

    @DeleteMapping("/{attendeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttendee(@PathVariable BigInteger attendeeId) {
        attendeeService.deleteAttendee(attendeeId);
    }
}
