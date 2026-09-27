package com.pleasebookme.server.service.widget.fullpage.service.impl;

import com.pleasebookme.server.service.widget.fullpage.SlugOrganizationResolver;
import com.pleasebookme.server.service.widget.fullpage.service.FullPageWidgetService;
import com.pleasebookme.server.service.widget.barbershop.service.BarbershopBookingService;
import com.pleasebookme.server.service.widget.barbershop.dto.*;
import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FullPageWidgetServiceImpl implements FullPageWidgetService {
    private final SlugOrganizationResolver resolver;
    private final BarbershopBookingService bookingService;

    @Override
    @Transactional(readOnly = true)
    public WidgetOrganizationResponse getOrganization(String organizationSlug) {
        return bookingService.getOrganization(resolver.resolve(organizationSlug));
    }

    @Override
    @Transactional(readOnly = true)
    public WidgetServiceResponse getService(
        String organizationSlug,
        String serviceSlug
    ) {
        return bookingService.getService(
            resolver.resolve(organizationSlug),
            serviceSlug
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AvailableSlotsResponse getSlots(
        String organizationSlug,
        String serviceSlug,
        LocalDate date
    ) {
        return bookingService.getSlots(
            resolver.resolve(organizationSlug),
            serviceSlug,
            date
        );
    }

    // Resolve before the flow opens its write transaction and takes the host lock.
    @Override
    public WidgetBookingResponse createBooking(
        String organizationSlug,
        String serviceSlug,
        WidgetBookingRequest request
    ) {
        return bookingService.createBooking(
            resolver.resolve(organizationSlug),
            serviceSlug,
            request
        );
    }
}
