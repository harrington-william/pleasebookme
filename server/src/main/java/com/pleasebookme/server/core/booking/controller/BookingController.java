package com.pleasebookme.server.core.booking.controller;

import com.pleasebookme.server.core.booking.dto.BookingRequest;
import com.pleasebookme.server.core.booking.dto.BookingResponse;
import com.pleasebookme.server.core.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        return BookingResponse.from(bookingService.createBooking(request));
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable BigInteger bookingId) {
        return BookingResponse.from(bookingService.getBookingById(bookingId));
    }

    @GetMapping
    public List<BookingResponse> getBookings() {
        return bookingService.getAllBookings().stream()
            .map(BookingResponse::from)
            .toList();
    }

    @PutMapping("/{bookingId}")
    public BookingResponse updateBooking(
        @PathVariable BigInteger bookingId,
        @Valid @RequestBody BookingRequest request
    ) {
        return BookingResponse.from(bookingService.updateBooking(bookingId, request));
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable BigInteger bookingId) {
        bookingService.deleteBooking(bookingId);
    }
}
