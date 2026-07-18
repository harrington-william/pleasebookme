package com.pleasebookme.server.resource.resources.controller;

import com.pleasebookme.server.resource.resources.dto.ResourceRequest;
import com.pleasebookme.server.resource.resources.dto.ResourceResponse;
import com.pleasebookme.server.resource.resources.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse createResource(@Valid @RequestBody ResourceRequest request) {
        return ResourceResponse.from(resourceService.createResource(request));
    }

    @GetMapping("/{resourceId}")
    public ResourceResponse getResource(@PathVariable BigInteger resourceId) {
        return ResourceResponse.from(resourceService.getResourceById(resourceId));
    }

    @GetMapping
    public List<ResourceResponse> getResources() {
        return resourceService.getAllResources().stream()
            .map(ResourceResponse::from)
            .toList();
    }

    @PutMapping("/{resourceId}")
    public ResourceResponse updateResource(
        @PathVariable BigInteger resourceId,
        @Valid @RequestBody ResourceRequest request
    ) {
        return ResourceResponse.from(resourceService.updateResource(resourceId, request));
    }

    @DeleteMapping("/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@PathVariable BigInteger resourceId) {
        resourceService.deleteResource(resourceId);
    }
}
