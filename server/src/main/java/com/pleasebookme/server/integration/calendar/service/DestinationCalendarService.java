package com.pleasebookme.server.integration.calendar.service;

import com.pleasebookme.server.integration.calendar.dto.DestinationCalendarRequest;
import com.pleasebookme.server.integration.calendar.entity.DestinationCalendarEntity;

import java.math.BigInteger;
import java.util.List;

public interface DestinationCalendarService {
    DestinationCalendarEntity createDestinationCalendar(DestinationCalendarRequest request);

    DestinationCalendarEntity getDestinationCalendarById(BigInteger destinationCalendarId);

    List<DestinationCalendarEntity> getAllDestinationCalendars();

    DestinationCalendarEntity updateDestinationCalendar(
        BigInteger destinationCalendarId,
        DestinationCalendarRequest request
    );

    void deleteDestinationCalendar(BigInteger destinationCalendarId);
}
