package com.pleasebookme.server.core.schedule.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.schedule.dto.ScheduleRequest;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.exception.ScheduleNotFoundException;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.core.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Override
    public ScheduleEntity createSchedule(ScheduleRequest request) {
        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        ScheduleEntity.ScheduleEntityBuilder schedule = ScheduleEntity.builder()
            .user(user)
            .title(request.title());

        if (request.timezone() != null) schedule.timezone(request.timezone());

        return scheduleRepository.save(schedule.build());
    }

    @Override
    public ScheduleEntity getScheduleById(BigInteger scheduleId) {
        return scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ScheduleNotFoundException(
                "Schedule not found: " + scheduleId
            ));
    }

    @Override
    public List<ScheduleEntity> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    public ScheduleEntity updateSchedule(
        BigInteger scheduleId,
        ScheduleRequest request
    ) {
        ScheduleEntity schedule = getScheduleById(scheduleId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        schedule.setUser(user);
        schedule.setTitle(request.title());

        if (request.timezone() != null) schedule.setTimezone(request.timezone());

        return scheduleRepository.save(schedule);
    }

    @Override
    public void deleteSchedule(BigInteger scheduleId) {
        scheduleRepository.delete(getScheduleById(scheduleId));
    }
}
