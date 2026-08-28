package com.pleasebookme.server.core.service.services.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.bookingpolicy.dto.ServiceBookingPolicyRequest;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.exception.DuplicateBookingPolicyException;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.exception.ScheduleNotFoundException;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.core.service.dto.ServiceRequest;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.DuplicateServiceException;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.core.service.services.BusinessServiceService;
import com.pleasebookme.server.core.service.services.ServiceCreateResult;
import com.pleasebookme.server.integration.calendar.entity.DestinationCalendarEntity;
import com.pleasebookme.server.integration.calendar.exception.DestinationCalendarNotFoundException;
import com.pleasebookme.server.integration.calendar.repository.DestinationCalendarRepository;
import com.pleasebookme.server.integration.sheets.entity.DestinationSheetsEntity;
import com.pleasebookme.server.integration.sheets.exception.DestinationSheetsNotFoundException;
import com.pleasebookme.server.integration.sheets.repository.DestinationSheetsRepository;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import com.pleasebookme.server.organization.profile.exception.ProfileNotFoundException;
import com.pleasebookme.server.organization.profile.repository.ProfileRepository;
import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
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
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ScheduleRepository scheduleRepository;
    private final DestinationCalendarRepository destinationCalendarRepository;
    private final DestinationSheetsRepository destinationSheetsRepository;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    @Override
    @Transactional
    public ServiceCreateResult createService(ServiceRequest request) {
        OwnerContext owner = resolveCurrentOwner();

        if (serviceRepository.existsByOrganizationOrganizationIdAndSlug(owner.organization().getOrganizationId(), request.slug())) {
            throw new DuplicateServiceException("Slug already exists for organization: " + request.slug());
        }

        ScheduleEntity schedule = scheduleRepository.findById(request.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + request.scheduleId()));

        ServiceEntity.ServiceEntityBuilder service = ServiceEntity.builder()
            .title(request.title())
            .slug(request.slug())
            .description(request.description())
            .location(request.location())
            .user(owner.user())
            .profile(owner.profile())
            .organization(owner.organization())
            .schedule(schedule)
            .minPrice(request.minPrice())
            .maxPrice(request.maxPrice())
            .successRedirectUrl(request.successRedirectUrl())
            .maxActiveBookingPerBooker(request.maxActiveBookingPerBooker());

        if (request.interfaceLanguage() != null) service.interfaceLanguage(request.interfaceLanguage());
        if (request.periodType() != null) service.periodType(request.periodType());
        if (request.timezone() != null) service.timezone(request.timezone());
        if (request.currency() != null) service.currency(request.currency());
        if (request.requiresConfirmation() != null) service.requiresConfirmation(request.requiresConfirmation());
        if (request.disableCancelling() != null) service.disableCancelling(request.disableCancelling());
        if (request.disableRescheduling() != null) service.disableRescheduling(request.disableRescheduling());
        if (request.isInstantService() != null) service.isInstantService(request.isInstantService());

        if (request.destinationCalendarId() != null) {
            DestinationCalendarEntity destinationCalendar = destinationCalendarRepository.findById(request.destinationCalendarId())
                .orElseThrow(() -> new DestinationCalendarNotFoundException("Destination calendar not found: " + request.destinationCalendarId()));
            service.destinationCalendar(destinationCalendar);
        }

        if (request.destinationSheetsId() != null) {
            DestinationSheetsEntity destinationSheets = destinationSheetsRepository.findById(request.destinationSheetsId())
                .orElseThrow(() -> new DestinationSheetsNotFoundException("Destination sheets not found: " + request.destinationSheetsId()));
            service.destinationSheets(destinationSheets);
        }

        ServiceEntity createdService = serviceRepository.save(service.build());
        BookingPolicyEntity bookingPolicy = null;

        if (request.bookingPolicy() != null) {
            if (bookingPolicyRepository.existsByServiceServiceId(createdService.getServiceId())) {
                throw new DuplicateBookingPolicyException("Booking policy already exists for service: " + createdService.getServiceId());
            }

            // TASK-0002 deliberately keeps this cross-domain write local instead of adding a service/core orchestration bean.
            bookingPolicy = bookingPolicyRepository.save(buildBookingPolicy(createdService, request.bookingPolicy()));
        }

        return new ServiceCreateResult(createdService, bookingPolicy);
    }

    @Override
    public ServiceEntity getServiceById(BigInteger serviceId) {
        return serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException(
                "Service not found: " + serviceId
            ));
    }

    @Override
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    @Override
    @Transactional
    public ServiceEntity updateService(
        BigInteger serviceId,
        ServiceRequest request
    ) {
        ServiceEntity service = getServiceById(serviceId);
        OwnerContext owner = resolveCurrentOwner();

        ScheduleEntity schedule = scheduleRepository.findById(request.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + request.scheduleId()));

        service.setTitle(request.title());
        service.setSlug(request.slug());
        service.setDescription(request.description());
        service.setLocation(request.location());
        service.setUser(owner.user());
        service.setProfile(owner.profile());
        service.setOrganization(owner.organization());
        service.setSchedule(schedule);
        service.setMinPrice(request.minPrice());
        service.setMaxPrice(request.maxPrice());
        service.setSuccessRedirectUrl(request.successRedirectUrl());
        service.setMaxActiveBookingPerBooker(request.maxActiveBookingPerBooker());

        if (request.interfaceLanguage() != null) service.setInterfaceLanguage(request.interfaceLanguage());
        if (request.periodType() != null) service.setPeriodType(request.periodType());
        if (request.timezone() != null) service.setTimezone(request.timezone());
        if (request.currency() != null) service.setCurrency(request.currency());
        if (request.requiresConfirmation() != null) service.setRequiresConfirmation(request.requiresConfirmation());
        if (request.disableCancelling() != null) service.setDisableCancelling(request.disableCancelling());
        if (request.disableRescheduling() != null) service.setDisableRescheduling(request.disableRescheduling());
        if (request.isInstantService() != null) service.setIsInstantService(request.isInstantService());

        if (request.destinationCalendarId() != null) {
            DestinationCalendarEntity destinationCalendar = destinationCalendarRepository.findById(request.destinationCalendarId())
                .orElseThrow(() -> new DestinationCalendarNotFoundException("Destination calendar not found: " + request.destinationCalendarId()));
            service.setDestinationCalendar(destinationCalendar);
        } else {
            service.setDestinationCalendar(null);
        }

        if (request.destinationSheetsId() != null) {
            DestinationSheetsEntity destinationSheets = destinationSheetsRepository.findById(request.destinationSheetsId())
                .orElseThrow(() -> new DestinationSheetsNotFoundException("Destination sheets not found: " + request.destinationSheetsId()));
            service.setDestinationSheets(destinationSheets);
        } else {
            service.setDestinationSheets(null);
        }

        return serviceRepository.save(service);
    }

    @Override
    public void deleteService(BigInteger serviceId) {
        serviceRepository.delete(getServiceById(serviceId));
    }

    private OwnerContext resolveCurrentOwner() {
        BigInteger userId = currentPrincipalProvider.requireUser().userId();

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        ProfileEntity profile = profileRepository.findByUserUserId(userId)
            .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + userId));

        return new OwnerContext(user, profile, profile.getOrganization());
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

    private record OwnerContext(
        UserEntity user,
        ProfileEntity profile,
        OrganizationEntity organization
    ) {
    }
}
