package com.pleasebookme.server.service.slot.dto;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

public record AvailableSlotsResponse(
    BigInteger serviceId,
    LocalDate date,
    String timezone,
    List<TimeSlot> slots
) {
}
