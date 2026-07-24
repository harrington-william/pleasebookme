package com.pleasebookme.server.security.token.jwt.claims;

import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.token.jwt.enums.JwtTokenType;

import java.time.Instant;
import java.util.UUID;

public record JwtClaims(
    AuthenticatedActorType actorType,

    // User UUID
    UUID subject,

    UUID tenant,

    UUID tokenId,
    JwtTokenType tokenType,

    Instant issuedAt,
    Instant expiresAt
) {}
