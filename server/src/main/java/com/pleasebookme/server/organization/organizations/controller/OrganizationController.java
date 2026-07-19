package com.pleasebookme.server.organization.organizations.controller;

import com.pleasebookme.server.organization.organizations.dto.OrganizationRequest;
import com.pleasebookme.server.organization.organizations.dto.OrganizationResponse;
import com.pleasebookme.server.organization.organizations.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse createOrganization(@Valid @RequestBody OrganizationRequest request) {
        return OrganizationResponse.from(organizationService.createOrganization(request));
    }

    @GetMapping("/{organizationId}")
    public OrganizationResponse getOrganization(@PathVariable BigInteger organizationId) {
        return OrganizationResponse.from(organizationService.getOrganizationById(organizationId));
    }

    @GetMapping
    public List<OrganizationResponse> getOrganizations() {
        return organizationService.getAllOrganizations().stream()
            .map(OrganizationResponse::from)
            .toList();
    }

    @PutMapping("/{organizationId}")
    public OrganizationResponse updateOrganization(
        @PathVariable BigInteger organizationId,
        @Valid @RequestBody OrganizationRequest request
    ) {
        return OrganizationResponse.from(organizationService.updateOrganization(organizationId, request));
    }

    @DeleteMapping("/{organizationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganization(@PathVariable BigInteger organizationId) {
        organizationService.deleteOrganization(organizationId);
    }
}
