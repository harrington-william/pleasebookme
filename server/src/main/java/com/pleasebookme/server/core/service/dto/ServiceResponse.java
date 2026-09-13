package com.pleasebookme.server.core.service.dto;

import com.pleasebookme.server.core.bookingpolicy.dto.BookingPolicyResponse;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.global.enums.Currency;
import com.pleasebookme.server.global.enums.Locale;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public record ServiceResponse(
    BigInteger serviceId,
    String title,
    String slug,
    String description,
    Locale interfaceLanguage,
    String location,
    BigInteger userId,
    BigInteger profileId,
    BigInteger organizationId,
    BigInteger scheduleId,
    String periodType,
    String timezone,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Currency currency,
    Boolean disableCancelling,
    Boolean disableRescheduling,
    String successRedirectUrl,
    Boolean isInstantService,
    Integer maxActiveBookingPerBooker,
    BigInteger destinationCalendarId,
    BigInteger destinationSheetsId,
    BookingPolicyResponse bookingPolicy,
    Instant createdAt,
    Instant updatedAt
) {
    public static ServiceResponse from(ServiceEntity service) {
        return from(service, null);
    }

    public static ServiceResponse from(ServiceEntity service, BookingPolicyEntity bookingPolicy) {
        return new ServiceResponse(
            service.getServiceId(),
            service.getTitle(),
            service.getSlug(),
            service.getDescription(),
            service.getInterfaceLanguage(),
            service.getLocation(),
            service.getUser().getUserId(),
            service.getProfile().getProfileId(),
            service.getOrganization().getOrganizationId(),
            service.getSchedule().getScheduleId(),
            service.getPeriodType(),
            service.getTimezone(),
            service.getMinPrice(),
            service.getMaxPrice(),
            service.getCurrency(),
            service.getDisableCancelling(),
            service.getDisableRescheduling(),
            service.getSuccessRedirectUrl(),
            service.getIsInstantService(),
            service.getMaxActiveBookingPerBooker(),
            service.getDestinationCalendar() != null ? service.getDestinationCalendar().getDestinationCalendarId() : null,
            service.getDestinationSheets() != null ? service.getDestinationSheets().getDestinationSheetsId() : null,
            bookingPolicy != null ? BookingPolicyResponse.from(bookingPolicy) : null,
            service.getCreatedAt(),
            service.getUpdatedAt()
        );
    }
}
