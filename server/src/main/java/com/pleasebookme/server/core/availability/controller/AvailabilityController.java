package com.pleasebookme.server.core.availability.controller;

import com.pleasebookme.server.core.availability.dto.AvailabilityRequest;
import com.pleasebookme.server.core.availability.dto.AvailabilityResponse;
import com.pleasebookme.server.core.availability.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityResponse createAvailability(@Valid @RequestBody AvailabilityRequest request) {
        return AvailabilityResponse.from(availabilityService.createAvailability(request));
    }

    @GetMapping("/{availabilityId}")
    public AvailabilityResponse getAvailability(@PathVariable BigInteger availabilityId) {
        return AvailabilityResponse.from(availabilityService.getAvailabilityById(availabilityId));
    }

    @GetMapping
    public List<AvailabilityResponse> getAvailabilities() {
        return availabilityService.getAllAvailabilities().stream()
            .map(AvailabilityResponse::from)
            .toList();
    }

    @PutMapping("/{availabilityId}")
    public AvailabilityResponse updateAvailability(
        @PathVariable BigInteger availabilityId,
        @Valid @RequestBody AvailabilityRequest request
    ) {
        return AvailabilityResponse.from(availabilityService.updateAvailability(availabilityId, request));
    }

    @DeleteMapping("/{availabilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvailability(@PathVariable BigInteger availabilityId) {
        availabilityService.deleteAvailability(availabilityId);
    }
}
