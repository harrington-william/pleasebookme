package com.pleasebookme.server.resource.resources.controller;

import com.pleasebookme.server.resource.enums.ResourceStatus;
import com.pleasebookme.server.resource.resources.dto.ResourceFilter;
import com.pleasebookme.server.resource.resources.dto.ResourceLookupResponse;
import com.pleasebookme.server.resource.resources.dto.ResourceRequest;
import com.pleasebookme.server.resource.resources.dto.ResourceStatsResponse;
import com.pleasebookme.server.resource.resources.dto.ResourcePageResponse;
import com.pleasebookme.server.resource.resources.dto.ResourceResponse;
import com.pleasebookme.server.resource.resources.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/lookup")
    public List<ResourceLookupResponse> getResourceLookup(@RequestParam BigInteger organizationId) {
        return resourceService.getResourceLookupByOrganizationId(organizationId).stream()
            .map(ResourceLookupResponse::from)
            .toList();
    }

    @GetMapping("/{resourceId}")
    public ResourceResponse getResource(@PathVariable BigInteger resourceId) {
        return ResourceResponse.from(resourceService.getResourceById(resourceId));
    }

    @GetMapping
    public ResourcePageResponse getResources(
        @RequestParam BigInteger organizationId,
        @RequestParam(required = false) BigInteger resourceTypeId,
        @RequestParam(required = false) ResourceStatus status,
        @RequestParam(required = false) String q,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        ResourceFilter filter = new ResourceFilter(resourceTypeId, status, q);

        return ResourcePageResponse.from(
            resourceService.getResourcesByOrganizationId(organizationId, filter, pageable)
        );
    }

    @GetMapping("/stats")
    public ResourceStatsResponse getResourceStats(@RequestParam BigInteger organizationId) {
        return resourceService.getResourceStatsByOrganizationId(organizationId);
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
