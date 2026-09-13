package com.pleasebookme.server.core.bookingpolicy.service.impl;

import com.pleasebookme.server.core.bookingpolicy.dto.BookingPolicyRequest;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.exception.BookingPolicyNotFoundException;
import com.pleasebookme.server.core.bookingpolicy.exception.DuplicateBookingPolicyException;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.bookingpolicy.service.BookingPolicyService;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingPolicyServiceImpl implements BookingPolicyService {
    private final BookingPolicyRepository bookingPolicyRepository;
    private final ServiceRepository serviceRepository;

    @Override
    public BookingPolicyEntity createBookingPolicy(BookingPolicyRequest request) {
        if (bookingPolicyRepository.existsByServiceServiceId(request.serviceId())) {
            throw new DuplicateBookingPolicyException("Booking policy already exists for service: " + request.serviceId());
        }

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        BookingPolicyEntity.BookingPolicyEntityBuilder bookingPolicy = BookingPolicyEntity.builder()
            .service(service)
            .minimumDuration(request.minimumDuration())
            .maximumDuration(request.maximumDuration())
            .minimumNotice(request.minimumNotice())
            .maximumAdvanceBooking(request.maximumAdvanceBooking())
            .bookingWindowType(request.bookingWindowType())
            .capacity(request.capacity());

        if (request.bookingMode() != null) bookingPolicy.bookingMode(request.bookingMode());
        if (request.defaultDuration() != null) bookingPolicy.defaultDuration(request.defaultDuration());
        if (request.slotInterval() != null) bookingPolicy.slotInterval(request.slotInterval());
        if (request.beforeBuffer() != null) bookingPolicy.beforeBuffer(request.beforeBuffer());
        if (request.afterBuffer() != null) bookingPolicy.afterBuffer(request.afterBuffer());
        if (request.allowOverlap() != null) bookingPolicy.allowOverlap(request.allowOverlap());
        if (request.allowMultipleAttendee() != null) bookingPolicy.allowMultipleAttendee(request.allowMultipleAttendee());
        if (request.requiresPayment() != null) bookingPolicy.requiresPayment(request.requiresPayment());
        if (request.autoConfirm() != null) bookingPolicy.autoConfirm(request.autoConfirm());

        return bookingPolicyRepository.save(bookingPolicy.build());
    }

    @Override
    public BookingPolicyEntity getBookingPolicyById(BigInteger bookingPolicyId) {
        return bookingPolicyRepository.findById(bookingPolicyId)
            .orElseThrow(() -> new BookingPolicyNotFoundException(
                "Booking policy not found: " + bookingPolicyId
            ));
    }

    @Override
    public Optional<BookingPolicyEntity> getBookingPolicyByServiceId(BigInteger serviceId) {
        return bookingPolicyRepository.findByServiceServiceId(serviceId);
    }

    @Override
    public List<BookingPolicyEntity> getAllBookingPolicies() {
        return bookingPolicyRepository.findAll();
    }

    @Override
    public BookingPolicyEntity updateBookingPolicy(
        BigInteger bookingPolicyId,
        BookingPolicyRequest request
    ) {
        BookingPolicyEntity bookingPolicy = getBookingPolicyById(bookingPolicyId);

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        bookingPolicy.setService(service);
        bookingPolicy.setMinimumDuration(request.minimumDuration());
        bookingPolicy.setMaximumDuration(request.maximumDuration());
        bookingPolicy.setMinimumNotice(request.minimumNotice());
        bookingPolicy.setMaximumAdvanceBooking(request.maximumAdvanceBooking());
        bookingPolicy.setBookingWindowType(request.bookingWindowType());
        bookingPolicy.setCapacity(request.capacity());

        if (request.bookingMode() != null) bookingPolicy.setBookingMode(request.bookingMode());
        if (request.defaultDuration() != null) bookingPolicy.setDefaultDuration(request.defaultDuration());
        if (request.slotInterval() != null) bookingPolicy.setSlotInterval(request.slotInterval());
        if (request.beforeBuffer() != null) bookingPolicy.setBeforeBuffer(request.beforeBuffer());
        if (request.afterBuffer() != null) bookingPolicy.setAfterBuffer(request.afterBuffer());
        if (request.allowOverlap() != null) bookingPolicy.setAllowOverlap(request.allowOverlap());
        if (request.allowMultipleAttendee() != null) bookingPolicy.setAllowMultipleAttendee(request.allowMultipleAttendee());
        if (request.requiresPayment() != null) bookingPolicy.setRequiresPayment(request.requiresPayment());
        if (request.autoConfirm() != null) bookingPolicy.setAutoConfirm(request.autoConfirm());

        return bookingPolicyRepository.save(bookingPolicy);
    }

    @Override
    public void deleteBookingPolicy(BigInteger bookingPolicyId) {
        bookingPolicyRepository.delete(getBookingPolicyById(bookingPolicyId));
    }
}
