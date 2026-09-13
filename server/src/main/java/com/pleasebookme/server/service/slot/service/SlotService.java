package com.pleasebookme.server.service.slot.service;

import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;

import java.math.BigInteger;
import java.time.LocalDate;

public interface SlotService {
    AvailableSlotsResponse getAvailableSlots(
        BigInteger serviceId,
        LocalDate date
    );
}
