package com.pleasebookme.server.resource.assignment.dto;

import com.pleasebookme.server.resource.assignment.entity.ResourceAssignmentEntity;

import java.math.BigInteger;
import java.time.Instant;

public record ResourceAssignmentResponse(
    BigInteger resourceAssignmentId,
    BigInteger resourceId,
    BigInteger membershipId,
    Instant assignedAt,
    Instant releasedAt,
    BigInteger assignedById,
    BigInteger releasedById,
    Boolean isPrimary
) {
    public static ResourceAssignmentResponse from(ResourceAssignmentEntity resourceAssignment) {
        return new ResourceAssignmentResponse(
            resourceAssignment.getResourceAssignmentId(),
            resourceAssignment.getResource().getResourceId(),
            resourceAssignment.getMembership().getMembershipId(),
            resourceAssignment.getAssignedAt(),
            resourceAssignment.getReleasedAt(),
            resourceAssignment.getAssignedBy() != null ? resourceAssignment.getAssignedBy().getUserId() : null,
            resourceAssignment.getReleasedBy() != null ? resourceAssignment.getReleasedBy().getUserId() : null,
            resourceAssignment.getIsPrimary()
        );
    }
}
