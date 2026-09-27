package com.pleasebookme.server.service.widget.barbershop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record WidgetBookingRequest(
    @NotBlank @Size(max = 255)
    String name,

    @NotBlank @Size(max = 50)
    String phone,

    @Size(max = 255)
    String email,

    @Size(max = 100)
    String timezone,

    @NotNull
    Instant slotStart,

    String notes
) {}
