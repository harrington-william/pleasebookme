package com.pleasebookme.server.core.outofoffice.dto;

import com.pleasebookme.server.core.outofoffice.entity.OutOfOfficeEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record OutOfOfficeResponse(
    BigInteger outOfOfficeId,
    UUID outOfOfficeUid,
    Instant startTime,
    Instant endTime,
    String notes,
    Boolean showNotePublicly,
    BigInteger userId,
    BigInteger toUserId,
    String reason,
    Instant createdAt,
    Instant updatedAt
) {
    public static OutOfOfficeResponse from(OutOfOfficeEntity outOfOffice) {
        return new OutOfOfficeResponse(
            outOfOffice.getOutOfOfficeId(),
            outOfOffice.getOutOfOfficeUid(),
            outOfOffice.getStartTime(),
            outOfOffice.getEndTime(),
            outOfOffice.getNotes(),
            outOfOffice.getShowNotePublicly(),
            outOfOffice.getUser().getUserId(),
            outOfOffice.getToUser().getUserId(),
            outOfOffice.getReason(),
            outOfOffice.getCreatedAt(),
            outOfOffice.getUpdatedAt()
        );
    }
}
