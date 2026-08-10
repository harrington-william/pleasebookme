package com.pleasebookme.server.auth.handoff.store;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface SessionHandoffStore {
    String issue(
        UUID userUid,
        Duration ttl
    );

    Optional<UUID> consume(String code);
}
