package com.pleasebookme.server.service.slot.dto;

import java.time.Instant;

public record TimeSlot(
    Instant slotStart,
    Instant slotEnd
) {}
