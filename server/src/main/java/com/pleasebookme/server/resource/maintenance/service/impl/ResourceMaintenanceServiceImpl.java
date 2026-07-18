package com.pleasebookme.server.resource.maintenance.service.impl;

import com.pleasebookme.server.resource.maintenance.dto.ResourceMaintenanceRequest;
import com.pleasebookme.server.resource.maintenance.entity.ResourceMaintenanceEntity;
import com.pleasebookme.server.resource.maintenance.exception.ResourceMaintenanceNotFoundException;
import com.pleasebookme.server.resource.maintenance.repository.ResourceMaintenanceRepository;
import com.pleasebookme.server.resource.maintenance.service.ResourceMaintenanceService;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceMaintenanceServiceImpl implements ResourceMaintenanceService {
    private final ResourceMaintenanceRepository resourceMaintenanceRepository;
    private final ResourceRepository resourceRepository;

    @Override
    public ResourceMaintenanceEntity createResourceMaintenance(ResourceMaintenanceRequest request) {
        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        ResourceMaintenanceEntity resourceMaintenance = ResourceMaintenanceEntity.builder()
            .resource(resource)
            .startTime(request.startTime())
            .endTime(request.endTime())
            .reason(request.reason())
            .status(request.status())
            .build();

        return resourceMaintenanceRepository.save(resourceMaintenance);
    }

    @Override
    public ResourceMaintenanceEntity getResourceMaintenanceById(BigInteger resourceMaintenanceId) {
        return resourceMaintenanceRepository.findById(resourceMaintenanceId)
            .orElseThrow(() -> new ResourceMaintenanceNotFoundException("Resource maintenance not found: " + resourceMaintenanceId));
    }

    @Override
    public List<ResourceMaintenanceEntity> getAllResourceMaintenances() {
        return resourceMaintenanceRepository.findAll();
    }

    @Override
    public ResourceMaintenanceEntity updateResourceMaintenance(
        BigInteger resourceMaintenanceId,
        ResourceMaintenanceRequest request
    ) {
        ResourceMaintenanceEntity resourceMaintenance = getResourceMaintenanceById(resourceMaintenanceId);

        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        resourceMaintenance.setResource(resource);
        resourceMaintenance.setStartTime(request.startTime());
        resourceMaintenance.setEndTime(request.endTime());
        resourceMaintenance.setReason(request.reason());
        resourceMaintenance.setStatus(request.status());

        return resourceMaintenanceRepository.save(resourceMaintenance);
    }

    @Override
    public void deleteResourceMaintenance(BigInteger resourceMaintenanceId) {
        resourceMaintenanceRepository.delete(getResourceMaintenanceById(resourceMaintenanceId));
    }
}
