package com.pleasebookme.server.service.dashboard.service.impl;

import com.pleasebookme.server.core.attendee.entity.AttendeeEntity;
import com.pleasebookme.server.core.attendee.repository.AttendeeRepository;
import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.booking.repository.BookingRepository;
import com.pleasebookme.server.core.booking.specification.BookingSpecifications;
import com.pleasebookme.server.core.bookingresource.entity.BookingResourceEntity;
import com.pleasebookme.server.core.bookingresource.repository.BookingResourceRepository;
import com.pleasebookme.server.core.enums.BookingStatus;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.service.dashboard.dto.DashboardBookingRowResponse;
import com.pleasebookme.server.service.dashboard.dto.DashboardMetricsResponse;
import com.pleasebookme.server.service.dashboard.dto.DashboardSummaryResponse;
import com.pleasebookme.server.service.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private static final int RECENT_BOOKING_LIMIT = 10;
    private static final List<BookingStatus> LIVE_STATUSES = List.of(
        BookingStatus.PENDING,
        BookingStatus.ACCEPTED,
        BookingStatus.AWAITING_HOST
    );
    private static final List<BookingStatus> PENDING_STATUSES = List.of(
        BookingStatus.PENDING,
        BookingStatus.AWAITING_HOST
    );
    private static final List<BookingStatus> CANCELLATION_STATUSES = List.of(
        BookingStatus.CANCELLED,
        BookingStatus.REJECTED
    );

    private final BookingRepository bookingRepository;
    private final AttendeeRepository attendeeRepository;
    private final BookingResourceRepository bookingResourceRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(BigInteger organizationId) {
        ZoneId zone = organizationRepository.findById(organizationId)
            .map(organization -> resolveZone(organization.getTimezone()))
            .orElse(ZoneOffset.UTC);
        LocalDate today = LocalDate.now(zone);
        Instant dayStart = today.atStartOfDay(zone).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant();

        Specification<BookingEntity> organizationScope =
            BookingSpecifications.hasOrganization(organizationId);
        Specification<BookingEntity> dayScope =
            BookingSpecifications.startsBetween(dayStart, dayEnd);

        long todaysBookings = bookingRepository.count(Specification.allOf(
            organizationScope,
            dayScope,
            BookingSpecifications.hasStatusIn(LIVE_STATUSES)
        ));

        // Pending is a work queue across all dates, so overdue requests remain visible.
        long pending = bookingRepository.count(Specification.allOf(
            organizationScope,
            BookingSpecifications.hasStatusIn(PENDING_STATUSES)
        ));

        // There is no cancelled_at column, so this measures negative outcomes among
        // bookings scheduled to start on the organization's current local date.
        long cancellations = bookingRepository.count(Specification.allOf(
            organizationScope,
            dayScope,
            BookingSpecifications.hasStatusIn(CANCELLATION_STATUSES)
        ));

        DashboardMetricsResponse metrics = new DashboardMetricsResponse(
            todaysBookings,
            pending,
            cancellations
        );

        // The dashboard contract deliberately keeps the furthest-future start first.
        Pageable recentPage = PageRequest.of(
            0,
            RECENT_BOOKING_LIMIT,
            Sort.by(Sort.Direction.DESC, "startTime")
        );
        List<BookingEntity> recent = bookingRepository
            .findAll(organizationScope, recentPage)
            .getContent();

        if (recent.isEmpty()) {
            return new DashboardSummaryResponse(
                organizationId,
                zone.getId(),
                today,
                metrics,
                List.of()
            );
        }

        List<BigInteger> bookingIds = recent.stream()
            .map(BookingEntity::getBookingId)
            .toList();
        List<AttendeeEntity> attendees =
            attendeeRepository.findByBookingBookingIdIn(bookingIds);
        List<BookingResourceEntity> primaries =
            bookingResourceRepository.findByBookingBookingIdInAndIsPrimaryTrue(bookingIds);

        Map<BigInteger, AttendeeEntity> attendeeByBooking = attendees.stream()
            .collect(Collectors.toMap(
                attendee -> attendee.getBooking().getBookingId(),
                Function.identity(),
                (first, second) -> Comparator
                    .comparing(AttendeeEntity::getAttendeeId)
                    .compare(first, second) <= 0 ? first : second
            ));
        Map<BigInteger, BookingResourceEntity> primaryByBooking = primaries.stream()
            .collect(Collectors.toMap(
                primary -> primary.getBooking().getBookingId(),
                Function.identity()
            ));

        // Service and resource associations may each lazy-load once per distinct row;
        // the fixed ten-row cap keeps that accepted cost bounded inside this transaction.
        List<DashboardBookingRowResponse> recentBookings = recent.stream()
            .map(booking -> toRow(
                booking,
                attendeeByBooking.get(booking.getBookingId()),
                primaryByBooking.get(booking.getBookingId())
            ))
            .toList();

        return new DashboardSummaryResponse(
            organizationId,
            zone.getId(),
            today,
            metrics,
            recentBookings
        );
    }

    private ZoneId resolveZone(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            // Timezones are stored as unvalidated text; bad legacy data must not make
            // this read-only overview unavailable.
            return ZoneOffset.UTC;
        }
    }

    private DashboardBookingRowResponse toRow(
        BookingEntity booking,
        AttendeeEntity attendee,
        BookingResourceEntity primary
    ) {
        return new DashboardBookingRowResponse(
            booking.getBookingId(),
            booking.getBookingUid(),
            booking.getTitle(),
            booking.getStartTime(),
            booking.getEndTime(),
            booking.getStatus(),
            booking.getService().getServiceId(),
            booking.getService().getTitle(),
            attendee != null ? attendee.getName() : null,
            primary != null ? primary.getResource().getName() : null
        );
    }
}
