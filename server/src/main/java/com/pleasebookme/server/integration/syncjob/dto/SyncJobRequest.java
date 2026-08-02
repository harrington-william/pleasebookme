package com.pleasebookme.server.integration.syncjob.dto;

import com.pleasebookme.server.integration.enums.SyncJobStatus;
import com.pleasebookme.server.integration.enums.SyncJobType;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.Instant;

public record SyncJobRequest(
    @NotNull
    SyncJobType jobType,

    @NotNull
    BigInteger bookingId,

    @NotNull
    BigInteger oauthConnectionId,

    SyncJobStatus status,

    Integer attempts,

    Integer maxAttempts,

    Instant availableAt,

    Instant lastAttemptAt,

    String lastError
) {
}
