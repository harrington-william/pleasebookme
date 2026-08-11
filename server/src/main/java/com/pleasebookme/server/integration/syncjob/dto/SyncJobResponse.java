package com.pleasebookme.server.integration.syncjob.dto;

import com.pleasebookme.server.integration.enums.SyncJobStatus;
import com.pleasebookme.server.integration.enums.SyncJobType;
import com.pleasebookme.server.integration.syncjob.entity.SyncJobEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record SyncJobResponse(
    BigInteger syncJobId,
    UUID syncJobUid,
    SyncJobType jobType,
    BigInteger bookingId,
    BigInteger oauthConnectionId,
    SyncJobStatus status,
    Integer attempts,
    Integer maxAttempts,
    Instant availableAt,
    Instant lastAttemptAt,
    String lastError,
    Instant createdAt,
    Instant updatedAt
) {
    public static SyncJobResponse from(SyncJobEntity syncJob) {
        return new SyncJobResponse(
            syncJob.getSyncJobId(),
            syncJob.getSyncJobUid(),
            syncJob.getJobType(),
            syncJob.getBooking().getBookingId(),
            syncJob.getOauthConnection().getOauthConnectionId(),
            syncJob.getStatus(),
            syncJob.getAttempts(),
            syncJob.getMaxAttempts(),
            syncJob.getAvailableAt(),
            syncJob.getLastAttemptAt(),
            syncJob.getLastError(),
            syncJob.getCreatedAt(),
            syncJob.getUpdatedAt()
        );
    }
}
