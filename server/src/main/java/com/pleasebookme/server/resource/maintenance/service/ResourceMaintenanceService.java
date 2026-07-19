package com.pleasebookme.server.resource.maintenance.service;

import com.pleasebookme.server.resource.maintenance.dto.ResourceMaintenanceRequest;
import com.pleasebookme.server.resource.maintenance.entity.ResourceMaintenanceEntity;

import java.math.BigInteger;
import java.util.List;

public interface ResourceMaintenanceService {
    ResourceMaintenanceEntity createResourceMaintenance(ResourceMaintenanceRequest request);

    ResourceMaintenanceEntity getResourceMaintenanceById(BigInteger resourceMaintenanceId);

    List<ResourceMaintenanceEntity> getAllResourceMaintenances();

    ResourceMaintenanceEntity updateResourceMaintenance(
        BigInteger resourceMaintenanceId,
        ResourceMaintenanceRequest request
    );

    void deleteResourceMaintenance(BigInteger resourceMaintenanceId);
}
