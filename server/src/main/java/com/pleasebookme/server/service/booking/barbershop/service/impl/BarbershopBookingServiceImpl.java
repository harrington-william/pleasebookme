package com.pleasebookme.server.service.booking.barbershop.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.attendee.entity.AttendeeEntity;
import com.pleasebookme.server.core.attendee.repository.AttendeeRepository;
import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import com.pleasebookme.server.core.availability.repository.AvailabilityRepository;
import com.pleasebookme.server.core.booking.domain.entity.BookingEntity;
import com.pleasebookme.server.core.booking.domain.repository.BookingRepository;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.enums.BookingStatus;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import com.pleasebookme.server.service.slot.dto.TimeSlot;
import com.pleasebookme.server.service.slot.service.SlotService;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingRequest;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetOrganizationResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetServiceResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetServiceSummary;
import com.pleasebookme.server.service.booking.barbershop.exception.SlotUnavailableException;
import com.pleasebookme.server.service.booking.barbershop.service.BarbershopBookingService;
import com.pleasebookme.server.service.widget.ServedOrganization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BarbershopBookingServiceImpl implements BarbershopBookingService {
    private final ServiceRepository serviceRepository;
    private final BookingPolicyRepository bookingPolicyRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final AttendeeRepository attendeeRepository;
    private final SlotService slotService;

    @Override
    @Transactional(readOnly = true)
    public WidgetOrganizationResponse getOrganization(ServedOrganization served) {
        OrganizationEntity organization = served.organization();

        List<ServiceEntity> services = serviceRepository
            .findByOrganizationOrganizationId(organization.getOrganizationId());
        List<BigInteger> serviceIds = services.stream()
            .map(ServiceEntity::getServiceId)
            .toList();

        // One IN query avoids a policy lookup for every service on the public page.
        Map<BigInteger, BookingPolicyEntity> policiesByServiceId = bookingPolicyRepository
            .findByServiceServiceIdIn(serviceIds)
            .stream()
            .collect(Collectors.toMap(
                policy -> policy.getService().getServiceId(),
                Function.identity()
            ));

        List<WidgetServiceSummary> summaries = services.stream()
            .filter(service -> policiesByServiceId.containsKey(service.getServiceId()))
            .sorted(Comparator.comparing(ServiceEntity::getTitle))
            .map(service -> toSummary(service, policiesByServiceId.get(service.getServiceId())))
            .toList();

        return new WidgetOrganizationResponse(
            organization.getName(),
            organization.getSlug(),
            organization.getLogoUrl(),
            organization.getBannerUrl(),
            organization.getBio(),
            organization.getTimezone(),
            organization.getWeekStart(),
            served.ecosystemCode(),
            summaries
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WidgetServiceResponse getService(
        ServedOrganization served,
        String serviceSlug
    ) {
        OrganizationEntity organization = served.organization();
        ServiceEntity service = resolveService(organization, serviceSlug);

        BookingPolicyEntity policy = resolvePolicy(service);

        List<AvailabilityEntity> availabilities = availabilityRepository
            .findByScheduleScheduleId(service.getSchedule().getScheduleId());

        // Weekdays are a calendar hint only, slot generation remains authoritative
        List<Integer> availableWeekdays = availabilities.stream()
            .flatMap(availability -> Arrays.stream(availability.getDays()))
            .filter(day -> day != null)
            .distinct()
            .sorted()
            .toList();

        return new WidgetServiceResponse(
            new WidgetServiceResponse.Organization(
                organization.getName(),
                organization.getSlug(),
                organization.getLogoUrl(),
                organization.getTimezone(),
                organization.getWeekStart()
            ),
            service.getSlug(),
            service.getTitle(),
            service.getDescription(),
            service.getLocation(),
            policy.getDefaultDuration(),
            service.getMinPrice(),
            service.getMaxPrice(),
            service.getCurrency(),
            service.getSchedule().getTimezone(),
            policy.getMinimumNotice(),
            policy.getMaximumAdvanceBooking(),
            policy.getAutoConfirm(),
            service.getSuccessRedirectUrl(),
            availableWeekdays
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AvailableSlotsResponse getSlots(
        ServedOrganization served,
        String serviceSlug,
        LocalDate date
    ) {
        OrganizationEntity organization = served.organization();
        ServiceEntity service = resolveService(organization, serviceSlug);

        return slotService.getAvailableSlots(service.getServiceId(), date);
    }

    @Override
    @Transactional
    public WidgetBookingResponse createBooking(
        ServedOrganization served,
        String serviceSlug,
        WidgetBookingRequest request
    ) {
        OrganizationEntity organization = served.organization();
        ServiceEntity service = resolveService(organization, serviceSlug);
        BookingPolicyEntity policy = resolvePolicy(service);

        // A later captcha check belongs here, before the transaction takes the host lock.

        // Conflicts are host-wide, so every service owned by this host serializes before
        // the slot engine reads current bookings.
        UserEntity host = userRepository.findLockedByUserId(service.getUser().getUserId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + organization.getSlug()));

        ZoneId zone = ZoneId.of(service.getSchedule().getTimezone());
        LocalDate date = request.slotStart().atZone(zone).toLocalDate();
        Instant slotEnd = request.slotStart().plus(
            policy.getDefaultDuration(),
            ChronoUnit.MINUTES
        );

        AvailableSlotsResponse offered = slotService.getAvailableSlots(
            service.getServiceId(),
            date
        );

        if (!offered.slots().contains(new TimeSlot(request.slotStart(), slotEnd))) {
            throw new SlotUnavailableException("That time is no longer available");
        }

        // PENDING has broader legacy meaning, AWAITING_HOST explicitly represents
        // a valid public request that still needs the business to confirm it.
        BookingStatus status = Boolean.TRUE.equals(policy.getAutoConfirm())
            ? BookingStatus.ACCEPTED
            : BookingStatus.AWAITING_HOST;

        BookingEntity booking = bookingRepository.save(
            BookingEntity.builder()
                .user(host)
                .service(service)
                .title(service.getTitle() + " - " + request.name())
                .description(request.notes())
                .location(service.getLocation())
                .startTime(request.slotStart())
                .endTime(slotEnd)
                .status(status)
                .build()
        );

        attendeeRepository.save(
            AttendeeEntity.builder()
                .booking(booking)
                .name(request.name())
                .phone(request.phone())
                .email(request.email())
                .timezone(request.timezone())
                .build()
        );

        return new WidgetBookingResponse(
            booking.getBookingUid(),
            booking.getStatus(),
            booking.getStartTime(),
            booking.getEndTime(),
            zone.getId(),
            service.getTitle(),
            organization.getName()
        );
    }

    private ServiceEntity resolveService(
        OrganizationEntity organization,
        String serviceSlug
    ) {
        return serviceRepository.findByOrganizationOrganizationIdAndSlug(
            organization.getOrganizationId(),
            serviceSlug
        ).orElseThrow(() -> serviceNotFound(serviceSlug));
    }

    private BookingPolicyEntity resolvePolicy(ServiceEntity service) {
        return bookingPolicyRepository.findByServiceServiceId(service.getServiceId())
            .orElseThrow(() -> serviceNotFound(service.getSlug()));
    }

    private WidgetServiceSummary toSummary(
        ServiceEntity service,
        BookingPolicyEntity policy
    ) {
        return new WidgetServiceSummary(
            service.getSlug(),
            service.getTitle(),
            service.getDescription(),
            service.getLocation(),
            policy.getDefaultDuration(),
            service.getMinPrice(),
            service.getMaxPrice(),
            service.getCurrency(),
            policy.getAutoConfirm()
        );
    }

    private ServiceNotFoundException serviceNotFound(String serviceSlug) {
        return new ServiceNotFoundException("Service not found: " + serviceSlug);
    }
}
