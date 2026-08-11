package com.pleasebookme.server.integration.drive.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.integration.drive.dto.DestinationDriveRequest;
import com.pleasebookme.server.integration.drive.entity.DestinationDriveEntity;
import com.pleasebookme.server.integration.drive.exception.DestinationDriveNotFoundException;
import com.pleasebookme.server.integration.drive.repository.DestinationDriveRepository;
import com.pleasebookme.server.integration.drive.service.DestinationDriveService;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.exception.OAuthConnectionNotFoundException;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinationDriveServiceImpl implements DestinationDriveService {
    private final DestinationDriveRepository destinationDriveRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final OAuthConnectionRepository oauthConnectionRepository;

    @Override
    public DestinationDriveEntity createDestinationDrive(DestinationDriveRequest request) {
        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        OAuthConnectionEntity oauthConnection = oauthConnectionRepository.findById(request.oauthConnectionId())
            .orElseThrow(() -> new OAuthConnectionNotFoundException(
                "OAuth connection not found: " + request.oauthConnectionId()
            ));

        DestinationDriveEntity destinationDrive = DestinationDriveEntity.builder()
            .integrationType(request.integrationType())
            .externalId(request.externalId())
            .user(user)
            .service(service)
            .oauthConnection(oauthConnection)
            .build();

        return destinationDriveRepository.save(destinationDrive);
    }

    @Override
    public DestinationDriveEntity getDestinationDriveById(BigInteger destinationDriveId) {
        return destinationDriveRepository.findById(destinationDriveId)
            .orElseThrow(() -> new DestinationDriveNotFoundException(
                "Destination drive not found: " + destinationDriveId
            ));
    }

    @Override
    public List<DestinationDriveEntity> getAllDestinationDrives() {
        return destinationDriveRepository.findAll();
    }

    @Override
    public DestinationDriveEntity updateDestinationDrive(
        BigInteger destinationDriveId,
        DestinationDriveRequest request
    ) {
        DestinationDriveEntity destinationDrive = getDestinationDriveById(destinationDriveId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        OAuthConnectionEntity oauthConnection = oauthConnectionRepository.findById(request.oauthConnectionId())
            .orElseThrow(() -> new OAuthConnectionNotFoundException(
                "OAuth connection not found: " + request.oauthConnectionId()
            ));

        destinationDrive.setIntegrationType(request.integrationType());
        destinationDrive.setExternalId(request.externalId());
        destinationDrive.setUser(user);
        destinationDrive.setService(service);
        destinationDrive.setOauthConnection(oauthConnection);

        return destinationDriveRepository.save(destinationDrive);
    }

    @Override
    public void deleteDestinationDrive(BigInteger destinationDriveId) {
        destinationDriveRepository.delete(getDestinationDriveById(destinationDriveId));
    }
}
