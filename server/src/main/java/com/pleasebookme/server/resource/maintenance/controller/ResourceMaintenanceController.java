package com.pleasebookme.server.resource.maintenance.controller;

import com.pleasebookme.server.resource.maintenance.dto.ResourceMaintenanceRequest;
import com.pleasebookme.server.resource.maintenance.dto.ResourceMaintenanceResponse;
import com.pleasebookme.server.resource.maintenance.service.ResourceMaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resource-maintenance")
@RequiredArgsConstructor
public class ResourceMaintenanceController {
    private final ResourceMaintenanceService resourceMaintenanceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceMaintenanceResponse createResourceMaintenance(@Valid @RequestBody ResourceMaintenanceRequest request) {
        return ResourceMaintenanceResponse.from(resourceMaintenanceService.createResourceMaintenance(request));
    }

    @GetMapping("/{resourceMaintenanceId}")
    public ResourceMaintenanceResponse getResourceMaintenance(@PathVariable BigInteger resourceMaintenanceId) {
        return ResourceMaintenanceResponse.from(resourceMaintenanceService.getResourceMaintenanceById(resourceMaintenanceId));
    }

    @GetMapping
    public List<ResourceMaintenanceResponse> getResourceMaintenances() {
        return resourceMaintenanceService.getAllResourceMaintenances().stream()
            .map(ResourceMaintenanceResponse::from)
            .toList();
    }

    @PutMapping("/{resourceMaintenanceId}")
    public ResourceMaintenanceResponse updateResourceMaintenance(
        @PathVariable BigInteger resourceMaintenanceId,
        @Valid @RequestBody ResourceMaintenanceRequest request
    ) {
        return ResourceMaintenanceResponse.from(resourceMaintenanceService.updateResourceMaintenance(resourceMaintenanceId, request));
    }

    @DeleteMapping("/{resourceMaintenanceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResourceMaintenance(@PathVariable BigInteger resourceMaintenanceId) {
        resourceMaintenanceService.deleteResourceMaintenance(resourceMaintenanceId);
    }
}
