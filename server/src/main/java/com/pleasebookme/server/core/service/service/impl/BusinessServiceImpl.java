package com.pleasebookme.server.core.service.service.impl;

import com.pleasebookme.server.core.bookingpolicy.dto.ServiceBookingPolicyRequest;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.exception.ScheduleNotFoundException;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.core.service.dto.ServiceRequest;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.DuplicateServiceException;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.core.service.service.BusinessServiceService;
import com.pleasebookme.server.core.service.dto.ServiceCreateResult;
import com.pleasebookme.server.integration.calendar.entity.DestinationCalendarEntity;
import com.pleasebookme.server.integration.calendar.exception.DestinationCalendarNotFoundException;
import com.pleasebookme.server.integration.calendar.repository.DestinationCalendarRepository;
import com.pleasebookme.server.integration.sheets.entity.DestinationSheetsEntity;
import com.pleasebookme.server.integration.sheets.exception.DestinationSheetsNotFoundException;
import com.pleasebookme.server.integration.sheets.repository.DestinationSheetsRepository;
import com.pleasebookme.server.service.organization.context.OrganizationContext;
import com.pleasebookme.server.service.organization.service.CurrentOrganizationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessServiceService {
    private final ServiceRepository serviceRepository;
    private final BookingPolicyRepository bookingPolicyRepository;
    private final ScheduleRepository scheduleRepository;
    private final DestinationCalendarRepository destinationCalendarRepository;
    private final DestinationSheetsRepository destinationSheetsRepository;
    private final CurrentOrganizationProvider currentOrganizationProvider;

    @Override
    @Transactional
    public ServiceCreateResult createService(ServiceRequest request) {
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();

        if (serviceRepository.existsByOrganizationOrganizationIdAndSlug(organizationContext.organizationId(), request.slug())) {
            throw new DuplicateServiceException("Slug already exists for organization: " + request.slug());
        }

        ScheduleEntity schedule = scheduleRepository.findById(request.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + request.scheduleId()));

        ServiceEntity.ServiceEntityBuilder service = ServiceEntity.builder()
            .title(request.title())
            .slug(request.slug())
            .description(request.description())
            .location(request.location())
            .user(organizationContext.user())
            .profile(currentOrganizationProvider.requireProfile(organizationContext))
            .organization(organizationContext.organization())
            .schedule(schedule)
            .minPrice(request.minPrice())
            .maxPrice(request.maxPrice())
            .successRedirectUrl(request.successRedirectUrl())
            .maxActiveBookingPerBooker(request.maxActiveBookingPerBooker())
            .destinationCalendar(resolveDestinationCalendar(request.destinationCalendarId(), null))
            .destinationSheets(resolveDestinationSheets(request.destinationSheetsId(), null));

        if (request.interfaceLanguage() != null) service.interfaceLanguage(request.interfaceLanguage());
        if (request.periodType() != null) service.periodType(request.periodType());
        if (request.timezone() != null) service.timezone(request.timezone());
        if (request.currency() != null) service.currency(request.currency());
        if (request.disableCancelling() != null) service.disableCancelling(request.disableCancelling());
        if (request.disableRescheduling() != null) service.disableRescheduling(request.disableRescheduling());
        if (request.isInstantService() != null) service.isInstantService(request.isInstantService());

        ServiceEntity createdService = serviceRepository.save(service.build());
        BookingPolicyEntity bookingPolicy = null;

        if (request.bookingPolicy() != null) {
            bookingPolicy = bookingPolicyRepository.save(buildBookingPolicy(createdService, request.bookingPolicy()));
        }

        return new ServiceCreateResult(createdService, bookingPolicy);
    }

    @Override
    public ServiceCreateResult getServiceById(BigInteger serviceId) {
        ServiceEntity service = findServiceOrThrow(serviceId);
        BookingPolicyEntity bookingPolicy = bookingPolicyRepository.findByServiceServiceId(serviceId).orElse(null);

        return new ServiceCreateResult(service, bookingPolicy);
    }

    @Override
    public List<ServiceEntity> getServicesByOrganizationId(BigInteger organizationId) {
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();

        if (!organizationContext.organizationId().equals(organizationId)) {
            return List.of();
        }

        return serviceRepository.findByOrganizationOrganizationId(organizationId);
    }

    @Override
    @Transactional
    public ServiceCreateResult updateService(
        BigInteger serviceId,
        ServiceRequest request
    ) {
        ServiceEntity service = findServiceOrThrow(serviceId);
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();

        if (!service.getOrganization().getOrganizationId().equals(organizationContext.organizationId())) {
            throw new ServiceNotFoundException("Service not found: " + serviceId);
        }

        ScheduleEntity schedule = scheduleRepository.findById(request.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + request.scheduleId()));

        service.setTitle(request.title());
        service.setSlug(request.slug());
        service.setDescription(request.description());
        service.setLocation(request.location());
        service.setSchedule(schedule);
        service.setMinPrice(request.minPrice());
        service.setMaxPrice(request.maxPrice());
        service.setSuccessRedirectUrl(request.successRedirectUrl());
        service.setMaxActiveBookingPerBooker(request.maxActiveBookingPerBooker());
        service.setDestinationCalendar(resolveDestinationCalendar(request.destinationCalendarId(), service.getDestinationCalendar()));
        service.setDestinationSheets(resolveDestinationSheets(request.destinationSheetsId(), service.getDestinationSheets()));

        if (request.interfaceLanguage() != null) service.setInterfaceLanguage(request.interfaceLanguage());
        if (request.periodType() != null) service.setPeriodType(request.periodType());
        if (request.timezone() != null) service.setTimezone(request.timezone());
        if (request.currency() != null) service.setCurrency(request.currency());
        if (request.disableCancelling() != null) service.setDisableCancelling(request.disableCancelling());
        if (request.disableRescheduling() != null) service.setDisableRescheduling(request.disableRescheduling());
        if (request.isInstantService() != null) service.setIsInstantService(request.isInstantService());

        ServiceEntity updatedService = serviceRepository.save(service);
        BookingPolicyEntity bookingPolicy = upsertBookingPolicy(updatedService, request.bookingPolicy());

        return new ServiceCreateResult(updatedService, bookingPolicy);
    }

    @Override
    public void deleteService(BigInteger serviceId) {
        serviceRepository.delete(findServiceOrThrow(serviceId));
    }

    private ServiceEntity findServiceOrThrow(BigInteger serviceId) {
        return serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + serviceId));
    }

    /**
     * A service owns exactly one booking policy (uq_booking_policies_service), so an
     * update has to reuse the existing row rather than insert a second one. Keeping
     * this here rather than calling BookingPolicyService keeps both writes inside the
     * caller's transaction, matching how createService already persists the pair.
     */
    private BookingPolicyEntity upsertBookingPolicy(
        ServiceEntity service,
        ServiceBookingPolicyRequest request
    ) {
        if (request == null) {
            return bookingPolicyRepository.findByServiceServiceId(service.getServiceId()).orElse(null);
        }

        return bookingPolicyRepository.findByServiceServiceId(service.getServiceId())
            .map(existing -> bookingPolicyRepository.save(applyBookingPolicy(existing, request)))
            .orElseGet(() -> bookingPolicyRepository.save(buildBookingPolicy(service, request)));
    }

    /**
     * Update is full-replace everywhere else, but no client surface manages
     * destination calendars yet, so replacing a missing id with null would silently
     * unlink a connected calendar on every save. Keep the stored association until
     * a picker exists that can send an explicit clear.
     */
    private DestinationCalendarEntity resolveDestinationCalendar(
        BigInteger destinationCalendarId,
        DestinationCalendarEntity current
    ) {
        if (destinationCalendarId == null) {
            return current;
        }

        return destinationCalendarRepository.findById(destinationCalendarId)
            .orElseThrow(() -> new DestinationCalendarNotFoundException("Destination calendar not found: " + destinationCalendarId));
    }

    private DestinationSheetsEntity resolveDestinationSheets(
        BigInteger destinationSheetsId,
        DestinationSheetsEntity current
    ) {
        if (destinationSheetsId == null) {
            return current;
        }

        return destinationSheetsRepository.findById(destinationSheetsId)
            .orElseThrow(() -> new DestinationSheetsNotFoundException("Destination sheets not found: " + destinationSheetsId));
    }

    private BookingPolicyEntity buildBookingPolicy(
        ServiceEntity service,
        ServiceBookingPolicyRequest request
    ) {
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

        return bookingPolicy.build();
    }

    private BookingPolicyEntity applyBookingPolicy(
        BookingPolicyEntity bookingPolicy,
        ServiceBookingPolicyRequest request
    ) {
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

        return bookingPolicy;
    }
}
