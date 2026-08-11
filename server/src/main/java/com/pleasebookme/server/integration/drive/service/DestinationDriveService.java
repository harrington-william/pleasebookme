package com.pleasebookme.server.integration.drive.service;

import com.pleasebookme.server.integration.drive.dto.DestinationDriveRequest;
import com.pleasebookme.server.integration.drive.entity.DestinationDriveEntity;

import java.math.BigInteger;
import java.util.List;

public interface DestinationDriveService {
    DestinationDriveEntity createDestinationDrive(DestinationDriveRequest request);

    DestinationDriveEntity getDestinationDriveById(BigInteger destinationDriveId);

    List<DestinationDriveEntity> getAllDestinationDrives();

    DestinationDriveEntity updateDestinationDrive(
        BigInteger destinationDriveId,
        DestinationDriveRequest request
    );

    void deleteDestinationDrive(BigInteger destinationDriveId);
}
