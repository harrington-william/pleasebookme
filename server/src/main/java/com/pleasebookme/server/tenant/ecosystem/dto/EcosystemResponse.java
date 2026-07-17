package com.pleasebookme.server.tenant.ecosystem.dto;

import com.pleasebookme.server.tenant.ecosystem.entity.EcosystemEntity;
import com.pleasebookme.server.tenant.enums.EcosystemStatus;

import java.math.BigInteger;
import java.time.Instant;

public record EcosystemResponse(
    BigInteger ecosystemId,
    String code,
    String name,
    String description,
    String icon,
    EcosystemStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static EcosystemResponse from(EcosystemEntity ecosystem) {
        return new EcosystemResponse(
            ecosystem.getEcosystemId(),
            ecosystem.getCode(),
            ecosystem.getName(),
            ecosystem.getDescription(),
            ecosystem.getIcon(),
            ecosystem.getStatus(),
            ecosystem.getCreatedAt(),
            ecosystem.getUpdatedAt()
        );
    }
}
