package com.pleasebookme.server.service.availabilityruleset.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.availability.dto.AvailabilityResponse;
import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import com.pleasebookme.server.core.availability.repository.AvailabilityRepository;
import com.pleasebookme.server.core.schedule.dto.ScheduleResponse;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
import com.pleasebookme.server.service.availabilityruleset.service.AvailabilityRulesetService;
import com.pleasebookme.server.service.availabilityruleset.dto.AvailabilityRulesetRequest;
import com.pleasebookme.server.service.availabilityruleset.dto.AvailabilityRulesetResponse;
import com.pleasebookme.server.service.availabilityruleset.dto.AvailabilityWindowRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityRulesetServiceImpl implements AvailabilityRulesetService {
    private final ScheduleRepository scheduleRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    @Override
    @Transactional
    public AvailabilityRulesetResponse createAvailabilityRuleset(
        AvailabilityRulesetRequest request
    ) {
        UserEntity user = resolveCurrentUser();

        ScheduleEntity.ScheduleEntityBuilder scheduleBuilder = ScheduleEntity.builder()
            .user(user)
            .title(request.title());

        if (request.timezone() != null) scheduleBuilder.timezone(request.timezone());

        ScheduleEntity schedule = scheduleRepository.save(scheduleBuilder.build());

        List<AvailabilityEntity> availabilities =
            request.windows()
                .stream()
                .map(
                    window ->buildAvailability(user, schedule, window)
                )
                .toList();

        List<AvailabilityEntity> savedAvailabilities = availabilityRepository.saveAll(availabilities);

        return new AvailabilityRulesetResponse(
            ScheduleResponse.from(schedule),
            savedAvailabilities.stream()
                .map(AvailabilityResponse::from)
                .toList()
        );
    }

    private UserEntity resolveCurrentUser() {
        BigInteger userId = currentPrincipalProvider.requireUser().userId();

        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    private AvailabilityEntity buildAvailability(
        UserEntity user,
        ScheduleEntity schedule,
        AvailabilityWindowRequest window
    ) {
        return AvailabilityEntity.builder()
            .user(user)
            .schedule(schedule)
            .days(window.days().toArray(Integer[]::new))
            .startTime(window.startTime())
            .endTime(window.endTime())
            .build();
    }
}
