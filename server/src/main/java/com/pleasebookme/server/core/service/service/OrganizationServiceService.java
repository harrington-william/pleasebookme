package com.pleasebookme.server.core.service.service;

import com.pleasebookme.server.core.service.dto.ServiceRequest;
import com.pleasebookme.server.core.service.entity.ServiceEntity;

import java.math.BigInteger;
import java.util.List;

public interface OrganizationServiceService {
    ServiceEntity createService(ServiceRequest request);

    ServiceEntity getServiceById(BigInteger serviceId);

    List<ServiceEntity> getAllServices();

    ServiceEntity updateService(
        BigInteger serviceId,
        ServiceRequest request
    );

    void deleteService(BigInteger serviceId);
}
