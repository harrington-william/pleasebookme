package com.pleasebookme.server.core.booking.controller;

import com.pleasebookme.server.core.booking.dto.BookingFilter;
import com.pleasebookme.server.core.booking.dto.BookingPageResponse;
import com.pleasebookme.server.core.booking.dto.BookingRequest;
import com.pleasebookme.server.core.booking.dto.BookingResponse;
import com.pleasebookme.server.core.booking.service.BookingService;
import com.pleasebookme.server.core.booking.specification.BookingTab;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

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
    public BookingPageResponse getBookings(
        @RequestParam BigInteger organizationId,
        @RequestParam(required = false) String tab,
        @RequestParam(required = false) BigInteger serviceId,
        @RequestParam(required = false) BigInteger resourceId,
        @RequestParam(required = false) String q,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        BookingFilter filter = new BookingFilter(serviceId, resourceId, q);

        return BookingPageResponse.from(
            bookingService.getBookingsByOrganizationId(
                organizationId,
                parseTab(tab),
                filter,
                pageable
            )
        );
    }

    @GetMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(@PathVariable BigInteger bookingId) {
        return BookingResponse.from(bookingService.cancelBooking(bookingId));
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

    private BookingTab parseTab(String tab) {
        if (tab == null || tab.isBlank()) {
            return BookingTab.UPCOMING;
        }

        try {
            return BookingTab.valueOf(tab.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return BookingTab.UPCOMING;
        }
    }
}
