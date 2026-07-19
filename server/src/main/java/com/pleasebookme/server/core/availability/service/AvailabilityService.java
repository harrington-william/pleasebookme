package com.pleasebookme.server.core.availability.service;

import com.pleasebookme.server.core.availability.dto.AvailabilityRequest;
import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;

import java.math.BigInteger;
import java.util.List;

public interface AvailabilityService {
    AvailabilityEntity createAvailability(AvailabilityRequest request);

    AvailabilityEntity getAvailabilityById(BigInteger availabilityId);

    List<AvailabilityEntity> getAllAvailabilities();

    AvailabilityEntity updateAvailability(
        BigInteger availabilityId,
        AvailabilityRequest request
    );

    void deleteAvailability(BigInteger availabilityId);
}
