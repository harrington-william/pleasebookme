package com.pleasebookme.server.integration.calendar.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.integration.calendar.dto.DestinationCalendarRequest;
import com.pleasebookme.server.integration.calendar.entity.DestinationCalendarEntity;
import com.pleasebookme.server.integration.calendar.exception.DestinationCalendarNotFoundException;
import com.pleasebookme.server.integration.calendar.repository.DestinationCalendarRepository;
import com.pleasebookme.server.integration.calendar.service.DestinationCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinationCalendarServiceImpl implements DestinationCalendarService {
    private final DestinationCalendarRepository destinationCalendarRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    @Override
    public DestinationCalendarEntity createDestinationCalendar(DestinationCalendarRequest request) {
        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        DestinationCalendarEntity destinationCalendar = DestinationCalendarEntity.builder()
            .integrationType(request.integrationType())
            .externalId(request.externalId())
            .user(user)
            .service(service)
            .build();

        return destinationCalendarRepository.save(destinationCalendar);
    }

    @Override
    public DestinationCalendarEntity getDestinationCalendarById(BigInteger destinationCalendarId) {
        return destinationCalendarRepository.findById(destinationCalendarId)
            .orElseThrow(() -> new DestinationCalendarNotFoundException(
                "Destination calendar not found: " + destinationCalendarId
            ));
    }

    @Override
    public List<DestinationCalendarEntity> getAllDestinationCalendars() {
        return destinationCalendarRepository.findAll();
    }

    @Override
    public DestinationCalendarEntity updateDestinationCalendar(
        BigInteger destinationCalendarId,
        DestinationCalendarRequest request
    ) {
        DestinationCalendarEntity destinationCalendar = getDestinationCalendarById(destinationCalendarId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        destinationCalendar.setIntegrationType(request.integrationType());
        destinationCalendar.setExternalId(request.externalId());
        destinationCalendar.setUser(user);
        destinationCalendar.setService(service);

        return destinationCalendarRepository.save(destinationCalendar);
    }

    @Override
    public void deleteDestinationCalendar(BigInteger destinationCalendarId) {
        destinationCalendarRepository.delete(getDestinationCalendarById(destinationCalendarId));
    }
}
