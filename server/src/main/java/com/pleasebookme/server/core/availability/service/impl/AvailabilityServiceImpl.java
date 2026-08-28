package com.pleasebookme.server.core.availability.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.availability.dto.AvailabilityRequest;
import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import com.pleasebookme.server.core.availability.exception.AvailabilityNotFoundException;
import com.pleasebookme.server.core.availability.repository.AvailabilityRepository;
import com.pleasebookme.server.core.availability.service.AvailabilityService;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.exception.ScheduleNotFoundException;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    @Override
    public AvailabilityEntity createAvailability(AvailabilityRequest request) {
        UserEntity user = resolveCurrentUser();

        ScheduleEntity schedule = scheduleRepository.findById(request.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + request.scheduleId()));

        AvailabilityEntity availability = AvailabilityEntity.builder()
            .user(user)
            .schedule(schedule)
            .days(request.days())
            .startTime(request.startTime())
            .endTime(request.endTime())
            .build();

        return availabilityRepository.save(availability);
    }

    @Override
    public AvailabilityEntity getAvailabilityById(BigInteger availabilityId) {
        return availabilityRepository.findById(availabilityId)
            .orElseThrow(() -> new AvailabilityNotFoundException(
                "Availability not found: " + availabilityId
            ));
    }

    @Override
    public List<AvailabilityEntity> getAllAvailabilities() {
        return availabilityRepository.findAll();
    }

    @Override
    public AvailabilityEntity updateAvailability(
        BigInteger availabilityId,
        AvailabilityRequest request
    ) {
        AvailabilityEntity availability = getAvailabilityById(availabilityId);

        // A PUT can move a window to another schedule, but it must not reassign row ownership.
        resolveCurrentUser();

        ScheduleEntity schedule = scheduleRepository.findById(request.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + request.scheduleId()));

        availability.setSchedule(schedule);
        availability.setDays(request.days());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());

        return availabilityRepository.save(availability);
    }

    @Override
    public void deleteAvailability(BigInteger availabilityId) {
        availabilityRepository.delete(getAvailabilityById(availabilityId));
    }

    private UserEntity resolveCurrentUser() {
        BigInteger userId = currentPrincipalProvider.requireUser().userId();

        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}
