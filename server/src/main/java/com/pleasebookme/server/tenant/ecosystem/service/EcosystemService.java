package com.pleasebookme.server.tenant.ecosystem.service;

import com.pleasebookme.server.tenant.ecosystem.dto.EcosystemRequest;
import com.pleasebookme.server.tenant.ecosystem.entity.EcosystemEntity;

import java.math.BigInteger;
import java.util.List;

public interface EcosystemService {
    EcosystemEntity createEcosystem(EcosystemRequest request);

    EcosystemEntity getEcosystemById(BigInteger ecosystemId);

    List<EcosystemEntity> getAllEcosystems();

    EcosystemEntity updateEcosystem(
        BigInteger ecosystemId,
        EcosystemRequest request
    );

    void deleteEcosystem(BigInteger ecosystemId);
}
