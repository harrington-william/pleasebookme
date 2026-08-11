package com.pleasebookme.server.integration.syncjob.service;

import com.pleasebookme.server.integration.syncjob.dto.SyncJobRequest;
import com.pleasebookme.server.integration.syncjob.entity.SyncJobEntity;

import java.math.BigInteger;
import java.util.List;

public interface SyncJobService {
    SyncJobEntity createSyncJob(SyncJobRequest request);

    SyncJobEntity getSyncJobById(BigInteger syncJobId);

    List<SyncJobEntity> getAllSyncJobs();

    SyncJobEntity updateSyncJob(
        BigInteger syncJobId,
        SyncJobRequest request
    );

    void deleteSyncJob(BigInteger syncJobId);
}
