package com.pleasebookme.server.core.service.controller;

import com.pleasebookme.server.core.service.dto.ServiceRequest;
import com.pleasebookme.server.core.service.dto.ServiceResponse;
import com.pleasebookme.server.core.service.service.BusinessServiceService;
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
    private final BusinessServiceService businessService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(@Valid @RequestBody ServiceRequest request) {
        var result = businessService.createService(request);
        return ServiceResponse.from(result.service(), result.bookingPolicy());
    }

    @GetMapping("/{serviceId}")
    public ServiceResponse getService(@PathVariable BigInteger serviceId) {
        var result = businessService.getServiceById(serviceId);
        return ServiceResponse.from(result.service(), result.bookingPolicy());
    }

    @GetMapping
    public List<ServiceResponse> getServices(@RequestParam BigInteger organizationId) {
        return businessService.getServicesByOrganizationId(organizationId).stream()
            .map(ServiceResponse::from)
            .toList();
    }

    @PutMapping("/{serviceId}")
    public ServiceResponse updateService(
        @PathVariable BigInteger serviceId,
        @Valid @RequestBody ServiceRequest request
    ) {
        return ServiceResponse.from(businessService.updateService(serviceId, request));
    }

    @DeleteMapping("/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable BigInteger serviceId) {
        businessService.deleteService(serviceId);
    }
}
