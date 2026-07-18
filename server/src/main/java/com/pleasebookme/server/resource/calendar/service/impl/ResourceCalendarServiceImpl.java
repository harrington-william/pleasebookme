package com.pleasebookme.server.resource.calendar.service.impl;

import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.exception.ScheduleNotFoundException;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.resource.calendar.dto.ResourceCalendarRequest;
import com.pleasebookme.server.resource.calendar.entity.ResourceCalendarEntity;
import com.pleasebookme.server.resource.calendar.exception.ResourceCalendarNotFoundException;
import com.pleasebookme.server.resource.calendar.repository.ResourceCalendarRepository;
import com.pleasebookme.server.resource.calendar.service.ResourceCalendarService;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceCalendarServiceImpl implements ResourceCalendarService {
    private final ResourceCalendarRepository resourceCalendarRepository;
    private final ResourceRepository resourceRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    public ResourceCalendarEntity createResourceCalendar(ResourceCalendarRequest request) {
        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        ScheduleEntity schedule = scheduleRepository.findById(request.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + request.scheduleId()));

        ResourceCalendarEntity resourceCalendar = ResourceCalendarEntity.builder()
            .resource(resource)
            .schedule(schedule)
            .build();

        return resourceCalendarRepository.save(resourceCalendar);
    }

    @Override
    public ResourceCalendarEntity getResourceCalendarById(BigInteger resourceCalendarId) {
        return resourceCalendarRepository.findById(resourceCalendarId)
            .orElseThrow(() -> new ResourceCalendarNotFoundException("Resource calendar not found: " + resourceCalendarId));
    }

    @Override
    public List<ResourceCalendarEntity> getAllResourceCalendars() {
        return resourceCalendarRepository.findAll();
    }

    @Override
    public ResourceCalendarEntity updateResourceCalendar(
        BigInteger resourceCalendarId,
        ResourceCalendarRequest request
    ) {
        ResourceCalendarEntity resourceCalendar = getResourceCalendarById(resourceCalendarId);

        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        ScheduleEntity schedule = scheduleRepository.findById(request.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + request.scheduleId()));

        resourceCalendar.setResource(resource);
        resourceCalendar.setSchedule(schedule);

        return resourceCalendarRepository.save(resourceCalendar);
    }

    @Override
    public void deleteResourceCalendar(BigInteger resourceCalendarId) {
        resourceCalendarRepository.delete(getResourceCalendarById(resourceCalendarId));
    }
}
