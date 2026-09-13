package com.pleasebookme.server.core.booking.service;

import com.pleasebookme.server.core.booking.dto.BookingRequest;
import com.pleasebookme.server.core.booking.dto.BookingFilter;
import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.booking.specification.BookingTab;
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

    BookingEntity updateBooking(
        BigInteger bookingId,
        BookingRequest request
    );

    void deleteBooking(BigInteger bookingId);
}
