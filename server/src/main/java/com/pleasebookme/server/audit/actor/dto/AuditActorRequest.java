package com.pleasebookme.server.audit.actor.dto;

import com.pleasebookme.server.audit.enums.AuditActorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.net.InetAddress;

public record AuditActorRequest(
    @NotNull
    AuditActorType actorType,

    @Size(max = 255)
    String userUid,

    BigInteger membershipId,

    @Size(max = 255)
    String widgetUid,

    @Size(max = 255)
    String apiKeyUid,

    BigInteger attendeeId,

    @Size(max = 100)
    String systemName,

    @NotBlank
    @Size(max = 255)
    String displayName,

    @Size(max = 255)
    String email,

    @NotNull
    InetAddress ipAddress,

    @NotBlank
    String userAgent
) {
}
