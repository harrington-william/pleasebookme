package com.pleasebookme.server.security.authorization.membership;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

public interface MembershipResolver {
    Optional<MembershipSnapshot> resolve(UUID userUid, BigInteger organizationId);
}
