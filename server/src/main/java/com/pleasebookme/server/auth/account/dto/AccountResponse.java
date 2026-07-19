package com.pleasebookme.server.auth.account.dto;

import com.pleasebookme.server.auth.account.entity.AccountEntity;

import java.math.BigInteger;
import java.time.Instant;

public record AccountResponse(
    BigInteger accountId,
    BigInteger userId,
    String type,
    String provider,
    String providerAccountId,
    String providerEmail,
    Instant expiresAt,
    String tokenType,
    String scope
) {
    public static AccountResponse from(AccountEntity account) {
        return new AccountResponse(
            account.getAccountId(),
            account.getUser().getUserId(),
            account.getType(),
            account.getProvider(),
            account.getProviderAccountId(),
            account.getProviderEmail(),
            account.getExpiresAt(),
            account.getTokenType(),
            account.getScope()
        );
    }
}
