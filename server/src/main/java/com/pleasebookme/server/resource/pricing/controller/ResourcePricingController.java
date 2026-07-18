package com.pleasebookme.server.resource.pricing.controller;

import com.pleasebookme.server.resource.pricing.dto.ResourcePricingRequest;
import com.pleasebookme.server.resource.pricing.dto.ResourcePricingResponse;
import com.pleasebookme.server.resource.pricing.service.ResourcePricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resource-pricing")
@RequiredArgsConstructor
public class ResourcePricingController {
    private final ResourcePricingService resourcePricingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourcePricingResponse createResourcePricing(@Valid @RequestBody ResourcePricingRequest request) {
        return ResourcePricingResponse.from(resourcePricingService.createResourcePricing(request));
    }

    @GetMapping("/{resourcePricingId}")
    public ResourcePricingResponse getResourcePricing(@PathVariable BigInteger resourcePricingId) {
        return ResourcePricingResponse.from(resourcePricingService.getResourcePricingById(resourcePricingId));
    }

    @GetMapping
    public List<ResourcePricingResponse> getResourcePricings() {
        return resourcePricingService.getAllResourcePricings().stream()
            .map(ResourcePricingResponse::from)
            .toList();
    }

    @PutMapping("/{resourcePricingId}")
    public ResourcePricingResponse updateResourcePricing(
        @PathVariable BigInteger resourcePricingId,
        @Valid @RequestBody ResourcePricingRequest request
    ) {
        return ResourcePricingResponse.from(resourcePricingService.updateResourcePricing(resourcePricingId, request));
    }

    @DeleteMapping("/{resourcePricingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResourcePricing(@PathVariable BigInteger resourcePricingId) {
        resourcePricingService.deleteResourcePricing(resourcePricingId);
    }
}
