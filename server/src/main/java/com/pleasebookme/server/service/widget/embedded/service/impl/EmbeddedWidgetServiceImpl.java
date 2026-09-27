package com.pleasebookme.server.service.widget.embedded.service.impl;

import com.pleasebookme.server.service.widget.embedded.EmbeddedWidgetOrganizationResolver;
import com.pleasebookme.server.service.widget.embedded.service.EmbeddedWidgetService;
import com.pleasebookme.server.service.widget.barbershop.service.BarbershopBookingService;
import com.pleasebookme.server.service.widget.barbershop.dto.*;
import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmbeddedWidgetServiceImpl implements EmbeddedWidgetService {
    private final EmbeddedWidgetOrganizationResolver resolver;
    private final BarbershopBookingService bookingService;

    @Override
    @Transactional(readOnly = true)
    public WidgetOrganizationResponse getOrganization() {
        return bookingService.getOrganization(resolver.resolve());
    }

    @Override
    @Transactional(readOnly = true)
    public WidgetServiceResponse getService(
        String serviceSlug
    ) {
        return bookingService.getService(
            resolver.resolve(),
            serviceSlug
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AvailableSlotsResponse getSlots(
        String serviceSlug,
        LocalDate date
    ) {
        return bookingService.getSlots(
            resolver.resolve(),
            serviceSlug,
            date
        );
    }

    // Resolve before the flow opens its write transaction and takes the host lock.
    @Override
    public WidgetBookingResponse createBooking(
        String serviceSlug,
        WidgetBookingRequest request
    ) {
        return bookingService.createBooking(
            resolver.resolve(),
            serviceSlug,
            request
        );
    }
}
