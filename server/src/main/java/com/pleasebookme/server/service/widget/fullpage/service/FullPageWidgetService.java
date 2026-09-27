package com.pleasebookme.server.service.widget.fullpage.service;

import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import com.pleasebookme.server.service.widget.barbershop.dto.WidgetBookingRequest;
import com.pleasebookme.server.service.widget.barbershop.dto.WidgetBookingResponse;
import com.pleasebookme.server.service.widget.barbershop.dto.WidgetOrganizationResponse;
import com.pleasebookme.server.service.widget.barbershop.dto.WidgetServiceResponse;

import java.time.LocalDate;

public interface FullPageWidgetService {
    WidgetOrganizationResponse getOrganization(String organizationSlug);

    WidgetServiceResponse getService(
        String organizationSlug,
        String serviceSlug
    );

    AvailableSlotsResponse getSlots(
        String organizationSlug,
        String serviceSlug,
        LocalDate date
    );

    WidgetBookingResponse createBooking(
        String organizationSlug,
        String serviceSlug,
        WidgetBookingRequest request
    );
}
