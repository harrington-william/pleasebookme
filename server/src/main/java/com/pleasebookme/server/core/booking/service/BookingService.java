package com.pleasebookme.server.core.booking.service;

import com.pleasebookme.server.core.booking.dto.BookingRequest;
import com.pleasebookme.server.core.booking.entity.BookingEntity;

import java.math.BigInteger;
import java.util.List;

public interface BookingService {
    BookingEntity createBooking(BookingRequest request);

    BookingEntity getBookingById(BigInteger bookingId);

    List<BookingEntity> getAllBookings();

    BookingEntity updateBooking(
        BigInteger bookingId,
        BookingRequest request
    );

    void deleteBooking(BigInteger bookingId);
}
