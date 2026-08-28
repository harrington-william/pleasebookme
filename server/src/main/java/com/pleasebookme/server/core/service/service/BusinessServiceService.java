package com.pleasebookme.server.core.service.service;

import com.pleasebookme.server.core.service.dto.ServiceCreateResult;
import com.pleasebookme.server.core.service.dto.ServiceRequest;
import com.pleasebookme.server.core.service.entity.ServiceEntity;

import java.math.BigInteger;

public interface BusinessServiceService {
    ServiceCreateResult createService(ServiceRequest request);

    ServiceCreateResult getServiceById(BigInteger serviceId);

    ServiceEntity updateService(
        BigInteger serviceId,
        ServiceRequest request
    );

    void deleteService(BigInteger serviceId);
}
