package com.pleasebookme.server.tenant.ecosystem.service.impl;

import com.pleasebookme.server.tenant.ecosystem.dto.EcosystemRequest;
import com.pleasebookme.server.tenant.ecosystem.entity.EcosystemEntity;
import com.pleasebookme.server.tenant.ecosystem.exception.DuplicateEcosystemException;
import com.pleasebookme.server.tenant.ecosystem.exception.EcosystemNotFoundException;
import com.pleasebookme.server.tenant.ecosystem.repository.EcosystemRepository;
import com.pleasebookme.server.tenant.ecosystem.service.EcosystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EcosystemServiceImpl implements EcosystemService {
    private final EcosystemRepository ecosystemRepository;

    @Override
    public EcosystemEntity createEcosystem(EcosystemRequest request) {
        if (ecosystemRepository.existsByCode(request.code())) {
            throw new DuplicateEcosystemException("Code already exists: " + request.code());
        }

        EcosystemEntity.EcosystemEntityBuilder ecosystem = EcosystemEntity.builder()
            .code(request.code())
            .name(request.name())
            .description(request.description())
            .icon(request.icon());

        if (request.status() != null) ecosystem.status(request.status());

        return ecosystemRepository.save(ecosystem.build());
    }

    @Override
    public EcosystemEntity getEcosystemById(BigInteger ecosystemId) {
        return ecosystemRepository.findById(ecosystemId)
            .orElseThrow(() -> new EcosystemNotFoundException(
                "Ecosystem not found: " + ecosystemId
            ));
    }

    @Override
    public List<EcosystemEntity> getAllEcosystems() {
        return ecosystemRepository.findAll();
    }

    @Override
    public EcosystemEntity updateEcosystem(
        BigInteger ecosystemId,
        EcosystemRequest request
    ) {
        EcosystemEntity ecosystem = getEcosystemById(ecosystemId);

        ecosystem.setCode(request.code());
        ecosystem.setName(request.name());
        ecosystem.setDescription(request.description());
        ecosystem.setIcon(request.icon());

        if (request.status() != null) ecosystem.setStatus(request.status());

        return ecosystemRepository.save(ecosystem);
    }

    @Override
    public void deleteEcosystem(BigInteger ecosystemId) {
        ecosystemRepository.delete(getEcosystemById(ecosystemId));
    }
}
