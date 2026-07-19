package com.pleasebookme.server.core.selectedslot.dto;

import com.pleasebookme.server.core.selectedslot.entity.SelectedSlotEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record SelectedSlotResponse(
    BigInteger selectedSlotId,
    UUID selectedSlotUid,
    BigInteger serviceId,
    BigInteger userId,
    Instant slotStart,
    Instant slotEnd,
    Instant releaseAt,
    Boolean isSeat,
    Instant createdAt
) {
    public static SelectedSlotResponse from(SelectedSlotEntity selectedSlot) {
        return new SelectedSlotResponse(
            selectedSlot.getSelectedSlotId(),
            selectedSlot.getSelectedSlotUid(),
            selectedSlot.getService().getServiceId(),
            selectedSlot.getUser().getUserId(),
            selectedSlot.getSlotStart(),
            selectedSlot.getSlotEnd(),
            selectedSlot.getReleaseAt(),
            selectedSlot.getIsSeat(),
            selectedSlot.getCreatedAt()
        );
    }
}
