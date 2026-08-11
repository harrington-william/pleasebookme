package com.pleasebookme.server.integration.syncjob.service.impl;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.booking.exception.BookingNotFoundException;
import com.pleasebookme.server.core.booking.repository.BookingRepository;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.exception.OAuthConnectionNotFoundException;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import com.pleasebookme.server.integration.syncjob.dto.SyncJobRequest;
import com.pleasebookme.server.integration.syncjob.entity.SyncJobEntity;
import com.pleasebookme.server.integration.syncjob.exception.SyncJobNotFoundException;
import com.pleasebookme.server.integration.syncjob.repository.SyncJobRepository;
import com.pleasebookme.server.integration.syncjob.service.SyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncJobServiceImpl implements SyncJobService {
    private final SyncJobRepository syncJobRepository;
    private final BookingRepository bookingRepository;
    private final OAuthConnectionRepository oauthConnectionRepository;

    @Override
    public SyncJobEntity createSyncJob(SyncJobRequest request) {
        BookingEntity booking = bookingRepository.findById(request.bookingId())
            .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + request.bookingId()));

        OAuthConnectionEntity oauthConnection = oauthConnectionRepository.findById(request.oauthConnectionId())
            .orElseThrow(() -> new OAuthConnectionNotFoundException(
                "OAuth connection not found: " + request.oauthConnectionId()
            ));

        SyncJobEntity.SyncJobEntityBuilder syncJob = SyncJobEntity.builder()
            .jobType(request.jobType())
            .booking(booking)
            .oauthConnection(oauthConnection)
            .lastAttemptAt(request.lastAttemptAt())
            .lastError(request.lastError());

        if (request.status() != null) syncJob.status(request.status());
        if (request.attempts() != null) syncJob.attempts(request.attempts());
        if (request.maxAttempts() != null) syncJob.maxAttempts(request.maxAttempts());
        if (request.availableAt() != null) syncJob.availableAt(request.availableAt());

        return syncJobRepository.save(syncJob.build());
    }

    @Override
    public SyncJobEntity getSyncJobById(BigInteger syncJobId) {
        return syncJobRepository.findById(syncJobId)
            .orElseThrow(() -> new SyncJobNotFoundException("Sync job not found: " + syncJobId));
    }

    @Override
    public List<SyncJobEntity> getAllSyncJobs() {
        return syncJobRepository.findAll();
    }

    @Override
    public SyncJobEntity updateSyncJob(
        BigInteger syncJobId,
        SyncJobRequest request
    ) {
        SyncJobEntity syncJob = getSyncJobById(syncJobId);

        BookingEntity booking = bookingRepository.findById(request.bookingId())
            .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + request.bookingId()));

        OAuthConnectionEntity oauthConnection = oauthConnectionRepository.findById(request.oauthConnectionId())
            .orElseThrow(() -> new OAuthConnectionNotFoundException(
                "OAuth connection not found: " + request.oauthConnectionId()
            ));

        syncJob.setJobType(request.jobType());
        syncJob.setBooking(booking);
        syncJob.setOauthConnection(oauthConnection);
        syncJob.setLastAttemptAt(request.lastAttemptAt());
        syncJob.setLastError(request.lastError());

        if (request.status() != null) syncJob.setStatus(request.status());
        if (request.attempts() != null) syncJob.setAttempts(request.attempts());
        if (request.maxAttempts() != null) syncJob.setMaxAttempts(request.maxAttempts());
        if (request.availableAt() != null) syncJob.setAvailableAt(request.availableAt());

        return syncJobRepository.save(syncJob);
    }

    @Override
    public void deleteSyncJob(BigInteger syncJobId) {
        syncJobRepository.delete(getSyncJobById(syncJobId));
    }
}
