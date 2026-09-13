package com.pleasebookme.server.service.slot.service.impl;

import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import com.pleasebookme.server.core.availability.repository.AvailabilityRepository;
import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.booking.repository.BookingRepository;
import com.pleasebookme.server.core.booking.specification.BookingSpecifications;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.exception.BookingPolicyNotFoundException;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.enums.BookingStatus;
import com.pleasebookme.server.core.outofoffice.entity.OutOfOfficeEntity;
import com.pleasebookme.server.core.outofoffice.repository.OutOfOfficeRepository;
import com.pleasebookme.server.core.selectedslot.entity.SelectedSlotEntity;
import com.pleasebookme.server.core.selectedslot.repository.SelectedSlotRepository;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import com.pleasebookme.server.service.slot.dto.TimeSlot;
import com.pleasebookme.server.service.slot.engine.BookingWindowFilter;
import com.pleasebookme.server.service.slot.engine.SlotConflictValidator;
import com.pleasebookme.server.service.slot.engine.SlotGenerator;
import com.pleasebookme.server.service.slot.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {
    private static final Set<BookingStatus> BLOCKING_STATUSES = EnumSet.of(
        BookingStatus.PENDING,
        BookingStatus.ACCEPTED,
        BookingStatus.AWAITING_HOST
    );

    private final ServiceRepository serviceRepository;
    private final BookingPolicyRepository bookingPolicyRepository;
    private final AvailabilityRepository availabilityRepository;
    private final BookingRepository bookingRepository;
    private final SelectedSlotRepository selectedSlotRepository;
    private final OutOfOfficeRepository outOfOfficeRepository;
    private final SlotGenerator slotGenerator;
    private final BookingWindowFilter bookingWindowFilter;
    private final SlotConflictValidator slotConflictValidator;

    @Override
    @Transactional(readOnly = true)
    public AvailableSlotsResponse getAvailableSlots(
        BigInteger serviceId,
        LocalDate date
    ) {
        ServiceEntity service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + serviceId));

        BookingPolicyEntity policy = bookingPolicyRepository.findByServiceServiceId(serviceId)
            .orElseThrow(() -> new BookingPolicyNotFoundException(
                "Booking policy not found for service: " + serviceId
            ));

        ZoneId zone = ZoneId.of(service.getSchedule().getTimezone());
        BigInteger hostUserId = service.getUser().getUserId();

        int weekday = date.getDayOfWeek().getValue();

        List<AvailabilityEntity> availabilities = availabilityRepository
            .findByScheduleScheduleId(service.getSchedule().getScheduleId())
            .stream()
            .filter(availability -> isAvailableOn(availability, weekday))
            .toList();

        if (availabilities.isEmpty()) {
            return new AvailableSlotsResponse(serviceId, date, zone.getId(), List.of());
        }

        Instant now = Instant.now();
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

        Instant loadFrom = dayStart.minus(policy.getAfterBuffer(), ChronoUnit.MINUTES);
        Instant loadTo = dayEnd.plus(policy.getBeforeBuffer(), ChronoUnit.MINUTES);

        List<BookingEntity> bookings = bookingRepository.findAll(Specification.allOf(
            BookingSpecifications.hasServiceOwnedBy(hostUserId),
            BookingSpecifications.hasStatusIn(BLOCKING_STATUSES),
            BookingSpecifications.overlaps(loadFrom, loadTo),
            BookingSpecifications.isNotDeleted()
        ));

        List<SelectedSlotEntity> holds = selectedSlotRepository
            .findByServiceUserUserIdAndReleaseAtAfterAndSlotStartBeforeAndSlotEndAfter(
                hostUserId,
                now,
                loadTo,
                loadFrom
            );

        List<OutOfOfficeEntity> outOfOfficePeriods = outOfOfficeRepository
            .findByUserUserIdAndStartTimeBeforeAndEndTimeAfter(
                hostUserId,
                dayEnd,
                dayStart
            );

        List<TimeSlot> availableSlots = new ArrayList<>();

        for (AvailabilityEntity availability : availabilities) {
            List<TimeSlot> candidates = bookingWindowFilter.filter(
                slotGenerator.generateSlots(
                    date,
                    availability.getStartTime(),
                    availability.getEndTime(),
                    policy.getDefaultDuration(),
                    policy.getSlotInterval(),
                    zone
                ),
                policy.getMinimumNotice(),
                policy.getMaximumAdvanceBooking(),
                now
            );

            for (TimeSlot candidate : candidates) {
                boolean isAvailable = slotConflictValidator.isSlotAvailable(
                    candidate,
                    bookings,
                    holds,
                    outOfOfficePeriods,
                    policy.getBeforeBuffer(),
                    policy.getAfterBuffer()
                );

                if (isAvailable) {
                    availableSlots.add(candidate);
                }
            }
        }

        return new AvailableSlotsResponse(
            serviceId,
            date,
            zone.getId(),
            availableSlots.stream()
                .distinct()
                .sorted(Comparator.comparing(TimeSlot::slotStart))
                .toList()
        );
    }

    private boolean isAvailableOn(
        AvailabilityEntity availability,
        int weekday
    ) {
        return Arrays.stream(availability.getDays())
            .anyMatch(day -> day != null && day == weekday);
    }
}
