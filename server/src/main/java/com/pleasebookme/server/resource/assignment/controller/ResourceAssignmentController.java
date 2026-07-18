package com.pleasebookme.server.resource.assignment.controller;

import com.pleasebookme.server.resource.assignment.dto.ResourceAssignmentRequest;
import com.pleasebookme.server.resource.assignment.dto.ResourceAssignmentResponse;
import com.pleasebookme.server.resource.assignment.service.ResourceAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resource-assignments")
@RequiredArgsConstructor
public class ResourceAssignmentController {
    private final ResourceAssignmentService resourceAssignmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceAssignmentResponse createResourceAssignment(@Valid @RequestBody ResourceAssignmentRequest request) {
        return ResourceAssignmentResponse.from(resourceAssignmentService.createResourceAssignment(request));
    }

    @GetMapping("/{resourceAssignmentId}")
    public ResourceAssignmentResponse getResourceAssignment(@PathVariable BigInteger resourceAssignmentId) {
        return ResourceAssignmentResponse.from(resourceAssignmentService.getResourceAssignmentById(resourceAssignmentId));
    }

    @GetMapping
    public List<ResourceAssignmentResponse> getResourceAssignments() {
        return resourceAssignmentService.getAllResourceAssignments().stream()
            .map(ResourceAssignmentResponse::from)
            .toList();
    }

    @PutMapping("/{resourceAssignmentId}")
    public ResourceAssignmentResponse updateResourceAssignment(
        @PathVariable BigInteger resourceAssignmentId,
        @Valid @RequestBody ResourceAssignmentRequest request
    ) {
        return ResourceAssignmentResponse.from(resourceAssignmentService.updateResourceAssignment(resourceAssignmentId, request));
    }

    @DeleteMapping("/{resourceAssignmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResourceAssignment(@PathVariable BigInteger resourceAssignmentId) {
        resourceAssignmentService.deleteResourceAssignment(resourceAssignmentId);
    }
}
