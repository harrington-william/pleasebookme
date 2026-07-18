package com.pleasebookme.server.resource.assignment.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.resource.assignment.dto.ResourceAssignmentRequest;
import com.pleasebookme.server.resource.assignment.entity.ResourceAssignmentEntity;
import com.pleasebookme.server.resource.assignment.exception.ResourceAssignmentNotFoundException;
import com.pleasebookme.server.resource.assignment.repository.ResourceAssignmentRepository;
import com.pleasebookme.server.resource.assignment.service.ResourceAssignmentService;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceAssignmentServiceImpl implements ResourceAssignmentService {
    private final ResourceAssignmentRepository resourceAssignmentRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Override
    public ResourceAssignmentEntity createResourceAssignment(ResourceAssignmentRequest request) {
        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        MembershipEntity membership = MembershipEntity.builder()
            .membershipId(request.membershipId())
            .build();

        ResourceAssignmentEntity.ResourceAssignmentEntityBuilder resourceAssignment = ResourceAssignmentEntity.builder()
            .resource(resource)
            .membership(membership)
            .releasedAt(request.releasedAt())
            .assignedBy(resolveUser(request.assignedById()))
            .releasedBy(resolveUser(request.releasedById()));

        if (request.isPrimary() != null) resourceAssignment.isPrimary(request.isPrimary());

        return resourceAssignmentRepository.save(resourceAssignment.build());
    }

    @Override
    public ResourceAssignmentEntity getResourceAssignmentById(BigInteger resourceAssignmentId) {
        return resourceAssignmentRepository.findById(resourceAssignmentId)
            .orElseThrow(() -> new ResourceAssignmentNotFoundException("Resource assignment not found: " + resourceAssignmentId));
    }

    @Override
    public List<ResourceAssignmentEntity> getAllResourceAssignments() {
        return resourceAssignmentRepository.findAll();
    }

    @Override
    public ResourceAssignmentEntity updateResourceAssignment(
        BigInteger resourceAssignmentId,
        ResourceAssignmentRequest request
    ) {
        ResourceAssignmentEntity resourceAssignment = getResourceAssignmentById(resourceAssignmentId);

        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        MembershipEntity membership = MembershipEntity.builder()
            .membershipId(request.membershipId())
            .build();

        resourceAssignment.setResource(resource);
        resourceAssignment.setMembership(membership);
        resourceAssignment.setReleasedAt(request.releasedAt());
        resourceAssignment.setAssignedBy(resolveUser(request.assignedById()));
        resourceAssignment.setReleasedBy(resolveUser(request.releasedById()));

        if (request.isPrimary() != null) resourceAssignment.setIsPrimary(request.isPrimary());

        return resourceAssignmentRepository.save(resourceAssignment);
    }

    @Override
    public void deleteResourceAssignment(BigInteger resourceAssignmentId) {
        resourceAssignmentRepository.delete(getResourceAssignmentById(resourceAssignmentId));
    }

    private UserEntity resolveUser(BigInteger userId) {
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}
