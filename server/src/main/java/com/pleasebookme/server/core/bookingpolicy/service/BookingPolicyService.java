package com.pleasebookme.server.core.bookingpolicy.service;

import com.pleasebookme.server.core.bookingpolicy.dto.BookingPolicyRequest;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;

import java.math.BigInteger;
import java.util.List;

public interface BookingPolicyService {
    BookingPolicyEntity createBookingPolicy(BookingPolicyRequest request);

    BookingPolicyEntity getBookingPolicyById(BigInteger bookingPolicyId);

    List<BookingPolicyEntity> getAllBookingPolicies();

    BookingPolicyEntity updateBookingPolicy(
        BigInteger bookingPolicyId,
        BookingPolicyRequest request
    );

    void deleteBookingPolicy(BigInteger bookingPolicyId);
}
