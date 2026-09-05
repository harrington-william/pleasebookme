package com.pleasebookme.server.core.attendee.service.impl;

import com.pleasebookme.server.core.attendee.dto.AttendeeRequest;
import com.pleasebookme.server.core.attendee.entity.AttendeeEntity;
import com.pleasebookme.server.core.attendee.exception.AttendeeNotFoundException;
import com.pleasebookme.server.core.attendee.repository.AttendeeRepository;
import com.pleasebookme.server.core.attendee.service.AttendeeService;
import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.booking.exception.BookingNotFoundException;
import com.pleasebookme.server.core.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendeeServiceImpl implements AttendeeService {
    private final AttendeeRepository attendeeRepository;
    private final BookingRepository bookingRepository;

    @Override
    public AttendeeEntity createAttendee(AttendeeRequest request) {
        BookingEntity booking = bookingRepository.findById(request.bookingId())
            .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + request.bookingId()));

        AttendeeEntity.AttendeeEntityBuilder attendee = AttendeeEntity.builder()
            .booking(booking)
            .email(request.email())
            .phone(request.phone())
            .name(request.name())
            .locale(request.locale())
            .timezone(request.timezone());

        if (request.noShow() != null) attendee.noShow(request.noShow());

        return attendeeRepository.save(attendee.build());
    }

    @Override
    public AttendeeEntity getAttendeeById(BigInteger attendeeId) {
        return attendeeRepository.findById(attendeeId)
            .orElseThrow(() -> new AttendeeNotFoundException(
                "Attendee not found: " + attendeeId
            ));
    }

    @Override
    public List<AttendeeEntity> getAttendeesByBookingId(BigInteger bookingId) {
        return attendeeRepository.findByBookingBookingId(bookingId);
    }

    @Override
    public List<AttendeeEntity> getAttendeesByOrganizationId(BigInteger organizationId) {
        return attendeeRepository.findByBookingServiceOrganizationOrganizationId(organizationId);
    }

    @Override
    public AttendeeEntity updateAttendee(
        BigInteger attendeeId,
        AttendeeRequest request
    ) {
        AttendeeEntity attendee = getAttendeeById(attendeeId);

        BookingEntity booking = bookingRepository.findById(request.bookingId())
            .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + request.bookingId()));

        attendee.setBooking(booking);
        attendee.setEmail(request.email());
        attendee.setPhone(request.phone());
        attendee.setName(request.name());
        attendee.setLocale(request.locale());
        attendee.setTimezone(request.timezone());

        if (request.noShow() != null) attendee.setNoShow(request.noShow());

        return attendeeRepository.save(attendee);
    }

    @Override
    public void deleteAttendee(BigInteger attendeeId) {
        attendeeRepository.delete(getAttendeeById(attendeeId));
    }
}
