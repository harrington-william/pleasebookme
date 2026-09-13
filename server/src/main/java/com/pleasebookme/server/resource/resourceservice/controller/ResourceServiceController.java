package com.pleasebookme.server.resource.resourceservice.controller;

import com.pleasebookme.server.resource.resourceservice.dto.ResourceServiceRequest;
import com.pleasebookme.server.resource.resourceservice.dto.ResourceServiceResponse;
import com.pleasebookme.server.resource.resourceservice.entity.ResourceServiceEntity;
import com.pleasebookme.server.resource.resourceservice.service.ResourceServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resource-services")
@RequiredArgsConstructor
public class ResourceServiceController {
    private final ResourceServiceService resourceServiceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceServiceResponse createResourceService(
        @Valid @RequestBody ResourceServiceRequest request
    ) {
        return ResourceServiceResponse.from(
            resourceServiceService.createResourceService(request)
        );
    }

    @GetMapping("/{resourceId}/{serviceId}")
    public ResourceServiceResponse getResourceService(
        @PathVariable BigInteger resourceId,
        @PathVariable BigInteger serviceId
    ) {
        return ResourceServiceResponse.from(
            resourceServiceService.getResourceServiceById(resourceId, serviceId)
        );
    }

    @GetMapping
    public List<ResourceServiceResponse> getResourceServices(
        @RequestParam(required = false) BigInteger resourceId,
        @RequestParam(required = false) BigInteger organizationId
    ) {
        List<ResourceServiceEntity> resourceServices;

        if (resourceId != null) {
            resourceServices = resourceServiceService.getResourceServicesByResourceId(resourceId);
        } else if (organizationId != null) {
            resourceServices = resourceServiceService.getResourceServicesByOrganizationId(organizationId);
        } else {
            resourceServices = resourceServiceService.getAllResourceServices();
        }

        return resourceServices.stream()
            .map(ResourceServiceResponse::from)
            .toList();
    }

    @DeleteMapping("/{resourceId}/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResourceService(
        @PathVariable BigInteger resourceId,
        @PathVariable BigInteger serviceId
    ) {
        resourceServiceService.deleteResourceService(resourceId, serviceId);
    }
}
