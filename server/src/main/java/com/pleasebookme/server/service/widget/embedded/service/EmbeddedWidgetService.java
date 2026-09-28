package com.pleasebookme.server.service.widget.embedded.service;

import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingRequest;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetOrganizationResponse;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetServiceResponse;

import java.time.LocalDate;

public interface EmbeddedWidgetService {
    WidgetOrganizationResponse getOrganization();

    WidgetServiceResponse getService(
        String serviceSlug
    );

    AvailableSlotsResponse getSlots(
        String serviceSlug,
        LocalDate date
    );

    WidgetBookingResponse createBooking(
        String serviceSlug,
        WidgetBookingRequest request
    );
}
