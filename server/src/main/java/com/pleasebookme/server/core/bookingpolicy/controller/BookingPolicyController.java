package com.pleasebookme.server.core.bookingpolicy.controller;

import com.pleasebookme.server.core.bookingpolicy.dto.BookingPolicyRequest;
import com.pleasebookme.server.core.bookingpolicy.dto.BookingPolicyResponse;
import com.pleasebookme.server.core.bookingpolicy.service.BookingPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/booking-policies")
@RequiredArgsConstructor
public class BookingPolicyController {
    private final BookingPolicyService bookingPolicyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingPolicyResponse createBookingPolicy(@Valid @RequestBody BookingPolicyRequest request) {
        return BookingPolicyResponse.from(bookingPolicyService.createBookingPolicy(request));
    }

    @GetMapping("/{bookingPolicyId}")
    public BookingPolicyResponse getBookingPolicy(@PathVariable BigInteger bookingPolicyId) {
        return BookingPolicyResponse.from(bookingPolicyService.getBookingPolicyById(bookingPolicyId));
    }

    @GetMapping
    public List<BookingPolicyResponse> getBookingPolicies() {
        return bookingPolicyService.getAllBookingPolicies().stream()
            .map(BookingPolicyResponse::from)
            .toList();
    }

    @PutMapping("/{bookingPolicyId}")
    public BookingPolicyResponse updateBookingPolicy(
        @PathVariable BigInteger bookingPolicyId,
        @Valid @RequestBody BookingPolicyRequest request
    ) {
        return BookingPolicyResponse.from(bookingPolicyService.updateBookingPolicy(bookingPolicyId, request));
    }

    @DeleteMapping("/{bookingPolicyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBookingPolicy(@PathVariable BigInteger bookingPolicyId) {
        bookingPolicyService.deleteBookingPolicy(bookingPolicyId);
    }
}
