package com.pleasebookme.server.resource.overrides.controller;

import com.pleasebookme.server.resource.overrides.dto.ResourceOverrideRequest;
import com.pleasebookme.server.resource.overrides.dto.ResourceOverrideResponse;
import com.pleasebookme.server.resource.overrides.service.ResourceOverrideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resource-overrides")
@RequiredArgsConstructor
public class ResourceOverrideController {
    private final ResourceOverrideService resourceOverrideService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceOverrideResponse createResourceOverride(@Valid @RequestBody ResourceOverrideRequest request) {
        return ResourceOverrideResponse.from(resourceOverrideService.createResourceOverride(request));
    }

    @GetMapping("/{resourceOverrideId}")
    public ResourceOverrideResponse getResourceOverride(@PathVariable BigInteger resourceOverrideId) {
        return ResourceOverrideResponse.from(resourceOverrideService.getResourceOverrideById(resourceOverrideId));
    }

    @GetMapping
    public List<ResourceOverrideResponse> getResourceOverrides() {
        return resourceOverrideService.getAllResourceOverrides().stream()
            .map(ResourceOverrideResponse::from)
            .toList();
    }

    @PutMapping("/{resourceOverrideId}")
    public ResourceOverrideResponse updateResourceOverride(
        @PathVariable BigInteger resourceOverrideId,
        @Valid @RequestBody ResourceOverrideRequest request
    ) {
        return ResourceOverrideResponse.from(resourceOverrideService.updateResourceOverride(resourceOverrideId, request));
    }

    @DeleteMapping("/{resourceOverrideId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResourceOverride(@PathVariable BigInteger resourceOverrideId) {
        resourceOverrideService.deleteResourceOverride(resourceOverrideId);
    }
}
