package com.pleasebookme.server.resource.type.controller;

import com.pleasebookme.server.resource.type.dto.ResourceTypeRequest;
import com.pleasebookme.server.resource.type.dto.ResourceTypeResponse;
import com.pleasebookme.server.resource.type.service.ResourceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resource-types")
@RequiredArgsConstructor
public class ResourceTypeController {
    private final ResourceTypeService resourceTypeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceTypeResponse createResourceType(@Valid @RequestBody ResourceTypeRequest request) {
        return ResourceTypeResponse.from(resourceTypeService.createResourceType(request));
    }

    @GetMapping("/{resourceTypeId}")
    public ResourceTypeResponse getResourceType(@PathVariable BigInteger resourceTypeId) {
        return ResourceTypeResponse.from(resourceTypeService.getResourceTypeById(resourceTypeId));
    }

    @GetMapping
    public List<ResourceTypeResponse> getResourceTypes() {
        return resourceTypeService.getAllResourceTypes().stream()
            .map(ResourceTypeResponse::from)
            .toList();
    }

    @PutMapping("/{resourceTypeId}")
    public ResourceTypeResponse updateResourceType(
        @PathVariable BigInteger resourceTypeId,
        @Valid @RequestBody ResourceTypeRequest request
    ) {
        return ResourceTypeResponse.from(resourceTypeService.updateResourceType(resourceTypeId, request));
    }

    @DeleteMapping("/{resourceTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResourceType(@PathVariable BigInteger resourceTypeId) {
        resourceTypeService.deleteResourceType(resourceTypeId);
    }
}
