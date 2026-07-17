package com.pleasebookme.server.integration.sheets.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.integration.sheets.dto.DestinationSheetsRequest;
import com.pleasebookme.server.integration.sheets.entity.DestinationSheetsEntity;
import com.pleasebookme.server.integration.sheets.exception.DestinationSheetsNotFoundException;
import com.pleasebookme.server.integration.sheets.repository.DestinationSheetsRepository;
import com.pleasebookme.server.integration.sheets.service.DestinationSheetsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinationSheetsServiceImpl implements DestinationSheetsService {
    private final DestinationSheetsRepository destinationSheetsRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    @Override
    public DestinationSheetsEntity createDestinationSheets(DestinationSheetsRequest request) {
        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        DestinationSheetsEntity destinationSheets = DestinationSheetsEntity.builder()
            .integrationType(request.integrationType())
            .externalId(request.externalId())
            .user(user)
            .service(service)
            .build();

        return destinationSheetsRepository.save(destinationSheets);
    }

    @Override
    public DestinationSheetsEntity getDestinationSheetsById(BigInteger destinationSheetsId) {
        return destinationSheetsRepository.findById(destinationSheetsId)
            .orElseThrow(() -> new DestinationSheetsNotFoundException(
                "Destination sheets not found: " + destinationSheetsId
            ));
    }

    @Override
    public List<DestinationSheetsEntity> getAllDestinationSheets() {
        return destinationSheetsRepository.findAll();
    }

    @Override
    public DestinationSheetsEntity updateDestinationSheets(
        BigInteger destinationSheetsId,
        DestinationSheetsRequest request
    ) {
        DestinationSheetsEntity destinationSheets = getDestinationSheetsById(destinationSheetsId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        destinationSheets.setIntegrationType(request.integrationType());
        destinationSheets.setExternalId(request.externalId());
        destinationSheets.setUser(user);
        destinationSheets.setService(service);

        return destinationSheetsRepository.save(destinationSheets);
    }

    @Override
    public void deleteDestinationSheets(BigInteger destinationSheetsId) {
        destinationSheetsRepository.delete(getDestinationSheetsById(destinationSheetsId));
    }
}
