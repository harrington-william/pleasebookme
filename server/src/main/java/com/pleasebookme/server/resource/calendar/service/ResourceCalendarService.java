package com.pleasebookme.server.resource.calendar.service;

import com.pleasebookme.server.resource.calendar.dto.ResourceCalendarRequest;
import com.pleasebookme.server.resource.calendar.entity.ResourceCalendarEntity;

import java.math.BigInteger;
import java.util.List;

public interface ResourceCalendarService {
    ResourceCalendarEntity createResourceCalendar(ResourceCalendarRequest request);

    ResourceCalendarEntity getResourceCalendarById(BigInteger resourceCalendarId);

    List<ResourceCalendarEntity> getAllResourceCalendars();

    ResourceCalendarEntity updateResourceCalendar(
        BigInteger resourceCalendarId,
        ResourceCalendarRequest request
    );

    void deleteResourceCalendar(BigInteger resourceCalendarId);
}
