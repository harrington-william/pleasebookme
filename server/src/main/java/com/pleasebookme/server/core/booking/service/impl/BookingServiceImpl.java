package com.pleasebookme.server.core.booking.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.booking.dto.BookingFilter;
import com.pleasebookme.server.core.booking.dto.BookingRequest;
import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.booking.exception.BookingNotFoundException;
import com.pleasebookme.server.core.booking.repository.BookingRepository;
import com.pleasebookme.server.core.booking.service.BookingService;
import com.pleasebookme.server.core.booking.specification.BookingSort;
import com.pleasebookme.server.core.booking.specification.BookingSpecifications;
import com.pleasebookme.server.core.booking.specification.BookingTab;
import com.pleasebookme.server.core.enums.BookingStatus;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.integration.calendar.entity.DestinationCalendarEntity;
import com.pleasebookme.server.integration.calendar.exception.DestinationCalendarNotFoundException;
import com.pleasebookme.server.integration.calendar.repository.DestinationCalendarRepository;
import com.pleasebookme.server.integration.sheets.entity.DestinationSheetsEntity;
import com.pleasebookme.server.integration.sheets.exception.DestinationSheetsNotFoundException;
import com.pleasebookme.server.integration.sheets.repository.DestinationSheetsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final DestinationCalendarRepository destinationCalendarRepository;
    private final DestinationSheetsRepository destinationSheetsRepository;

    @Override
    public BookingEntity createBooking(BookingRequest request) {
        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        BookingEntity.BookingEntityBuilder booking = BookingEntity.builder()
            .idempotencyKey(request.idempotencyKey())
            .user(user)
            .title(request.title())
            .description(request.description())
            .startTime(request.startTime())
            .endTime(request.endTime())
            .service(service)
            .location(request.location())
            .cancellationReason(request.cancellationReason())
            .rejectionReason(request.rejectionReason())
            .deletedAt(request.deletedAt());

        if (request.status() != null) booking.status(request.status());
        if (request.paid() != null) booking.paid(request.paid());
        if (request.rescheduled() != null) booking.rescheduled(request.rescheduled());
        if (request.noShowHost() != null) booking.noShowHost(request.noShowHost());

        booking.cancelledBy(resolveUser(request.cancelledById()));
        booking.rescheduledBy(resolveUser(request.rescheduledById()));
        booking.deletedBy(resolveUser(request.deletedById()));

        if (request.destinationCalendarId() != null) {
            DestinationCalendarEntity destinationCalendar = destinationCalendarRepository.findById(request.destinationCalendarId())
                .orElseThrow(() -> new DestinationCalendarNotFoundException("Destination calendar not found: " + request.destinationCalendarId()));
            booking.destinationCalendar(destinationCalendar);
        }

        if (request.destinationSheetsId() != null) {
            DestinationSheetsEntity destinationSheets = destinationSheetsRepository.findById(request.destinationSheetsId())
                .orElseThrow(() -> new DestinationSheetsNotFoundException("Destination sheets not found: " + request.destinationSheetsId()));
            booking.destinationSheets(destinationSheets);
        }

        return bookingRepository.save(booking.build());
    }

    @Override
    public BookingEntity getBookingById(BigInteger bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(
                "Booking not found: " + bookingId
            ));
    }

    @Override
    public Page<BookingEntity> getBookingsByOrganizationId(
        BigInteger organizationId,
        BookingTab tab,
        BookingFilter filter,
        Pageable pageable
    ) {
        Pageable safePageable = BookingSort.sanitize(pageable);
        BookingFilter appliedFilter = filter != null ? filter : BookingFilter.none();
        Instant now = Instant.now();

        Specification<BookingEntity> specification = Specification.allOf(
            BookingSpecifications.hasOrganization(organizationId),
            BookingSpecifications.matchesTab(tab, now),
            BookingSpecifications.hasService(appliedFilter.serviceId()),
            BookingSpecifications.hasResource(appliedFilter.resourceId()),
            BookingSpecifications.matchesText(appliedFilter.q())
        );

        return bookingRepository.findAll(specification, safePageable);
    }

    @Override
    public BookingEntity cancelBooking(BigInteger bookingId) {
        BookingEntity booking = getBookingById(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return booking;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Override
    public BookingEntity updateBooking(
        BigInteger bookingId,
        BookingRequest request
    ) {
        BookingEntity booking = getBookingById(bookingId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        booking.setIdempotencyKey(request.idempotencyKey());
        booking.setUser(user);
        booking.setTitle(request.title());
        booking.setDescription(request.description());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setService(service);
        booking.setLocation(request.location());
        booking.setCancellationReason(request.cancellationReason());
        booking.setRejectionReason(request.rejectionReason());
        booking.setDeletedAt(request.deletedAt());

        if (request.status() != null) booking.setStatus(request.status());
        if (request.paid() != null) booking.setPaid(request.paid());
        if (request.rescheduled() != null) booking.setRescheduled(request.rescheduled());
        if (request.noShowHost() != null) booking.setNoShowHost(request.noShowHost());

        booking.setCancelledBy(resolveUser(request.cancelledById()));
        booking.setRescheduledBy(resolveUser(request.rescheduledById()));
        booking.setDeletedBy(resolveUser(request.deletedById()));

        if (request.destinationCalendarId() != null) {
            DestinationCalendarEntity destinationCalendar = destinationCalendarRepository.findById(request.destinationCalendarId())
                .orElseThrow(() -> new DestinationCalendarNotFoundException("Destination calendar not found: " + request.destinationCalendarId()));
            booking.setDestinationCalendar(destinationCalendar);
        } else {
            booking.setDestinationCalendar(null);
        }

        if (request.destinationSheetsId() != null) {
            DestinationSheetsEntity destinationSheets = destinationSheetsRepository.findById(request.destinationSheetsId())
                .orElseThrow(() -> new DestinationSheetsNotFoundException("Destination sheets not found: " + request.destinationSheetsId()));
            booking.setDestinationSheets(destinationSheets);
        } else {
            booking.setDestinationSheets(null);
        }

        return bookingRepository.save(booking);
    }

    @Override
    public void deleteBooking(BigInteger bookingId) {
        bookingRepository.delete(getBookingById(bookingId));
    }

    private UserEntity resolveUser(BigInteger userId) {
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}
