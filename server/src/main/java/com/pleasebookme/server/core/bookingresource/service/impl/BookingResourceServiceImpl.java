package com.pleasebookme.server.core.bookingresource.service.impl;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.booking.exception.BookingNotFoundException;
import com.pleasebookme.server.core.booking.repository.BookingRepository;
import com.pleasebookme.server.core.bookingresource.dto.BookingResourceRequest;
import com.pleasebookme.server.core.bookingresource.entity.BookingResourceEntity;
import com.pleasebookme.server.core.bookingresource.exception.BookingResourceNotFoundException;
import com.pleasebookme.server.core.bookingresource.exception.DuplicateBookingResourceException;
import com.pleasebookme.server.core.bookingresource.id.BookingResourceId;
import com.pleasebookme.server.core.bookingresource.repository.BookingResourceRepository;
import com.pleasebookme.server.core.bookingresource.service.BookingResourceService;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingResourceServiceImpl implements BookingResourceService {
    private final BookingResourceRepository bookingResourceRepository;
    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;

    @Override
    public BookingResourceEntity createBookingResource(BookingResourceRequest request) {
        BookingResourceId bookingResourceId = new BookingResourceId(
            request.bookingId(),
            request.resourceId()
        );

        // A non-null ID makes repository.save use merge, must reject duplicates first
        if (bookingResourceRepository.existsById(bookingResourceId)) {
            throw new DuplicateBookingResourceException(
                "Resource " + request.resourceId() + " is already assigned to booking " + request.bookingId()
            );
        }

        requirePrimarySlotAvailable(request.bookingId(), request.isPrimary());

        BookingEntity booking = bookingRepository.findById(request.bookingId())
            .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + request.bookingId()));

        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        BookingResourceEntity.BookingResourceEntityBuilder bookingResource = BookingResourceEntity.builder()
            .booking(booking)
            .resource(resource);

        if (request.isPrimary() != null) bookingResource.isPrimary(request.isPrimary());

        return bookingResourceRepository.save(bookingResource.build());
    }

    @Override
    public BookingResourceEntity getBookingResourceById(
        BigInteger bookingId,
        BigInteger resourceId
    ) {
        return bookingResourceRepository.findById(new BookingResourceId(bookingId, resourceId))
            .orElseThrow(() -> new BookingResourceNotFoundException(
                "Resource " + resourceId + " is not assigned to booking " + bookingId
            ));
    }

    @Override
    public List<BookingResourceEntity> getBookingResourcesByBookingId(BigInteger bookingId) {
        return bookingResourceRepository.findByBookingBookingId(bookingId);
    }

    @Override
    public List<BookingResourceEntity> getBookingResourcesByResourceId(BigInteger resourceId) {
        return bookingResourceRepository.findByResourceResourceId(resourceId);
    }

    // Lets the bookings list fetch every assignment in one call instead of one per row.
    @Override
    public List<BookingResourceEntity> getBookingResourcesByOrganizationId(BigInteger organizationId) {
        return bookingResourceRepository.findByBookingServiceOrganizationOrganizationId(organizationId);
    }

    @Override
    public void deleteBookingResource(
        BigInteger bookingId,
        BigInteger resourceId
    ) {
        bookingResourceRepository.delete(getBookingResourceById(bookingId, resourceId));
    }

    /**
     * Backs uq_booking_resources_primary, the partial unique index allowing at most
     * one primary resource per booking.
     */
    private void requirePrimarySlotAvailable(
        BigInteger bookingId,
        Boolean requestedPrimary
    ) {
        if (!Boolean.TRUE.equals(requestedPrimary)) {
            return;
        }

        bookingResourceRepository.findByBookingBookingIdAndIsPrimaryTrue(bookingId)
            .ifPresent(existing -> {
                throw new DuplicateBookingResourceException(
                    "Booking " + bookingId + " already has a primary resource: "
                        + existing.getBookingResourceId().getResourceId()
                );
            });
    }
}
