package com.pleasebookme.server.core.booking.domain.service;

import com.pleasebookme.server.core.booking.domain.dto.BookingRequest;
import com.pleasebookme.server.core.booking.domain.dto.BookingFilter;
import com.pleasebookme.server.core.booking.domain.entity.BookingEntity;
import com.pleasebookme.server.core.booking.domain.specification.BookingTab;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;

public interface BookingService {
    BookingEntity createBooking(BookingRequest request);

    BookingEntity getBookingById(BigInteger bookingId);

    Page<BookingEntity> getBookingsByOrganizationId(
        BigInteger organizationId,
        BookingTab tab,
        BookingFilter filter,
        Pageable pageable
    );

    BookingEntity cancelBooking(BigInteger bookingId);

    BookingEntity acceptBooking(BigInteger bookingId);

    BookingEntity rejectBooking(BigInteger bookingId);

    BookingEntity updateBooking(
        BigInteger bookingId,
        BookingRequest request
    );

    void deleteBooking(BigInteger bookingId);
}
