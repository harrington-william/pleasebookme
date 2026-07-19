package com.pleasebookme.server.core.attendee.service;

import com.pleasebookme.server.core.attendee.dto.AttendeeRequest;
import com.pleasebookme.server.core.attendee.entity.AttendeeEntity;

import java.math.BigInteger;
import java.util.List;

public interface AttendeeService {
    AttendeeEntity createAttendee(AttendeeRequest request);

    AttendeeEntity getAttendeeById(BigInteger attendeeId);

    List<AttendeeEntity> getAllAttendees();

    AttendeeEntity updateAttendee(
        BigInteger attendeeId,
        AttendeeRequest request
    );

    void deleteAttendee(BigInteger attendeeId);
}
