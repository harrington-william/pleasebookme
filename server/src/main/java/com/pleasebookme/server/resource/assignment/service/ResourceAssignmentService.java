package com.pleasebookme.server.resource.assignment.service;

import com.pleasebookme.server.resource.assignment.dto.ResourceAssignmentRequest;
import com.pleasebookme.server.resource.assignment.entity.ResourceAssignmentEntity;

import java.math.BigInteger;
import java.util.List;

public interface ResourceAssignmentService {
    ResourceAssignmentEntity createResourceAssignment(ResourceAssignmentRequest request);

    ResourceAssignmentEntity getResourceAssignmentById(BigInteger resourceAssignmentId);

    List<ResourceAssignmentEntity> getAllResourceAssignments();

    ResourceAssignmentEntity updateResourceAssignment(
        BigInteger resourceAssignmentId,
        ResourceAssignmentRequest request
    );

    void deleteResourceAssignment(BigInteger resourceAssignmentId);
}
