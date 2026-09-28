package com.pleasebookme.server.service.booking.barbershop.service;

import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingRequest;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetOrganizationResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetServiceResponse;

import java.time.LocalDate;
import com.pleasebookme.server.service.widget.ServedOrganization;

public interface BarbershopBookingService {
    WidgetOrganizationResponse getOrganization(ServedOrganization served);

    WidgetServiceResponse getService(
        ServedOrganization served,
        String serviceSlug
    );

    AvailableSlotsResponse getSlots(
        ServedOrganization served,
        String serviceSlug,
        LocalDate date
    );

    WidgetBookingResponse createBooking(
        ServedOrganization served,
        String serviceSlug,
        WidgetBookingRequest request
    );
}
