package com.pleasebookme.server.core.schedule.service;

import com.pleasebookme.server.core.schedule.dto.ScheduleRequest;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;

import java.math.BigInteger;
import java.util.List;

public interface ScheduleService {
    ScheduleEntity createSchedule(ScheduleRequest request);

    ScheduleEntity getScheduleById(BigInteger scheduleId);

    List<ScheduleEntity> getAllSchedules();

    ScheduleEntity updateSchedule(
        BigInteger scheduleId,
        ScheduleRequest request
    );

    void deleteSchedule(BigInteger scheduleId);
}
