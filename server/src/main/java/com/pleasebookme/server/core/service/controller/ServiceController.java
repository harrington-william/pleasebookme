package com.pleasebookme.server.core.service.controller;

import com.pleasebookme.server.core.service.dto.ServiceRequest;
import com.pleasebookme.server.core.service.dto.ServiceResponse;
import com.pleasebookme.server.core.service.service.OrganizationServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {
    private final OrganizationServiceService organizationServiceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(@Valid @RequestBody ServiceRequest request) {
        return ServiceResponse.from(organizationServiceService.createService(request));
    }

    @GetMapping("/{serviceId}")
    public ServiceResponse getService(@PathVariable BigInteger serviceId) {
        return ServiceResponse.from(organizationServiceService.getServiceById(serviceId));
    }

    @GetMapping
    public List<ServiceResponse> getServices() {
        return organizationServiceService.getAllServices().stream()
            .map(ServiceResponse::from)
            .toList();
    }

    @PutMapping("/{serviceId}")
    public ServiceResponse updateService(
        @PathVariable BigInteger serviceId,
        @Valid @RequestBody ServiceRequest request
    ) {
        return ServiceResponse.from(organizationServiceService.updateService(serviceId, request));
    }

    @DeleteMapping("/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable BigInteger serviceId) {
        organizationServiceService.deleteService(serviceId);
    }
}
