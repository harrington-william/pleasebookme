package com.pleasebookme.server.resource.attribute.controller;

import com.pleasebookme.server.resource.attribute.dto.ResourceAttributeRequest;
import com.pleasebookme.server.resource.attribute.dto.ResourceAttributeResponse;
import com.pleasebookme.server.resource.attribute.service.ResourceAttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resource-attributes")
@RequiredArgsConstructor
public class ResourceAttributeController {
    private final ResourceAttributeService resourceAttributeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceAttributeResponse createResourceAttribute(@Valid @RequestBody ResourceAttributeRequest request) {
        return ResourceAttributeResponse.from(resourceAttributeService.createResourceAttribute(request));
    }

    @GetMapping("/{resourceId}/{key}")
    public ResourceAttributeResponse getResourceAttribute(
        @PathVariable BigInteger resourceId,
        @PathVariable String key
    ) {
        return ResourceAttributeResponse.from(resourceAttributeService.getResourceAttributeById(resourceId, key));
    }

    @GetMapping
    public List<ResourceAttributeResponse> getResourceAttributes() {
        return resourceAttributeService.getAllResourceAttributes().stream()
            .map(ResourceAttributeResponse::from)
            .toList();
    }

    @PutMapping("/{resourceId}/{key}")
    public ResourceAttributeResponse updateResourceAttribute(
        @PathVariable BigInteger resourceId,
        @PathVariable String key,
        @Valid @RequestBody ResourceAttributeRequest request
    ) {
        return ResourceAttributeResponse.from(resourceAttributeService.updateResourceAttribute(resourceId, key, request));
    }

    @DeleteMapping("/{resourceId}/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResourceAttribute(
        @PathVariable BigInteger resourceId,
        @PathVariable String key
    ) {
        resourceAttributeService.deleteResourceAttribute(resourceId, key);
    }
}
